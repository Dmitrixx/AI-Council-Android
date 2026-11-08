package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local straw-man signals.
 * Attacking a distorted, absurd or "so you just want…" version of the other side
 * without first stating their strongest argument is a hypothesis, not evidence.
 * Distinct from reactive devaluation (rejecting because of the source) and belief bias:
 * here the target claim itself has been replaced.
 * The coordinator must not treat a caricature as the position that was refuted.
 */
public final class StrawManPolicy {
    private static final int MAX_SIGNALS = 2;

    private StrawManPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "соломенное чучело: бьют искажённую версию чужой позиции («они якобы», «так вы просто хотите сдаться») без сильнейшей формулировки тезиса");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта опровергают карикатуру, а не лучшую версию противоположного аргумента");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "straw man (possible signal, requires context): " + core;
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
                "steelman", "steel-man",
                "сильнейшая версия", "сильнейшую версию",
                "лучший аргумент против", "их лучший аргумент",
                "не искажая", "без искажения",
                "стальной человек");
    }

    private static boolean naivePattern(String t) {
        boolean caricature = has(t,
                "они якобы", "он якобы считает",
                "на самом деле они хотят",
                "их позиция сводится к абсурду",
                "подменяют тезис", "подмена тезиса",
                "так вы просто хотите", "так что вы предлагаете просто",
                "so you just want", "so you're saying we should just",
                "nobody in their right mind", "никто в здравом уме",
                "they literally want to destroy",
                "искажённая версия их", "карикатура на позицию");
        return caricature && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
