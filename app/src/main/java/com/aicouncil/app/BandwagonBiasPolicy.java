package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local bandwagon / groupthink signals.
 * Preferring a conclusion because "everyone agrees", "most people think", "popular opinion",
 * "consensus is clear" without independent evidence evaluation or steelmanning dissent
 * is a hypothesis, not evidence. Distinct from social proof (observing others' behavior)
 * and authority bias (deferring to experts/status).
 * Coordinator must not treat majority view as settled without independent reasons and dissent analysis.
 */
public final class BandwagonBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private BandwagonBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "bandwagon / groupthink: вывод на основе «все согласны», «большинство так думает», «популярное мнение» без независимой оценки доказательств или анализа диссента");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта опираются на большинство/консенсус без независимой проверки доказательств");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "bandwagon bias (possible signal, requires context): " + core;
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
                "bandwagon", "groupthink", "эффект бандвагона",
                "majority is not proof", "большинство не доказательство",
                "independent evaluation", "независимая оценка",
                "steelman dissent", "разбери диссент",
                "popular ≠ true", "популярность не истина");
    }

    private static boolean naivePattern(String t) {
        boolean bandwagon = has(t,
                "все согласны", "большинство так думает",
                "популярное мнение", "консенсус ясен",
                "everyone agrees", "most people think",
                "popular opinion", "the consensus is",
                "все эксперты согласны", "общепринятое мнение",
                "никто не спорит", "everybody knows");
        return bandwagon && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
