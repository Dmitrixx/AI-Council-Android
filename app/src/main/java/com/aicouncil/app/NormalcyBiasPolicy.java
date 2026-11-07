package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local normalcy-bias signals.
 * Underestimating the probability or impact of a disruptive event, black swan, regime change or disaster
 * solely because it has not happened recently ("things will stay normal", "this never happened before",
 * "everything will continue as usual") without explicit base rates, historical precedents outside the recent window,
 * tail-risk analysis or scenario planning, is a hypothesis, not evidence.
 * Distinct from status-quo bias (preference for current state), optimism bias (overestimating positive outcomes)
 * and availability heuristic (overweighting recent examples).
 * Coordinator must not treat "it will stay normal" as settled without weighing low-probability high-impact alternatives.
 */
public final class NormalcyBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private NormalcyBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "normalcy bias: недооценка вероятности/масштаба резких изменений или катастроф («всё будет как обычно», «такого не было», «things will stay normal») без базовых рисков, tail-risk или исторических прецедентов вне недавнего окна");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта недооценивают риск резких изменений, полагаясь только на недавнюю «нормальность»");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "normalcy bias (possible signal, requires context): " + core;
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

    private static boolean acknowledges(String t) {
        return has(t,
                "normalcy bias", "предвзятость к нормальности",
                "black swan", "чёрный лебедь",
                "tail risk", "хвостовой риск",
                "base rate", "базовая вероятность",
                "historical precedent", "исторический прецедент",
                "regime change", "смена режима",
                "scenario planning", "сценарное планирование");
    }

    private static boolean naivePattern(String t) {
        boolean normalcy = has(t,
                "всё будет как обычно", "такого не было",
                "никогда такого не случалось", "things will stay normal",
                "this never happened before", "everything will continue as usual",
                "будет как всегда", "ничего не изменится",
                "нормальный ход событий", "business as usual",
                "резких изменений не будет");
        return normalcy && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
