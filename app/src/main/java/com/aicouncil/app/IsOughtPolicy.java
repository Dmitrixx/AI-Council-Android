package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local is-ought / Hume's guillotine policy, including the moralistic reverse.
 * Detects a conclusion that a bare fact yields a duty, or that a wish yields a fact
 * ("it is, therefore it should be", "it ought to be, therefore it is").
 * Naming the gap, or saying a fact does not make a norm, is not the error.
 * Appeal-to-nature stays a separate check. Signals remain hypotheses that require context.
 */
public final class IsOughtPolicy {
    private static final int MAX_SIGNALS = 2;

    private IsOughtPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "is-ought: вывод нормы из голого факта или факта из одного желания");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта переходят между «есть» и «должно» без отдельной посылки");
        add(conflicts, initial, hitA ^ hitB,
                "is-ought: один эксперт выводит норму из факта или факт из нормы");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "is-ought (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "раз так есть, значит так и должно",
                "так устроено, значит так и надо",
                "это факт, поэтому это правильно",
                "из того что есть следует что должно",
                "раз так должно быть, значит так и есть",
                "желаемое и есть действительное",
                "because it is the case it ought to be",
                "it is, therefore it should be",
                "what is must be what ought to be",
                "it ought to be, therefore it is",
                "is implies ought",
                "ought implies is");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "is-ought",
                "гильотина юма",
                "hume's guillotine",
                "из есть не следует должно",
                "факт не делает норму",
                "ought does not follow from is",
                "is does not imply ought",
                "не выводи долг из факта",
                "a wish is not a fact");
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
