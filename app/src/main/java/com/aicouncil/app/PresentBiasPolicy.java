package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local present-bias signals.
 * A nearer payoff or pain, without a horizon and a discount rate, is not a
 * reason to act or refuse. Choosing the immediate gain on both sides without
 * comparing the later outcome is not evidence. Hypotheses, not verdicts.
 */
public final class PresentBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private PresentBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, pullWithoutHorizon(a) || pullWithoutHorizon(b),
                "ближняя выгода или боль без горизонта и ставки дисконтирования");
        add(conflicts, initial, bothImmediate(a, b),
                "оба эксперта выбирают ближний выигрыш и не сравнивают поздний итог");
        add(conflicts, initial, fork(a, b),
                "развилка: ближняя выгода против сравнения на горизонте");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "present bias (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(36, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean pullWithoutHorizon(String text) {
        return hasPull(text) && !hasHorizon(text);
    }

    private static boolean bothImmediate(String a, String b) {
        return pullWithoutHorizon(a) && pullWithoutHorizon(b);
    }

    private static boolean fork(String a, String b) {
        return (pullWithoutHorizon(a) && hasLaterComparison(b))
                || (pullWithoutHorizon(b) && hasLaterComparison(a));
    }

    private static boolean hasPull(String text) {
        return contains(text,
                "сейчас важнее", "потом разбер", "ближайшая выгода", "ближайшей выгоды",
                "синица в руках", "квартальный бонус", "выгода сегодня",
                "не будем ждать отдачи", "не ждать отдачи", "деньги сейчас",
                "immediate payoff", "take the money now", "jam today",
                "short-term gain", "this quarter only", "present bias");
    }

    private static boolean hasHorizon(String text) {
        return contains(text,
                "ставка дисконт", "ставке дисконт", "discount rate",
                "горизонт", "time horizon", "чистая приведённая", "чистую приведённую",
                " npv", "npv ", "через 5 лет", "через пять лет", "на дистанции");
    }

    private static boolean hasLaterComparison(String text) {
        return hasHorizon(text) || contains(text,
                "поздний итог", "долгосрочн", "сравнить через год", "сравниваем через год");
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
