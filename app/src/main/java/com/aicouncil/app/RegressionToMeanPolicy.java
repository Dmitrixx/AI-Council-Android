package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local regression-to-the-mean signals.
 * Flags when extreme observations (very high or low performance, scores, outcomes)
 * are treated as stable traits or predictive without noting that subsequent
 * measurements tend to be closer to the average. Hypotheses requiring context, not verdicts.
 * The coordinator must not average them or treat extremes as permanent.
 */
public final class RegressionToMeanPolicy {
    private static final int MAX_SIGNALS = 3;

    private RegressionToMeanPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, treatsExtremeAsPermanent(a) || treatsExtremeAsPermanent(b),
                "регрессия к среднему: крайний результат (лучший/худший) считается устойчивой характеристикой без учёта естественного возврата к среднему при повторном измерении");
        add(conflicts, initial, bothIgnoreRegression(a, b),
                "оба трактуют экстремальные наблюдения как стабильные и не упоминают регрессию к среднему или изменчивость");
        add(conflicts, initial, predictsContinuationOfExtreme(a) || predictsContinuationOfExtreme(b),
                "прогноз продолжения крайнего результата без поправки на регрессию к среднему");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "regression to the mean (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(42, core.length()));
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

    private static boolean accountsForRegression(String t) {
        return has(t,
                "регрессия к среднему", "regression to the mean", "вернётся к среднему",
                "естественная изменчивость", "noise", "шум измерения", "random variation",
                "не устойчив", "temporary", "временный эффект");
    }

    private static boolean treatsExtremeAsPermanent(String t) {
        boolean extreme = has(t,
                "лучший результат", "худший результат", "рекорд", "провал", "outstanding performance",
                "worst performance", "top score", "bottom score", "extreme result",
                "намного выше среднего", "намного ниже среднего");
        boolean permanent = has(t, "всегда будет", "постоянно", "устойчивая черта", "his true level",
                "её уровень", "будет продолжать", "стабильно");
        return extreme && permanent && !accountsForRegression(t);
    }

    private static boolean bothIgnoreRegression(String a, String b) {
        return (treatsExtremeAsPermanent(a) || predictsContinuationOfExtreme(a))
                && (treatsExtremeAsPermanent(b) || predictsContinuationOfExtreme(b))
                && !accountsForRegression(a) && !accountsForRegression(b);
    }

    private static boolean predictsContinuationOfExtreme(String t) {
        boolean extreme = has(t, "этот результат", "такой успех", "такой провал", "this performance",
                "that outcome");
        boolean continuePred = has(t, "продолжит", "будет таким же", "ожидаем такой же", "will continue",
                "expect the same", "persist");
        return extreme && continuePred && !accountsForRegression(t);
    }
}
