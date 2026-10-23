package com.aicouncil.app.ocr;

import android.content.Context;
import com.aicouncil.app.OcrModelManager;
import java.io.File;

/** Integration-shape test executed against compile-time Android/OCR API stubs, never a device. */
public final class OcrDocumentProcessorSmokeTest {
    private static int tests;
    private static void check(boolean condition, String message) {
        tests++;
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        OcrDocumentProcessor processor = new OcrDocumentProcessor(
                new Context(), new OcrModelManager(new File(System.getProperty("java.io.tmpdir"), "aicouncil_ocr_smoke")));
        OcrDocumentProcessor.Result result = processor.process(
                OcrDocumentProcessor.Input.pdf(new byte[]{1, 2, 3}, "mixed.pdf", "eng"),
                () -> false, null);
        check("PDF text layer".equals(result.engine), "page with extractable text uses PDF text layer without starting OCR");
        check(result.pagesProcessed == 1, "one available page is represented in result metadata");
        check(result.text.contains("[Страница PDF 1 · текстовый слой]"), "preview identifies page using text layer");
        check(result.text.contains("test"), "text layer content is retained in result");
        check(result.warnings.isEmpty(), "text-layer-only PDF does not claim OCR was performed");
        check(OcrDocumentProcessor.Input.pdf(new byte[]{1}, "wide.pdf", "eng", 20) != null,
                "PDF input accepts the explicit twenty-page preference");
        check(OcrDocumentProcessor.Input.scannedPdf(new byte[]{1}, "default.pdf", "rus").getClass() != null,
                "legacy scanned-PDF input remains source-compatible");
        System.out.println("OcrDocumentProcessorSmokeTest: " + tests + "/" + tests + " passed");
    }
}
