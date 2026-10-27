package com.aicouncil.app;

/** Deterministic helper for locating which model produced the most recently truncated answer. */
public final class TruncatedAnswerPolicy {
    public static final String TRUNCATION_MARKER = "⚠️ Ответ, вероятно, обрезан лимитом токенов.";

    private static final String[] HEADINGS = {
        "⚡ БЫСТРЫЙ ОТВЕТ", "📝 ИСХОДНЫЙ ОТВЕТ", "🛡 НЕЗАВИСИМЫЙ АУДИТ",
        "✦ ИИ №1", "✦ ИИ №2", "✦ ИИ №3", "🔎 ВЗАИМНАЯ КРИТИКА · ИИ №1",
        "🔎 ВЗАИМНАЯ КРИТИКА · ИИ №2", "🔎 ВЗАИМНАЯ КРИТИКА · ИИ №3", "🏁 ИТОГ КООРДИНАТОРА",
        "🧠 ДОПОЛНЕНИЕ ИИ №1", "🧠 ДОПОЛНЕНИЕ ИИ №2", "🧠 ДОПОЛНЕНИЕ ИИ №3"
    };
    private static final int[] SLOTS = {0, 0, 1, 2, 0, 1, 2, 3, 0, 1, 2};

    private TruncatedAnswerPolicy() {}

    public static boolean hasTruncatedAnswer(String answer) {
        return answer != null && answer.contains(TRUNCATION_MARKER);
    }

    /** Returns the best matching provider slot (0..3), or -1 when there is no truncation marker. */
    public static int chooseExpertSlot(String answer) {
        if (!hasTruncatedAnswer(answer)) return -1;
        int markerAt = answer.lastIndexOf(TRUNCATION_MARKER);
        int best = -1;
        int slot = 0;
        for (int i = 0; i < HEADINGS.length; i++) {
            int position = answer.lastIndexOf(HEADINGS[i], markerAt);
            if (position > best) {
                best = position;
                slot = SLOTS[i];
            }
        }
        return slot;
    }
}
