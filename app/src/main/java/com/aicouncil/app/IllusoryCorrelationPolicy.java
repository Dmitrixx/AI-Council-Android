package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local illusory-correlation check.
 * Flags a verdict that treats co-occurrence, joint frequency or "they often
 * appear together" as a real association without an independent base-rate
 * comparison, control comparison, or an explicit note that chance is plausible.
 * Naming the gap is not itself an error.
 * Signals remain hypotheses requiring context.
 */
public final class IllusoryCorrelationPolicy {
    private static final int MAX_SIGNALS = 2;

    private IllusoryCorrelationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "illusory correlation: co-occurrence treated as a real association without base rate");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают связь на совместной частоте без сравнения с базовой");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "illusory correlation (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || hasBaseRateCheck(t)) return false;
        return hasCoOccurrence(t) && closesOnLink(t);
    }

    private static boolean hasCoOccurrence(String t) {
        return has(t,
                "часто вместе",
                "часто встречаются",
                "совместно появляются",
                "часто появляются вместе",
                "часто наблюдаются вместе",
                "often together",
                "often co-occur",
                "appear together often",
                "frequently paired",
                "часто йдуть разом");
    }

    private static boolean closesOnLink(String t) {
        return has(t,
                "значит связаны",
                "значит есть связь",
                "доказывает связь",
                "поэтому связаны",
                "therefore related",
                "so they are linked",
                "proves association",
                "must be connected",
                "therefore associated");
    }

    private static boolean hasBaseRateCheck(String t) {
        return has(t,
                "базовая частота",
                "сравнил с базовой",
                "случайное совпадение",
                "может быть случайно",
                "base rate",
                "chance co-occurrence",
                "expected by chance",
                "independent of chance",
                "after base-rate adjustment");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "illusory correlation",
                "иллюзорная корреляция",
                "ложная корреляция",
                "co-occurrence is not association");
    }

    private static boolean has(String text, String... phrases) {
        if (text == null) return false;
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
