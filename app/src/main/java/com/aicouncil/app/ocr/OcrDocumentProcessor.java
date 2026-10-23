package com.aicouncil.app.ocr;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import androidx.exifinterface.media.ExifInterface;

import com.aicouncil.app.OcrModelManager;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.google.android.gms.tasks.Tasks;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Local-only OCR facade for image and scanned-PDF inputs. UI, preview confirmation and task
 * insertion are intentionally left to the caller. Source bytes are never sent over the network.
 */
public final class OcrDocumentProcessor {
    private static final Object PDFBOX_INIT_LOCK = new Object();
    private static volatile boolean pdfBoxInitialized;
    public interface Cancellation { boolean isCancelled(); }
    public interface ProgressListener { void onProgress(String message); }

    /** A single input envelope for all OCR imports. Image input holds a URI; PDF input holds a bounded payload. */
    public static final class Input {
        private final Uri imageUri;
        private final byte[] pdfBytes;
        private final String sourceName;
        private final String language;
        private final int maxPages;

        private Input(Uri imageUri, byte[] pdfBytes, String sourceName, String language, int maxPages) {
            this.imageUri = imageUri;
            this.pdfBytes = pdfBytes;
            this.sourceName = sourceName;
            this.language = language;
            this.maxPages = OcrInputPolicy.clampPageLimit(maxPages);
        }
        public static Input image(Uri uri, String language) {
            if (uri == null) throw new IllegalArgumentException("image uri is required");
            return new Input(uri, null, null, language, OcrInputPolicy.DEFAULT_MAX_PAGES);
        }
        /** PDF import uses the documented default of five pages. */
        public static Input pdf(byte[] pdfBytes, String sourceName, String language) {
            return pdf(pdfBytes, sourceName, language, OcrInputPolicy.DEFAULT_MAX_PAGES);
        }
        /** PDF import: extract each page's text layer first, OCR only pages without text. */
        public static Input pdf(byte[] pdfBytes, String sourceName, String language, int maxPages) {
            if (pdfBytes == null) throw new IllegalArgumentException("PDF payload is required");
            return new Input(null, pdfBytes, sourceName, language, maxPages);
        }
        /** Compatibility alias retained for existing callers; processing is hybrid, not OCR-only. */
        public static Input scannedPdf(byte[] pdfBytes, String sourceName, String language) {
            return pdf(pdfBytes, sourceName, language, OcrInputPolicy.DEFAULT_MAX_PAGES);
        }
        public static Input scannedPdf(byte[] pdfBytes, String sourceName, String language, int maxPages) {
            return pdf(pdfBytes, sourceName, language, maxPages);
        }
    }

    public static final class Result {
        public final String sourceName;
        public final String language;
        public final String engine;
        public final String text;
        public final int pagesProcessed;
        public final List<String> warnings;

        private Result(String sourceName, String language, String engine, String text,
                       int pagesProcessed, List<String> warnings) {
            this.sourceName = OcrInputPolicy.sanitizeFileName(sourceName);
            this.language = OcrInputPolicy.normalizeLanguage(language);
            this.engine = engine == null ? "OCR" : engine;
            this.text = text == null ? "" : text;
            this.pagesProcessed = Math.max(0, pagesProcessed);
            this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
        }

        /** Visible, editable preview text. Warning metadata is made explicit instead of hidden. */
        public String previewText() {
            StringBuilder out = new StringBuilder();
            for (String warning : warnings) {
                if (warning != null && !warning.trim().isEmpty()) {
                    if (out.length() > 0) out.append("\n");
                    out.append("[Предупреждение OCR: ").append(warning.trim()).append("]");
                }
            }
            if (out.length() > 0 && !text.isEmpty()) out.append("\n\n");
            out.append(text);
            return out.toString();
        }
    }

    private final Context context;
    private final OcrModelManager modelManager;

    public OcrDocumentProcessor(Context context, OcrModelManager modelManager) {
        if (context == null) throw new IllegalArgumentException("context is required");
        if (modelManager == null) throw new IllegalArgumentException("modelManager is required");
        this.context = context.getApplicationContext();
        this.modelManager = modelManager;
    }

