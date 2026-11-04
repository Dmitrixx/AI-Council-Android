package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local base-rate neglect signals.
 * Flags when a conclusion relies on a vivid individual case, similarity,
 * or small unrepresentative sample while ignoring or downplaying the
 * population base rate / prior frequency of the class.
 * Hypotheses requiring context, not automatic verdicts. Coordinator must not average them away.
 */
public final class BaseRateNeglectPolicy {
    private static final int MAX_SIGNALS = 2;

    private BaseRateNeglectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, neglectsBaseRate(a) || neglectsBaseRate(b),
                "игнорирование базовой частоты: вывод из яркого случая или сходства без учёта prior / base rate популяции");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта опираются на репрезентативность, не называя базовую частоту класса");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "base rate neglect (возможный сигнал, требует контекста): " + core;
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

    private static boolean acknowledgesBaseRate(String t) {
        return has(t,
                "base rate", "базовая частота", "prior probability", "в популяции",
                "обычно встречается", "в среднем", "reference class", "частота класса",
                "не забывай базовую", "ignore base rate");
    }

    private static boolean neglectsBaseRate(String t) {
        boolean vividOrSimilar = has(t,
                "похож на", "напоминает", "типичный пример", "как в том случае",
                "resembles", "similar to the case", "vivid example", "looks like",
                "яркий пример", "один случай доказывает");
        boolean conclude = has(t, "значит", "следовательно", "рекомендую", "вероятность высока",
                "определённо", "therefore", "likely", "probably");
        return vividOrSimilar && conclude && !acknowledgesBaseRate(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return neglectsBaseRate(a) && neglectsBaseRate(b);
    }
}
