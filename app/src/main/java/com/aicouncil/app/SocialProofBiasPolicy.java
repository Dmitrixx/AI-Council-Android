package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local social-proof / bandwagon bias signals.
 * Flags when a conclusion rests on "everyone is doing it", "most people believe",
 * popularity, consensus of the crowd, or "it is widely accepted" without independent
 * evidence, mechanism, base rate of competence, or explicit acknowledgment that
 * popularity is not evidence of truth or quality.
 * Hypotheses requiring context, not automatic verdicts. Coordinator must not average them away.
 */
public final class SocialProofBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private SocialProofBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresIndependence(a) || ignoresIndependence(b),
                "social proof / bandwagon: вывод из популярности, «все так делают» или консенсуса толпы без независимых доказательств или механизма");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта опираются на социальное доказательство и не называют независимые основания");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "social proof bias (возможный сигнал, требует контекста): " + core;
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

    private static boolean acknowledgesIndependence(String t) {
        return has(t,
                "популярность не доказательство", "popularity is not evidence", "bandwagon",
                "все делают ≠ правильно", "консенсус толпы не аргумент", "независимое доказательство",
                "механизм", "base rate компетенции", "why it works", "evidence independent of popularity",
                "не потому что все так делают");
    }

    private static boolean socialClaim(String t) {
        return has(t,
                "все так делают", "все делают", "большинство считает", "most people",
                "everyone is doing", "widely accepted", "общепринят", "популярно",
                "все знают", "консенсус", "как у всех", "social proof", "bandwagon",
                "так принято", "норма", "все используют", "миллионы делают");
    }

    private static boolean ignoresIndependence(String t) {
        return socialClaim(t) && !acknowledgesIndependence(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignoresIndependence(a) && ignoresIndependence(b);
    }
}