    /** The only public processing entry point: file input in, text and warnings out. */
    public Result process(Input input, Cancellation cancellation, ProgressListener progress) throws Exception {
        if (input == null) throw new IOException("Не выбран файл для OCR.");
        checkCancelled(cancellation);
        if (input.imageUri != null) return recognizeImage(input.imageUri, input.language, cancellation, progress);
        return recognizePdf(input.pdfBytes, input.sourceName, input.language, input.maxPages, cancellation, progress);
    }

    private Result recognizeImage(Uri uri, String requestedLanguage, Cancellation cancellation,
                                  ProgressListener progress) throws Exception {
        if (uri == null) throw new IOException("Не выбран файл изображения.");
        checkCancelled(cancellation);
        String language = OcrInputPolicy.normalizeLanguage(requestedLanguage);
        String name = displayName(uri);
        progress(progress, "Проверяем изображение и его размер…");
        Bitmap bitmap = null;
        TessBaseAPI tess = null;
        TextRecognizer recognizer = null;
        try {
            bitmap = decodeBoundedBitmap(uri);
            checkCancelled(cancellation);
            String text;
            if ("rus".equals(language) || "ukr".equals(language)) {
                progress(progress, "Проверяем или загружаем локальную модель OCR…");
                ensureModel(language, cancellation);
                checkCancelled(cancellation);
                tess = new TessBaseAPI();
                if (!tess.init(modelManager.getTesseractDirectory().getAbsolutePath(), language,
                        TessBaseAPI.OEM_LSTM_ONLY)) {
                    try { tess.recycle(); } catch (Exception ignored) { }
                    tess = null;
                    File invalid = modelManager.modelFileFor(language);
                    if (invalid.exists()) invalid.delete();
                    throw new IOException("Модель OCR не прошла проверку и удалена. Повторно выберите OCR кириллицы, чтобы скачать её заново.");
                }
                progress(progress, "Распознаём текст локально…");
                tess.setImage(bitmap);
                text = tess.getUTF8Text();
            } else {
                progress(progress, "Распознаём текст локально…");
                recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                Text result = Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap, 0)), 45, TimeUnit.SECONDS);
                text = result == null ? "" : result.getText();
            }
            checkCancelled(cancellation);
            if (text == null || text.trim().isEmpty()) {
                throw new IOException("Текст не найден. Попробуйте более чёткое изображение с хорошим освещением.");
            }
            List<String> warnings = new ArrayList<>();
            warnings.add("Распознавание выполняется на устройстве; проверьте ошибки OCR перед вставкой.");
            if (text.length() > OcrInputPolicy.MAX_EXTRACTED_CHARS) {
                warnings.add("Распознанный текст ограничен 90 000 символами.");
                text = OcrInputPolicy.limitText(text);
            }
            return new Result(name, language, "rus".equals(language) || "ukr".equals(language)
                    ? "Tesseract · " + language : "ML Kit · Latin", text, 1, warnings);
        } finally {
            if (recognizer != null) try { recognizer.close(); } catch (Exception ignored) { }
            if (tess != null) try { tess.recycle(); } catch (Exception ignored) { }
            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
        }
    }

    private Result recognizePdf(byte[] pdfBytes, String sourceName, String requestedLanguage, int requestedMaxPages,
                                Cancellation cancellation, ProgressListener progress) throws Exception {
        if (pdfBytes == null || pdfBytes.length == 0) throw new IOException("PDF пуст.");
        if (pdfBytes.length > OcrInputPolicy.MAX_INPUT_BYTES) throw new IOException("PDF превышает 10 МБ.");
        String language = OcrInputPolicy.normalizeLanguage(requestedLanguage);
        if (!OcrInputPolicy.isSupportedLanguage(language)) throw new IOException("Выбран неподдерживаемый язык OCR.");
        checkCancelled(cancellation);

        final int maxPages = OcrInputPolicy.clampPageLimit(requestedMaxPages);
        final long deadline = System.currentTimeMillis() + 45_000L;
        File tempPdf = File.createTempFile("aicouncil_pdf_", ".pdf", context.getCacheDir());
        TessBaseAPI tess = null;
        TextRecognizer recognizer = null;
        Bitmap pageBitmap = null;
        StringBuilder out = new StringBuilder();
        List<String> warnings = new ArrayList<>();
        int ocrPages = 0;
        int processedPages = 0;
        boolean usedTextLayer = false;
        boolean usedOcr = false;
        try {
            try (FileOutputStream stream = new FileOutputStream(tempPdf)) {
                stream.write(pdfBytes);
                stream.flush();
                stream.getFD().sync();
            }
            synchronized (PDFBOX_INIT_LOCK) {
                if (!pdfBoxInitialized) {
                    PDFBoxResourceLoader.init(context.getApplicationContext());
                    pdfBoxInitialized = true;
                }
            }
            try (PDDocument textDocument = PDDocument.load(pdfBytes);
                 ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(tempPdf, ParcelFileDescriptor.MODE_READ_ONLY);
                 PdfRenderer renderer = new PdfRenderer(descriptor)) {
                int rendererPages = renderer.getPageCount();
                int pdfBoxPages = textDocument.getNumberOfPages();
                int pageCount = Math.min(rendererPages, pdfBoxPages);
                if (pageCount <= 0) throw new IOException("В PDF отсутствуют страницы.");
                if (rendererPages != pdfBoxPages) warnings.add("Число страниц у разных средств чтения PDF не совпало; обработана общая доступная часть.");
                int pagesToRead = OcrInputPolicy.pagesToProcess(pageCount, maxPages);
                for (int pageIndex = 0; pageIndex < pagesToRead; pageIndex++) {
                    checkCancelled(cancellation);
                    long remaining = deadline - System.currentTimeMillis();
                    if (remaining <= 0) throw new java.util.concurrent.TimeoutException("Обработка PDF превысила лимит времени 45 секунд.");
                    progress(progress, "PDF: анализ страницы " + (pageIndex + 1) + " из " + pagesToRead + "…");

                    String pageText = extractTextLayer(textDocument, pageIndex);
                    boolean pageHasText = OcrInputPolicy.hasTextLayer(pageText);
                    if (pageHasText) {
                        usedTextLayer = true;
                        pageText = pageText.trim();
                    } else {
                        ocrPages++;
                        usedOcr = true;
                        if ("rus".equals(language) || "ukr".equals(language)) {
                            if (tess == null) {
                                progress(progress, "Подготавливаем локальную модель OCR…");
                                ensureModel(language, cancellation);
                                checkCancelled(cancellation);
                                tess = new TessBaseAPI();
                                if (!tess.init(modelManager.getTesseractDirectory().getAbsolutePath(), language, TessBaseAPI.OEM_LSTM_ONLY)) {
                                    try { tess.recycle(); } catch (Exception ignored) { }
                                    tess = null;
                                    File invalid = modelManager.modelFileFor(language);
                                    if (invalid.exists()) invalid.delete();
                                    throw new IOException("Модель OCR не прошла проверку и удалена. Повторите импорт, чтобы скачать её заново.");
                                }
                            }
                        } else if (recognizer == null) {
                            recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                        }

                        progress(progress, "OCR: страница " + (pageIndex + 1) + " из " + pagesToRead + "…");
                        try (PdfRenderer.Page page = renderer.openPage(pageIndex)) {
                            int pageWidth = page.getWidth();
                            int pageHeight = page.getHeight();
                            if (pageWidth > 0 && pageHeight > 0) {
                                double ratio = Math.sqrt(1_800_000.0 / ((double) pageWidth * pageHeight));
                                float scale = (float) Math.max(0.5, Math.min(1.6, ratio));
                                int renderWidth = Math.max(1, (int) Math.ceil(pageWidth * scale));
                                int renderHeight = Math.max(1, (int) Math.ceil(pageHeight * scale));
                                if ((long) renderWidth * renderHeight > 2_000_000L) {
                                    float correction = (float) Math.sqrt(2_000_000.0 / ((double) renderWidth * renderHeight));
                                    renderWidth = Math.max(1, (int) (renderWidth * correction));
                                    renderHeight = Math.max(1, (int) (renderHeight * correction));
                                }
                                pageBitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.RGB_565);
                                pageBitmap.eraseColor(Color.WHITE);
                                page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                                try {
                                    if (tess != null) {
                                        tess.setImage(pageBitmap);
                                        pageText = tess.getUTF8Text();
                                    } else {
                                        Text recognized = Tasks.await(recognizer.process(InputImage.fromBitmap(pageBitmap, 0)),
                                                Math.max(1L, Math.min(12_000L, remaining)), TimeUnit.MILLISECONDS);
                                        pageText = recognized == null ? "" : recognized.getText();
                                    }
                                } catch (java.util.concurrent.TimeoutException timeout) {
                                    throw timeout;
                                } catch (Exception recognitionError) {
                                    warnings.add("На странице " + (pageIndex + 1) + " OCR завершился ошибкой; страница отмечена в предпросмотре.");
                                    pageText = "";
                                } finally {
                                    if (pageBitmap != null && !pageBitmap.isRecycled()) pageBitmap.recycle();
                                    pageBitmap = null;
                                }
                            } else {
                                pageText = "";
                            }
                        } catch (java.util.concurrent.TimeoutException timeout) {
                            throw timeout;
                        } catch (Exception renderError) {
                            warnings.add("Страницу " + (pageIndex + 1) + " не удалось отрисовать для OCR.");
                            pageText = "";
                        }
                        checkCancelled(cancellation);
                        if (System.currentTimeMillis() > deadline) throw new java.util.concurrent.TimeoutException("Обработка PDF превысила лимит времени 45 секунд.");
                    }

                    String marker = "[Страница PDF " + (pageIndex + 1) + (pageHasText ? " · текстовый слой" : " · OCR") + "]\n";
                    if (out.length() > 0) out.append("\n\n");
                    if (out.length() + marker.length() >= OcrInputPolicy.MAX_EXTRACTED_CHARS) {
                        warnings.add("Результат ограничен 90 000 символами.");
                        break;
                    }
                    out.append(marker);
                    String clean = pageText == null ? "" : pageText.trim();
                    if (clean.isEmpty()) {
                        out.append("[Текст на странице не распознан]");
                        warnings.add("На странице " + (pageIndex + 1) + " текст не распознан.");
                    } else {
                        int remainingChars = OcrInputPolicy.MAX_EXTRACTED_CHARS - out.length();
                        if (clean.length() > remainingChars) {
                            out.append(clean, 0, Math.max(0, remainingChars));
                            warnings.add("Результат ограничен 90 000 символами.");
                            processedPages++;
                            break;
                        }
                        out.append(clean);
                    }
                    processedPages++;
                }
                if (pageCount > pagesToRead) warnings.add("Обработаны первые " + pagesToRead + " страниц из " + pageCount + ".");
            }
        } catch (java.util.concurrent.TimeoutException timeout) {
            throw new IOException("Обработка PDF заняла слишком много времени. Попробуйте файл меньшего размера или с меньшим количеством страниц.", timeout);
        } finally {
            if (recognizer != null) try { recognizer.close(); } catch (Exception ignored) { }
            if (tess != null) try { tess.recycle(); } catch (Exception ignored) { }
            if (pageBitmap != null && !pageBitmap.isRecycled()) pageBitmap.recycle();
            //noinspection ResultOfMethodCallIgnored
            tempPdf.delete();
        }

        if (processedPages == 0 || out.length() == 0) throw new IOException("Не удалось обработать страницы PDF.");
        if (usedOcr) warnings.add("Сканированные страницы распознавались локально. Проверьте ошибки OCR перед вставкой.");
        String mode = usedTextLayer && usedOcr ? "текстовый слой + локальный OCR" : usedTextLayer ? "текстовый слой PDF" : "локальный OCR";
        String header = "[PDF: " + mode + "; страниц обработано — " + processedPages + ".\nИсточник: "
                + OcrInputPolicy.sanitizeFileName(sourceName) + "]\n\n";
        String text = header + out.toString().trim();
        if (text.length() > OcrInputPolicy.MAX_EXTRACTED_CHARS) text = OcrInputPolicy.limitText(text);
        String engine = usedTextLayer && usedOcr
                ? ("rus".equals(language) || "ukr".equals(language) ? "PDF text layer + Tesseract · " + language : "PDF text layer + ML Kit · Latin")
                : usedTextLayer ? "PDF text layer" : ("rus".equals(language) || "ukr".equals(language) ? "Tesseract · " + language : "ML Kit · Latin");
        return new Result(sourceName, language, engine, text, processedPages, warnings);
    }

    private static String extractTextLayer(PDDocument document, int zeroBasedPage) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setLineSeparator("\n");
        stripper.setStartPage(zeroBasedPage + 1);
        stripper.setEndPage(zeroBasedPage + 1);
        String text = stripper.getText(document);
        return text == null ? "" : text;
    }

    private void ensureModel(String language, Cancellation cancellation) throws IOException {
        modelManager.ensureModel(language, () -> isCancelled(cancellation));
    }

    private Bitmap decodeBoundedBitmap(Uri uri) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        long size = -1;
        try (Cursor cursor = resolver.query(uri, new String[]{OpenableColumns.SIZE}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (index >= 0 && !cursor.isNull(index)) size = cursor.getLong(index);
            }
        } catch (Exception ignored) { }
        if (size > OcrInputPolicy.MAX_INPUT_BYTES) throw new IOException("Изображение превышает 10 МБ. Выберите файл меньшего размера.");
        try (InputStream input = resolver.openInputStream(uri)) {
            if (input == null) throw new IOException("Не удалось открыть изображение.");
            byte[] buffer = new byte[8192];
            long total = 0;
            int n;
            while ((n = input.read(buffer)) != -1) {
                total += n;
                if (total > OcrInputPolicy.MAX_INPUT_BYTES) throw new IOException("Изображение превышает 10 МБ. Выберите файл меньшего размера.");
            }
            if (total == 0) throw new IOException("Выбранное изображение пустое.");
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream input = resolver.openInputStream(uri)) {
            if (input == null) throw new IOException("Не удалось прочитать размеры изображения.");
            BitmapFactory.decodeStream(input, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw new IOException("Файл не является поддерживаемым изображением.");
        BitmapFactory.Options decode = new BitmapFactory.Options();
        decode.inSampleSize = OcrInputPolicy.sampleSize(bounds.outWidth, bounds.outHeight);
        decode.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap;
        try (InputStream input = resolver.openInputStream(uri)) {
            if (input == null) throw new IOException("Не удалось открыть изображение для распознавания.");
            bitmap = BitmapFactory.decodeStream(input, null, decode);
        }
        if (bitmap == null) throw new IOException("Не удалось декодировать изображение.");
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try (InputStream input = resolver.openInputStream(uri)) {
            if (input != null) orientation = new ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception ignored) { }
        Bitmap oriented = orientBitmap(bitmap, orientation);
        if (oriented != bitmap) bitmap.recycle();
        return oriented;
    }

    private static Bitmap orientBitmap(Bitmap source, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matrix.setScale(-1f, 1f); break;
            case ExifInterface.ORIENTATION_ROTATE_180: matrix.setRotate(180f); break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL: matrix.setRotate(180f); matrix.postScale(-1f, 1f); break;
            case ExifInterface.ORIENTATION_TRANSPOSE: matrix.setRotate(90f); matrix.postScale(-1f, 1f); break;
            case ExifInterface.ORIENTATION_ROTATE_90: matrix.setRotate(90f); break;
            case ExifInterface.ORIENTATION_TRANSVERSE: matrix.setRotate(-90f); matrix.postScale(-1f, 1f); break;
            case ExifInterface.ORIENTATION_ROTATE_270: matrix.setRotate(-90f); break;
            default: return source;
        }
        try { return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true); }
        catch (RuntimeException ignored) { return source; }
    }

    private String displayName(Uri uri) {
        String name = null;
        try (Cursor cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) name = cursor.getString(index);
            }
        } catch (Exception ignored) { }
        if (name == null || name.trim().isEmpty()) name = "Файл";
        return OcrInputPolicy.sanitizeFileName(name);
    }

    private static boolean isCancelled(Cancellation cancellation) {
        return Thread.currentThread().isInterrupted() || (cancellation != null && cancellation.isCancelled());
    }

    private static void checkCancelled(Cancellation cancellation) throws IOException {
        if (isCancelled(cancellation)) throw new IOException("Распознавание OCR отменено.");
    }

    private static void progress(ProgressListener listener, String message) {
        if (listener != null) listener.onProgress(message);
    }
}
