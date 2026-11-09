package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local argument-to-moderation / middle-ground fallacy signals.
 * Declaring the midpoint or the average of two positions to be the truth,
 * without a new observation, is a hypothesis — not a resolution.
 * Naming the fallacy, or refusing to average, is not itself the error.
 * Coordinator must not treat a compromise as evidence.
 */
public final class MiddleGroundFallacyPolicy {
    private static final int MAX_SIGNALS = 2;

    private MiddleGroundFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, ignores(a) || ignores(b),
                "ошибка середины: компромисс или среднее двух позиций объявляется истиной без нового наблюдения");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта считают середину доказательством, а не нерешённым конфликтом");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "middle ground (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }

    private static boolean naivePattern(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "истина посередине",
                "правда посередине",
                "правда где-то посередине",
                "истина где-то посередине",
                "истина лежит посередине",
                "истина лежит между",
                "правильный ответ — среднее",
                "правильный ответ - среднее",
                "ответ — это среднее",
                "усредним позиции и получим истину",
                "компромисс и есть истина",
                "золотая середина и есть ответ",
                "золотая середина и есть истина",
                "the truth is in the middle",
                "the truth lies in the middle",
                "the truth lies somewhere in the middle",
                "the truth is somewhere in the middle",
                "split the difference and that is correct",
                "split the difference and that settles it",
                "the answer is the average of the two",
                "the answer is the average of both",
                "meeting in the middle makes it true",
                "the compromise is the truth");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "середина не доказательство",
                "середина не истина",
                "middle ground fallacy",
                "argument to moderation",
                "false compromise",
                "averaging is not evidence",
                "усреднение не доказательство",
                "компромисс не делает",
                "золотая середина не доказательство",
                "не усредняй",
                "do not average");
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
