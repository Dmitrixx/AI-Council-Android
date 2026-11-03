package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local gambler's fallacy signals.
 * Flags when a conclusion assumes that a sequence of independent events (coin flips, independent trials)
 * must "correct" or that a streak makes the opposite more likely, without acknowledging independence.
 * Hypotheses requiring context, not automatic verdicts. The coordinator must not treat them as definitive.
 */
public final class GamblersFallacyPolicy {
    private static final int MAX_SIGNALS = 2;

    private GamblersFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, assumesCorrection(a) || assumesCorrection(b),
                "ошибка игрока: предположение, что серия независимых исходов должна «исправиться» и противоположный исход стал более вероятным");
        add(conflicts, initial, bothAssumeIndependenceIgnored(a, b),
                "оба ответа считают, что прошлые независимые события влияют на будущую вероятность");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "gambler's fallacy (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (affirmed(text, word)) return true;
        return false;
    }

    private static boolean affirmed(String text, String phrase) {
        int from = 0;
        while (from < text.length()) {
            int i = text.indexOf(phrase, from);
            if (i < 0) return false;
            int start = Math.max(0, i - 4);
            String prefix = text.substring(start, i);
            if (!prefix.endsWith("не ") && !prefix.endsWith("not ")) return true;
            from = i + Math.max(1, phrase.length());
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean assumesCorrection(String t) {
        boolean streak = has(t, "серия", "подряд", "несколько раз подряд", "streak", "several in a row", "уже много раз");
        boolean due = has(t, "должно выпасть", "теперь должен", "исправится", "due for", "must come up", "law of averages");
        boolean independent = has(t, "независим", "монетка", "случайный", "independent", "coin flip", "random trial");
        return streak && due && !independent;
    }

    private static boolean bothAssumeIndependenceIgnored(String a, String b) {
        return assumesCorrection(a) && assumesCorrection(b);
    }
}
