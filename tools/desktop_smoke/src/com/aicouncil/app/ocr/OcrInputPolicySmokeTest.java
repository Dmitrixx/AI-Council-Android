package com.aicouncil.app.ocr;

public final class OcrInputPolicySmokeTest {
    private static int tests;
    private static void check(boolean condition, String name) {
        tests++;
        if (!condition) throw new AssertionError(name);
    }
    public static void main(String[] args) {
        check(OcrInputPolicy.MAX_INPUT_BYTES == 10 * 1024 * 1024, "image/PDF byte limit remains 10 MiB");
        check("rus".equals(OcrInputPolicy.normalizeLanguage("rus")), "Russian language retained");
        check("ukr".equals(OcrInputPolicy.normalizeLanguage("ukr")), "Ukrainian language retained");
        check("eng".equals(OcrInputPolicy.normalizeLanguage("unknown")), "unknown language falls back to Latin recognizer");
        check(OcrInputPolicy.sampleSize(6000, 4000) >= 2, "large image is downsampled before allocation");
        check(OcrInputPolicy.sampleSize(1000, 1000) == 1, "small image is not unnecessarily downsampled");
        int extremeSample = OcrInputPolicy.sampleSize(1_000_000, 500_000);
        check(extremeSample > 128 && 1_000_000 / extremeSample <= OcrInputPolicy.MAX_IMAGE_SIDE,
                "extreme compressed-image dimensions remain bounded");
        check(OcrInputPolicy.pagesToProcess(30, 5) == 5, "default PDF page limit is five");
        check(OcrInputPolicy.pagesToProcess(30, 20) == 20, "requested PDF limit cannot exceed twenty");
        check(OcrInputPolicy.clampPageLimit(0) == 5, "invalid stored preference returns to safe default");
        check(OcrInputPolicy.clampPageLimit(20) == 20, "user can raise page limit to twenty");
        check(OcrInputPolicy.clampPageLimit(200) == 20, "page preference is clamped at twenty");
        check(OcrInputPolicy.clampPageLimit(-1) == 5, "negative page preference returns to default");
        check(OcrInputPolicy.pagesToProcess(3, 20) == 3, "small PDF keeps its page count");
        check("doc name.pdf".equals(OcrInputPolicy.sanitizeFileName("doc\nname.pdf")), "filename removes control line breaks");
        check(OcrInputPolicy.sanitizeFileName(repeat('x', 130)).length() == 121, "filename has bounded display length");
        check(OcrInputPolicy.limitText(repeat('a', 90001)).length() <= 90100, "OCR output is bounded with a marker");
        check(OcrInputPolicy.isSupportedLanguage("eng") && OcrInputPolicy.isSupportedLanguage("rus")
                && OcrInputPolicy.isSupportedLanguage("ukr"), "only known OCR languages are supported");
        check(!OcrInputPolicy.hasTextLayer(null), "missing extracted text selects OCR fallback");
        check(!OcrInputPolicy.hasTextLayer("  \n\t  "), "whitespace-only PDF page selects OCR fallback");
        check(OcrInputPolicy.hasTextLayer("  Header\nparagraph "), "real text layer skips OCR");
        check(!OcrInputPolicy.hasTextLayer("\u0000\u0001\n"), "control-only PDF page selects OCR fallback");
        System.out.println("OcrInputPolicySmokeTest: " + tests + "/" + tests + " passed");
    }
    private static String repeat(char ch, int count) {
        StringBuilder out = new StringBuilder(count);
        for (int i = 0; i < count; i++) out.append(ch);
        return out.toString();
    }
}
