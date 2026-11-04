package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local scope-insensitivity signals.
 * The same recommendation for a village and a country, or for 10 and 10,000,
 * is not evidence that magnitude does not matter. Hypotheses, not verdicts.
 */
public final class ScopeInsensitivityPolicy {
    private static final int MAX_SIGNALS = 2;

    private ScopeInsensitivityPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, scopeDismissed(a) || scopeDismissed(b),
                "масштаб объявлен неважным без отношения величин");
        add(conflicts, initial, bothDismiss(a, b),
                "оба эксперта держат одну силу вывода при разном масштабе");
        add(conflicts, initial, fork(a, b),
                "развилка: масштаб неважен против явного отношения величин");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "scope insensitivity (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(36, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean scopeDismissed(String text) {
        return hasDismissal(text) && !hasMagnitude(text);
    }

    private static boolean bothDismiss(String a, String b) {
        return scopeDismissed(a) && scopeDismissed(b);
    }

    private static boolean fork(String a, String b) {
        return (scopeDismissed(a) && hasMagnitude(b))
                || (scopeDismissed(b) && hasMagnitude(a));
    }

    private static boolean hasDismissal(String text) {
        return contains(text,
                "масштаб не важен", "масштаб не меняет", "масштаб не влияет",
                "число не важно", "неважно сколько", "не важно сколько",
                "10 или 10000 одно", "10 или 10 000 одно", "10 или 10000 — одно",
                "деревня или страна — одно", "масштаб не меняет вывод",
                "scope doesn't matter", "scope does not matter",
                "the number doesn't matter", "the number does not matter",
                "whether 10 or 10000", "whether ten or ten thousand",
                "same if it's a village or a country",
                "magnitude doesn't change", "magnitude does not change");
    }

    private static boolean hasMagnitude(String text) {
        return contains(text,
                "пропорционально масштабу", "масштаб учтён", "масштаб учтен",
                "в 100 раз", "в 1000 раз", "на порядок", "на два порядка",
                "на душу", "отношение величин",
                "per person", "per capita", "orders of magnitude",
                "ratio of", "scales with", "proportional to the scope");
    }

    private static boolean contains(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
