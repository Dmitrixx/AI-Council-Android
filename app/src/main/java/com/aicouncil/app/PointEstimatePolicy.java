package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local point-estimate / uncertainty neglect signals.
 * Flags a verdict that treats a single number, percentage, or forecast as precise and decisive
 * without stating a range, confidence interval, sensitivity to assumptions, or that the figure is an estimate.
 * Naming the uncertainty is not itself an error.
 * The coordinator cannot close solely on a bare point estimate.
 */
public final class PointEstimatePolicy {
    private static final int MAX_SIGNALS = 2;

    private PointEstimatePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, decisiveNumberWithoutUncertainty(a) || decisiveNumberWithoutUncertainty(b),
                "point estimate: вердикт закрывает выбор на точном числе без диапазона, доверительного интервала или чувствительности");
        add(conflicts, initial, decisiveNumberWithoutUncertainty(a) && decisiveNumberWithoutUncertainty(b),
                "оба эксперта закрывают на точечной оценке без неопределённости");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "point estimate (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean acknowledgesUncertainty(String t) {
        return has(t,
                "диапазон", "интервал", "confidence interval", "ci ", "±", "+/-",
                "примерно", "около", "estimate", "оценка", "неопределённость",
                "uncertainty", "чувствительн", "sensitivity", "может быть от",
                "between", "от ... до", "roughly");
    }

    private static boolean decisiveNumberWithoutUncertainty(String t) {
        boolean hasNumber = t.matches(".*\\d+(\\.\\d+)?(%| percent| процент).*") || has(t, "точно", "exactly", "precisely", "ровно");
        boolean decisive = has(t,
                "поэтому", "значит", "следовательно", "clearly", " decisively",
                "выбираем", "закрываем", "рекомендую", "should do", "the number is");
        return hasNumber && decisive && !acknowledgesUncertainty(t);
    }
}
