package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local exponential / compound growth bias signals.
 * Flags when a conclusion projects linear change onto a process that is exponential,
 * compounding, or multiplicative (e.g. "doubles every N years", population, interest,
 * viral spread, technology adoption) without acknowledging the rate, doubling time,
 * or that linear intuition fails, or treats a short linear stretch as the long-term path.
 * Hypotheses requiring context, not automatic verdicts. Coordinator must not average them away.
 */
public final class ExponentialGrowthBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private ExponentialGrowthBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresExponential(a) || ignoresExponential(b),
                "exponential growth bias: линейная проекция на экспоненциальный / составной процесс без учёта ставки или времени удвоения");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта используют линейную интуицию для составного роста и не называют скорость или удвоение");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "exponential growth bias (возможный сигнал, требует контекста): " + core;
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

    private static boolean acknowledgesRate(String t) {
        return has(t,
                "экспоненциальн", "exponential", "compound", "составн", "удвоени", "doubling",
                "rate of growth", "темп роста", "процент в год", "% per year", "compounding",
                "не линейно", "not linear", "мультипликатив");
    }

    private static boolean exponentialClaim(String t) {
        return has(t,
                "растёт линейно", "linear growth", "постоянный прирост", "constant increase",
                "каждый год на столько же", "same amount each year", "линейная проекция",
                "продолжит так же", "will continue at the same rate" , "straight line");
    }

    private static boolean ignoresExponential(String t) {
        return exponentialClaim(t) && !acknowledgesRate(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignoresExponential(a) && ignoresExponential(b);
    }
}
