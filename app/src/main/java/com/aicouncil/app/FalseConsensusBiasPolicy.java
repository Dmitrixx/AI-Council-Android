package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local false-consensus bias signals.
 * Flags when a conclusion rests on "everyone thinks like me", "people like me would agree",
 * or projecting one's own view onto the population without independent data on actual distribution
 * or acknowledgment that one's own belief is not representative.
 * Hypotheses requiring context, not automatic verdicts. Coordinator must not average them away.
 */
public final class FalseConsensusBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private FalseConsensusBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresBase(a) || ignoresBase(b),
                "false consensus: проекция своего мнения на «все/большинство» без данных о реальном распределении");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта проецируют собственную позицию на популяцию и не называют реальные частоты");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "false consensus bias (возможный сигнал, требует контекста): " + core;
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

    private static boolean acknowledgesBase(String t) {
        return has(t,
                "не все так думают", "мое мнение не репрезентативно", "false consensus",
                "проекция", "реальное распределение", "опрос показывает", "данные о частоте",
                "не обязательно большинство", "my view is not the base rate", "survey data",
                "не проецирую");
    }

    private static boolean consensusClaim(String t) {
        return has(t,
                "все так думают", "все согласны", "большинство думает как я", "people like me",
                "все нормальные люди", "очевидно для всех", "everyone would agree",
                "any reasonable person", "все понимают", "так все считают", "common sense для всех");
    }

    private static boolean ignoresBase(String t) {
        return consensusClaim(t) && !acknowledgesBase(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignoresBase(a) && ignoresBase(b);
    }
}
