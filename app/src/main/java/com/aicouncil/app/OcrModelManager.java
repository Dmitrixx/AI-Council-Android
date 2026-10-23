package com.aicouncil.app;

import okhttp3.Call;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Owns app-private Tesseract models and verifies them before installation/use. */
public final class OcrModelManager {
    public interface Cancellation { boolean isCancelled(); }

    public static final String CANCELLATION_MESSAGE = "Загрузка модели OCR отменена пользователем.";
    private static final int MAX_MODEL_BYTES = 8 * 1024 * 1024;
    private static final int MIN_MODEL_BYTES = 1_000_000;
    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 30_000;

    private final File tesseractDirectory;
    private final OkHttpClient http;
    private volatile Call activeCall;

    public OcrModelManager(File filesDirectory) {
        if (filesDirectory == null) throw new IllegalArgumentException("filesDirectory is required");
        this.tesseractDirectory = new File(filesDirectory, "tesseract");
        Dns publicOnlyDns = hostname -> {
            List<InetAddress> addresses = Dns.SYSTEM.lookup(hostname);
            if (!WebPageFetchPolicy.areResolvedAddressesPublic(addresses)) {
                throw new UnknownHostException("OCR model host did not resolve to public unicast addresses");
            }
            // Pin the checked records for this request to avoid a second, unchecked DNS lookup.
            return new ArrayList<>(addresses);
        };
        this.http = new OkHttpClient.Builder()
                .dns(publicOnlyDns)
                .connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .callTimeout(READ_TIMEOUT_MS + CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .build();
    }

    public File getTesseractDirectory() { return tesseractDirectory; }

    public File modelFileFor(String language) throws IOException {
        if (!OcrModelIntegrityPolicy.isSupportedLanguage(language)) {
            throw new IOException("Выбран неподдерживаемый язык OCR.");
        }
        return new File(new File(tesseractDirectory, "tessdata"), language + ".traineddata");
    }

    public boolean hasUsableModel(String language) {
        try {
            File file = modelFileFor(language);
            long expectedLength = OcrModelIntegrityPolicy.expectedLength(language);
            if (!file.isFile() || file.length() < MIN_MODEL_BYTES || file.length() > MAX_MODEL_BYTES) return false;
            return file.length() == expectedLength
                    && OcrModelIntegrityPolicy.verify(language, file.length(), OcrModelIntegrityPolicy.sha512(file));
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Backward-compatible synchronous entry point. */
    public File ensureModel(String language) throws IOException { return ensureModel(language, null); }

    /** Downloads with OkHttp to a temporary file; verifies exact length and SHA-512 before atomic-style replacement. */
    public synchronized File ensureModel(String language, Cancellation cancellation) throws IOException {
        checkCancelled(cancellation, null);
        File model = modelFileFor(language);
        if (hasUsableModel(language)) return model;

        File folder = model.getParentFile();
        if (folder == null || (!folder.isDirectory() && !folder.mkdirs())) {
            throw new IOException("Не удалось создать закрытую папку для модели OCR.");
        }
        File temp = new File(folder, language + ".traineddata.download");
        File backup = new File(folder, language + ".traineddata.previous");
        Call call = null;
        boolean installed = false;
        boolean backupCreated = false;
        try {
            String target = "https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/"
                    + OcrModelIntegrityPolicy.VERSION + "/" + language + ".traineddata";
            if (!WebPageFetchPolicy.isAllowedUrl(target)) throw new IOException("Адрес загрузки модели OCR не прошёл проверку безопасности.");
            Request request = new Request.Builder().url(target).get()
                    .header("User-Agent", "AI-Council-Android/2.0.0")
                    .header("Accept", "application/octet-stream")
                    .header("Accept-Encoding", "identity")
                    .build();
            call = http.newCall(request);
            activeCall = call;
            checkCancelled(cancellation, call);

            try (Response response = call.execute()) {
                checkCancelled(cancellation, call);
                if (response.code() != 200) {
                    throw new IOException("Не удалось скачать модель OCR (HTTP " + response.code() + "). Проверьте интернет и повторите попытку.");
                }
                ResponseBody responseBody = response.body();
                if (responseBody == null) throw new IOException("Сервер не передал файл модели OCR.");
                long expectedLength = OcrModelIntegrityPolicy.expectedLength(language);
                long declaredLength = responseBody.contentLength();
                if (declaredLength >= 0 && declaredLength != expectedLength) {
                    throw new IOException("Размер скачиваемой модели OCR не соответствует закреплённому файлу версии "
                            + OcrModelIntegrityPolicy.VERSION + ".");
                }
                if (declaredLength > MAX_MODEL_BYTES || (declaredLength >= 0 && declaredLength < MIN_MODEL_BYTES)) {
                    throw new IOException("Размер модели OCR не прошёл проверку безопасности.");
                }

                long total = 0L;
                try (InputStream input = responseBody.byteStream(); FileOutputStream output = new FileOutputStream(temp)) {
                    byte[] buffer = new byte[16 * 1024];
                    int count;
                    while (true) {
                        checkCancelled(cancellation, call);
                        count = input.read(buffer);
                        if (count == -1) break;
                        total += count;
                        if (total > MAX_MODEL_BYTES || total > expectedLength) {
                            throw new IOException("Файл модели OCR превышает закреплённый размер.");
                        }
                        output.write(buffer, 0, count);
                    }
                    output.flush();
                    output.getFD().sync();
                }
                checkCancelled(cancellation, call);
                if (total != expectedLength || !temp.isFile() || temp.length() != expectedLength) {
                    throw new IOException("Скачанная модель OCR имеет неверный размер. Попробуйте ещё раз.");
                }
                String actualSha512 = OcrModelIntegrityPolicy.sha512(temp);
                if (!OcrModelIntegrityPolicy.verify(language, total, actualSha512)) {
                    throw new IOException("Контрольная сумма модели OCR не совпала с официальной версией "
                            + OcrModelIntegrityPolicy.VERSION + ". Файл отклонён и не будет установлен.");
                }
            } catch (IOException networkError) {
                if (isCancelled(cancellation, call)) throw cancelled(networkError);
                throw networkError;
            }

            checkCancelled(cancellation, call);
            // Keep a recoverable copy until the verified temporary file has been installed.
            if (backup.exists() && !backup.delete()) throw new IOException("Не удалось удалить устаревшую резервную модель OCR.");
            if (model.exists()) {
                if (!model.renameTo(backup)) throw new IOException("Не удалось подготовить замену модели OCR.");
                backupCreated = true;
            }
            if (!temp.renameTo(model)) {
                if (backupCreated && backup.renameTo(model)) backupCreated = false;
                throw new IOException("Не удалось установить модель OCR во внутреннее хранилище.");
            }
            installed = true;
            if (backupCreated) {
                if (backup.exists()) backup.delete();
                backupCreated = false;
            }
            return model;
        } finally {
            if (activeCall == call) activeCall = null;
            if (!installed && temp.exists()) temp.delete();
            if (backupCreated && backup.exists() && !model.exists()) backup.renameTo(model);
        }
    }

    /** Cancel a currently executing OkHttp call, including a blocked body read. */
    public void cancelActiveDownload() {
        Call call = activeCall;
        if (call != null) call.cancel();
    }

    public static boolean isCancellation(IOException error) {
        return error != null && CANCELLATION_MESSAGE.equals(error.getMessage());
    }

    private static void checkCancelled(Cancellation cancellation, Call call) throws IOException {
        if (Thread.currentThread().isInterrupted() || (cancellation != null && cancellation.isCancelled())
                || (call != null && call.isCanceled())) {
            throw cancelled(null);
        }
    }

    private static boolean isCancelled(Cancellation cancellation, Call call) {
        return Thread.currentThread().isInterrupted() || (cancellation != null && cancellation.isCancelled())
                || (call != null && call.isCanceled());
    }

    private static IOException cancelled(Throwable cause) {
        return new IOException(CANCELLATION_MESSAGE, cause);
    }
}
