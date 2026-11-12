package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local fallacy-fallacy / argument-from-fallacy policy.
 * Detects conclusions that a claim is false, or that the opposite is true,
 * merely because an argument for it was flawed, fallacious or badly worded.
 * Naming the error, or saying a bad argument does not settle the claim, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class FallacyFallacyPolicy {
    private static final int MAX_SIGNALS = 2;

    private FallacyFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "fallacy fallacy: ошибка в доводе объявлена опровержением тезиса");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта считают плохой аргумент доказательством обратного");
        add(conflicts, initial, hitA ^ hitB,
                "fallacy fallacy: один эксперт закрывает тезис из-за ошибки в доводе");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "fallacy fallacy (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "раз в аргументе ошибка значит вывод ложен",
                "раз аргумент ошибочен значит неверно",
                "ошибка в рассуждении доказывает обратное",
                "содержит ошибку поэтому тезис ложен",
                "логическая ошибка значит вывод неверен",
                "это софизм поэтому неправда",
                "плохой довод значит тезис ложен",
                "the argument is fallacious so the conclusion is false",
                "because the argument is wrong the claim is false",
                "fallacious therefore false",
                "bad argument so the opposite is true",
                "contains a fallacy therefore the conclusion is false",
                "the reasoning is invalid so the claim is untrue",
                "since the argument fails the claim is false");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "fallacy fallacy",
                "argument from fallacy",
                "ошибка в аргументе не делает вывод ложным",
                "ошибка в аргументе не доказывает обратный тезис",
                "a fallacy does not prove the conclusion false",
                "bad argument does not prove the opposite",
                "софизм не опровергает тезис",
                "a flawed argument does not settle the claim");
    }

    private static boolean has(String text, String... phrases) {
        if (text == null) return false;
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
