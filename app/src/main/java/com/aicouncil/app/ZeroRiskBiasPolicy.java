package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local zero-risk bias signals.
 * Flags when a conclusion demands or claims "zero risk", "completely safe",
 * "no chance of failure" or absolute elimination of a small risk as if
 * absolute safety is achievable or preferable, without comparing residual
 * risks, expected value, or the cost of chasing zero.
 * Hypotheses requiring context, not automatic verdicts. Coordinator must not average them away.
 */
public final class ZeroRiskBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private ZeroRiskBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, demandsZeroRisk(a) || demandsZeroRisk(b),
                "zero-risk bias: требование или заявление абсолютной безопасности / нулевого риска без сравнения остаточных рисков и ожидаемой ценности");
        add(conflicts, initial, bothDemand(a, b),
                "оба эксперта требуют нулевого риска и не сравнивают ожидаемую ценность или остаточный риск");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "zero risk bias (возможный сигнал, требует контекста): " + core;
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

    private static boolean acknowledgesTradeoff(String t) {
        return has(t,
                "остаточный риск", "residual risk", "expected value", "ожидаемая ценность",
                "нулевой риск невозможен", "zero risk impossible", "сравнивай риски",
                "не абсолютная безопасность", "trade-off", "стоимость нулевого риска");
    }

    private static boolean zeroRiskClaim(String t) {
        return has(t,
                "нулевой риск", "zero risk", "полностью безопасно", "абсолютно безопасно",
                "никакого риска", "no risk", "completely safe", "100% безопасно",
                "исключить любой риск", "eliminate all risk", "нулевой шанс");
    }

    private static boolean demandsZeroRisk(String t) {
        return zeroRiskClaim(t) && !acknowledgesTradeoff(t);
    }

    private static boolean bothDemand(String a, String b) {
        return demandsZeroRisk(a) && demandsZeroRisk(b);
    }
}
