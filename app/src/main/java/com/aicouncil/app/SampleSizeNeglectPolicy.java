package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local sample-size neglect / law of small numbers signals.
 * Flags a verdict that treats a small number of observations, a short streak, or a
 * tiny sample as representative or decisive without noting sampling variance,
 * regression to the mean, the need for a larger base rate, or that small samples
 * fluctuate more.
 * Naming the gap is not itself an error.
 * The coordinator cannot close on the small sample alone.
 */
public final class SampleSizeNeglectPolicy {
    private static final int MAX_SIGNALS = 2;

    private SampleSizeNeglectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, smallSampleDecisive(a) || smallSampleDecisive(b),
                "sample size neglect: вердикт закрывает выбор на малой выборке, короткой серии или нескольких наблюдениях без упоминания варианса, регрессии к среднему или необходимости большей базы");
        add(conflicts, initial, bothIgnoreSampleSize(a, b),
                "оба эксперта закрывают на малой выборке без независимой проверки размера выборки или варианса");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "sample size neglect (possible signal, requires context): " + core;
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
                "small sample", "малая выборка", "закон малых чисел",
                "sampling variance", "варианс выборки",
                "regression to the mean", "регрессия к среднему",
                "need larger sample", "нужна большая выборка",
                "small numbers fluctuate", "малые числа колеблются");
    }

    private static boolean smallSampleDecisive(String t) {
        boolean small = has(t,
                "всего три", "только два", "несколько случаев",
                "only three", "just two", "a few cases",
                "короткая серия", "short streak", "tiny sample",
                "маленькая выборка", "n=5", "n = 4");
        boolean decisive = has(t,
                "значит точно", "доказывает", "ясно что",
                "proves that", "shows clearly", "therefore",
                "надо делать", "recommend", "выбираем");
        return small && decisive && !acknowledges(t);
    }

    private static boolean bothIgnoreSampleSize(String a, String b) {
        return smallSampleDecisive(a) && smallSampleDecisive(b);
    }
}
