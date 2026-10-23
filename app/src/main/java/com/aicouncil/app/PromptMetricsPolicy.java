package com.aicouncil.app;

import java.util.Locale;

/** Lightweight, offline-only prompt size estimate. Token counts are intentionally approximate. */
public final class PromptMetricsPolicy {
    private PromptMetricsPolicy() { }

    /**
     * Estimates token count from Unicode character classes without transmitting or retaining text.
     * Real tokenization varies by provider/model; this estimate is for rough planning only.
     */
    public static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        double estimate = 0.0;
        for (int offset = 0; offset < text.length();) {
            int cp = text.codePointAt(offset);
            offset += Character.charCount(cp);
            if (Character.isWhitespace(cp)) {
                estimate += 0.20;
                continue;
            }
            Character.UnicodeScript script = Character.UnicodeScript.of(cp);
            if (script == Character.UnicodeScript.HAN
                    || script == Character.UnicodeScript.HIRAGANA
                    || script == Character.UnicodeScript.KATAKANA
                    || script == Character.UnicodeScript.HANGUL) {
                estimate += 1.0;
            } else if (Character.isLetterOrDigit(cp)) {
                if (script == Character.UnicodeScript.CYRILLIC) estimate += 0.40;
                else if (script == Character.UnicodeScript.ARABIC
                        || script == Character.UnicodeScript.HEBREW
                        || script == Character.UnicodeScript.DEVANAGARI
                        || script == Character.UnicodeScript.THAI) estimate += 0.45;
                else estimate += 0.25;
            } else {
                // Punctuation and emoji often split into tokens more aggressively than plain text.
                estimate += 0.50;
            }
        }
        return Math.max(1, (int) Math.ceil(estimate));
    }

    public static String sizeLabel(int estimatedTokens) {
        if (estimatedTokens <= 0) return "Пустой запрос";
        if (estimatedTokens <= 800) return "короткий запрос";
        if (estimatedTokens <= 2500) return "средний объём";
        if (estimatedTokens <= 6000) return "большой объём";
        return "очень большой запрос";
    }

    public static String compactNumber(int number) {
        return String.format(Locale.getDefault(), "%,d", Math.max(0, number)).replace(',', ' ');
    }
}
