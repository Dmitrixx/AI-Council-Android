package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local law-of-small-numbers signals.
 * Flags a verdict that treats a small sample or handful of observations as highly
 * representative of the underlying population or rate, without naming the sample size,
 * expected variance, confidence interval, or a larger reference class.
 * Naming the gap is not itself an error.
 * The coordinator cannot close a frequency or generalization solely on a small sample.
 */
public final class LawOfSmallNumbersPolicy {
    private static final int MAX_SIGNALS = 2;

    private LawOfSmallNumbersPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesOnSmallSample(a) || closesOnSmallSample(b),
                "law of small numbers: вердикт закрывает частоту или обобщение на малой выборке без учёта дисперсии, доверительного интервала или большего референс-класса");
        add(conflicts, initial, closesOnSmallSample(a) && closesOnSmallSample(b),
                "оба эксперта закрывают на малой выборке без учёта вариативности");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "law of small numbers (possible signal, requires context): " + core;
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

    private static boolean accountsForVariance(String t) {
        return has(t,
                "sample size", "размер выборки", "n=", "variance", "дисперсия",
                "confidence interval", "доверительный интервал", "reference class",
                "референс-класс", "base rate", "базовая частота",
                "small sample", "малая выборка", "expected fluctuation");
    }

    private static boolean closesOnSmallSample(String t) {
        boolean claim = has(t,
                "в трёх случаях", "in three cases", "пара примеров",
                "несколько наблюдений", "a few examples", "small sample shows",
                "в моёй выборке", "my sample of", "на нескольких примерах");
        boolean conclude = has(t, "следовательно", "рекомендую", "therefore", "i recommend", "стоит выбрать", "это доказывает", "clearly shows");
        return claim && conclude && !accountsForVariance(t);
    }
}
