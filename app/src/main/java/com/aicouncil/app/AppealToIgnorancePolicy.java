package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local appeal-to-ignorance signals.
 * Flags when a conclusion rests on "no one has proven otherwise" or
 * "absence of evidence means the opposite is true" without positive support
 * or explicit acknowledgment of the asymmetry of burden of proof.
 * Hypotheses requiring context, not automatic verdicts. Coordinator must not average them away.
 */
public final class AppealToIgnorancePolicy {
    private static final int MAX_SIGNALS = 2;

    private AppealToIgnorancePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, appealWithoutSupport(a) || appealWithoutSupport(b),
                "отсутствие доказательств не доказательство отсутствия: вывод из «никто не опроверг» без позитивного основания или явного бремени доказательства");
        add(conflicts, initial, bothAppeal(a, b),
                "оба эксперта закрывают вывод тем, что оппонент не доказал обратное");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "appeal to ignorance (возможный сигнал, требует контекста): " + core;
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

    private static boolean acknowledgesBurden(String t) {
        return has(t,
                "бремя доказательства", "burden of proof", "absence of evidence is not evidence of absence",
                "отсутствие доказательств не доказательство", "не доказано ≠ доказано отсутствие",
                "не знаю ≠ не существует", "unknown does not imply false");
    }

    private static boolean appealWithoutSupport(String t) {
        boolean appeal = has(t,
                "никто не доказал", "не опровергнуто", "no one has proven",
                "has not been disproven", "нет доказательств обратного", "не найдено доказательств",
                "отсутствие данных значит", "раз не доказано обратное");
        boolean conclude = has(t, "значит", "следовательно", "рекомендую", "безопасно", "вредно", "true", "false", "существует", "не существует", "therefore");
        return appeal && conclude && !acknowledgesBurden(t);
    }

    private static boolean bothAppeal(String a, String b) {
        return appealWithoutSupport(a) && appealWithoutSupport(b);
    }
}
