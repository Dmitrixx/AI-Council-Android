package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local ambiguity aversion signals.
 * Flags when experts prefer a known risk over an unknown one (or vice versa)
 * without explicitly comparing expected values, base rates, or information value.
 * Hypotheses requiring context, not verdicts.
 */
public final class AmbiguityAversionPolicy {
    private static final int MAX_SIGNALS = 3;

    private AmbiguityAversionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, prefersKnownOverUnknown(a) || prefersKnownOverUnknown(b),
                "неприятие неопределённости: предпочтение известного риска неизвестному без сравнения ожидаемой ценности или ценности информации");
        add(conflicts, initial, bothAvoidAmbiguity(a, b),
                "общий сигнал: оба эксперта избегают неопределённого варианта без количественного сравнения");
        add(conflicts, initial, ignoresValueOfInformation(a) || ignoresValueOfInformation(b),
                "игнорирование ценности информации: отказ от сбора данных, которые могли бы разрешить неоднозначность");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "ambiguity aversion (возможный сигнал, требует контекста): " + core;
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

    private static boolean prefersKnownOverUnknown(String t) {
        boolean known = has(t, "известный риск", "известная вероятность", "known risk", "known probability",
                "мы знаем шансы", "точно знаем");
        boolean unknown = has(t, "неизвестно", "неопределённо", "ambiguous", "unknown odds", "неясно сколько");
        boolean prefer = has(t, "лучше взять известный", "предпочту известный", "prefer the known",
                "лучше определённый риск");
        boolean comparison = has(t, "ожидаемая ценность", "expected value", "base rate", "ценность информации",
                "value of information", "сравнили");
        return known && unknown && prefer && !comparison;
    }

    private static boolean bothAvoidAmbiguity(String a, String b) {
        boolean aAvoid = has(a, "избегаем неопределённости", "не любим ambiguity", "prefer certainty");
        boolean bAvoid = has(b, "избегаем неопределённости", "не любим ambiguity", "prefer certainty");
        return aAvoid && bAvoid;
    }

    private static boolean ignoresValueOfInformation(String t) {
        boolean refuse = has(t, "не будем собирать данные", "не стоит проверять", "skip the pilot",
                "не нужно эксперимент");
        boolean ambiguous = has(t, "неясно", "неопределённо", "ambiguous", "unknown");
        boolean voi = has(t, "ценность информации", "value of information", "пилот", "эксперимент", "наблюдение");
        return refuse && ambiguous && !voi;
    }
}
