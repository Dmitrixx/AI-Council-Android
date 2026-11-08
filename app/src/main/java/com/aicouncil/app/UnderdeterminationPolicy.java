package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local underdetermination signals (Duhem–Quine).
 * Treating one story as the only mechanism compatible with the observations
 * ("the only possible explanation", "no rival mechanism fits", "the data prove
 * only this") without noting that other mechanisms can fit the same evidence
 * is a hypothesis, not evidence.
 * Distinct from false dilemma (a binary menu of actions), appeal to ignorance
 * (absence of disproof) and narrative fallacy (a tidy story): here the claim is
 * that the evidence uniquely identifies one cause.
 * Coordinator must not treat "this is the only explanation" as settled without
 * a rival that the same observations would also allow.
 */
public final class UnderdeterminationPolicy {
    private static final int MAX_SIGNALS = 2;

    private UnderdeterminationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "underdetermination: считает одно объяснение единственно совместимым с данными, не проверяя соперничающие механизмы");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта закрывают альтернативные механизмы, хотя те же наблюдения могут им соответствовать");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "underdetermination (possible signal, requires context): " + core;
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
                "underdetermination", "недоопредел",
                "альтернативное объяснение", "альтернативные объяснения",
                "соперничающий механизм", "соперничающие механизмы",
                "другой механизм совместим", "другие механизмы совместимы",
                "данные совместимы и с", "те же наблюдения совместимы",
                "rival explanation", "rival mechanism",
                "equally compatible", "несколько механизмов");
    }

    private static boolean naivePattern(String t) {
        boolean unique = has(t,
                "единственное возможное объяснение",
                "единственный возможный механизм",
                "другого объяснения нет",
                "альтернативных механизмов нет",
                "нет другого объяснения",
                "no other explanation",
                "the only possible explanation",
                "only possible mechanism",
                "данные совместимы только с этим",
                "наблюдения доказывают только это",
                "это единственный механизм, который");
        return unique && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
