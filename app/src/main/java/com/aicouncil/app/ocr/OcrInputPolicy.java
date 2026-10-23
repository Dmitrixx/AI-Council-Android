package com.aicouncil.app.ocr;

/** Pure-Java limits and normalisation for local OCR input. No Android or OCR SDK dependency. */
public final class OcrInputPolicy {
    public static final int MAX_INPUT_BYTES = 10 * 1024 * 1024;
    public static final int DEFAULT_MAX_PAGES = 5;
    public static final int ABSOLUTE_MAX_PAGES = 20;
    public static final int MAX_EXTRACTED_CHARS = 90_000;
    public static final int MAX_IMAGE_SIDE = 3200;
    public static final long MAX_IMAGE_PIXELS = 12_000_000L;

    private OcrInputPolicy() { }

    public static String normalizeLanguage(String language) {
        if ("rus".equalsIgnoreCase(language)) return "rus";
        if ("ukr".equalsIgnoreCase(language)) return "ukr";
        return "eng";
    }

    public static boolean isSupportedLanguage(String language) {
        return "eng".equals(language) || "rus".equals(language) || "ukr".equals(language);
    }

    /** A page with any non-whitespace extracted character is treated as having a text layer. */
    public static boolean hasTextLayer(String extractedText) {
        if (extractedText == null || extractedText.isEmpty()) return false;
        for (int i = 0; i < extractedText.length(); i++) {
            char c = extractedText.charAt(i);
            if (!Character.isWhitespace(c) && c != '\u0000' && !Character.isISOControl(c)) return true;
        }
        return false;
    }

    public static int sampleSize(int width, int height) {
        if (width <= 0 || height <= 0) return 1;
        int sample = 1;
        while ((width / sample > MAX_IMAGE_SIDE || height / sample > MAX_IMAGE_SIDE
                || ((long) width / sample) * ((long) height / sample) > MAX_IMAGE_PIXELS) && sample < (1 << 30)) {
            sample *= 2;
        }
        return sample;
    }

    /** Clamp the user's stored preference to a safe, documented page limit. */
    public static int clampPageLimit(int requestedPages) {
        if (requestedPages <= 0) return DEFAULT_MAX_PAGES;
        return Math.max(1, Math.min(ABSOLUTE_MAX_PAGES, requestedPages));
    }

    public static int pagesToProcess(int pageCount, int requestedPages) {
        if (pageCount <= 0) return 0;
        return Math.min(pageCount, clampPageLimit(requestedPages));
    }

    public static String sanitizeFileName(String name) {
        if (name == null || name.trim().isEmpty()) return "Файл";
        String safe = name.replace('\n', ' ').replace('\r', ' ').replace('\u0000', ' ').trim();
        return safe.length() > 120 ? safe.substring(0, 120) + "…" : safe;
    }

    public static String limitText(String text) {
        if (text == null) return "";
        if (text.length() <= MAX_EXTRACTED_CHARS) return text;
        return text.substring(0, MAX_EXTRACTED_CHARS) + "\n[Текст обрезан после лимита 90 000 символов.]";
    }
}
