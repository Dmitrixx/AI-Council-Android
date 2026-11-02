package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local hindsight bias signals.
 * Flags when, after an outcome is known, experts treat it as obvious, predictable,
 * or something they "knew all along" without acknowledging prior uncertainty,
 * alternative scenarios, or the actual information available at the time.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class HindsightBiasPolicy {
    private static final int MAX_SIGNALS = 3;

    private HindsightBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, knewItAllAlong(a) || knewItAllAlong(b),
                "hindsight bias: «я знал / было очевидно» после исхода без признания предыдущей неопределённости");
        add(conflicts, initial, bothClaimPredictable(a, b),
                "общий hindsight: оба эксперта считают исход предсказуемым задним числом без альтернатив");
        add(conflicts, initial, ignoresPriorUncertainty(a) || ignoresPriorUncertainty(b),
                "игнорирование предыдущей неопределённости: исход объявлен очевидным без упоминания альтернативных сценариев");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "hindsight bias (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean knewItAllAlong(String t) {
        boolean knew = has(t, "я знал", "было очевидно", "все знали", "предсказуемо",
                "i knew", "it was obvious", "everyone knew", "predictable",
                "знал заранее", "очевидно задним числом", "hindsight");
        boolean outcome = has(t, "исход", "результат", "произошло", "случилось",
                "outcome", "result", "happened", "after the fact");
        boolean uncertainty = has(t, "неопределённость", "альтернатива", "могло быть иначе",
                "uncertainty", "alternative", "could have been", "prior information");
        return knew && outcome && !uncertainty;
    }

    private static boolean bothClaimPredictable(String a, String b) {
        boolean aPred = has(a, "предсказуемо", "очевидно", "знал", "predictable", "obvious");
        boolean bPred = has(b, "предсказуемо", "очевидно", "знал", "predictable", "obvious");
        return aPred && bPred;
    }

    private static boolean ignoresPriorUncertainty(String t) {
        boolean obvious = has(t, "очевидно", "предсказуемо", "заранее было ясно", "obvious in retrospect");
        boolean noAlt = !has(t, "альтернатив", "могло быть иначе", "неопределённость была",
                "alternative scenario", "prior uncertainty", "could have gone differently");
        return obvious && noAlt;
    }
}
