package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local Knightian uncertainty signals.
 * Flags a verdict that treats a decision under genuine uncertainty (unknown probabilities,
 * unmodeled risks, "we don't know what we don't know") as if it were a risk problem
 * with known or easily estimated probabilities, without stating that the situation
 * is under uncertainty rather than risk, or without considering robust / maximin /
 * scenario-based approaches.
 * Naming the distinction is not itself an error.
 * The coordinator cannot close by assigning precise probabilities to unknown unknowns.
 */
public final class KnightianUncertaintyPolicy {
    private static final int MAX_SIGNALS = 2;

    private KnightianUncertaintyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, treatsAsRisk(a) || treatsAsRisk(b),
                "Knightian uncertainty: вердикт закрывает выбор под неизвестными вероятностями как будто это риск с известными вероятностями, без признания неопределённости или робастных альтернатив");
        add(conflicts, initial, treatsAsRisk(a) && treatsAsRisk(b),
                "оба эксперта считают вероятности известными в ситуации неопределённости");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "Knightian uncertainty (possible signal, requires context): " + core;
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
                "knightian", "найтовская неопределённость", "uncertainty not risk",
                "неизвестные неизвестные", "unknown unknowns", "we don't know the probabilities",
                "вероятности неизвестны", "robust decision", "maximin", "сценарный подход",
                "не риск а неопределённость");
    }

    private static boolean treatsAsRisk(String t) {
        boolean precise = has(t,
                "вероятность равна", "probability is", "шанс равен",
                "expected value", "ожидаемая полезность", "по расчёту",
                "модель даёт вероятность", "we estimate the chance at");
        boolean unknown = has(t,
                "неизвестно", "uncertain", "не знаем что", "black swan",
                "чёрный лебедь", "unforeseen", "непредвиденное", "novel situation",
                "новая ситуация", "no historical data", "нет исторических данных");
        return precise && unknown && !acknowledges(t);
    }
}
