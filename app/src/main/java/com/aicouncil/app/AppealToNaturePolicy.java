package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local appeal-to-nature / naturalistic fallacy policy.
 * Detects a conclusion that something is good, safe, or preferable merely because it is "natural"
 * («если естественно — значит хорошо», «natural means better», «it is natural so it is safe»).
 * Naming the error, or saying natural does not imply good/safe, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class AppealToNaturePolicy {
    private static final int MAX_SIGNALS = 2;

    private AppealToNaturePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "appeal to nature: вывод из того, что это естественно / natural");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта опираются на естественность как доказательство");
        add(conflicts, initial, hitA ^ hitB,
                "appeal to nature: один эксперт выводит тезис из естественности");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "appeal to nature (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "если естественно — значит хорошо",
                "раз естественно, значит безопасно",
                "естественное лучше химии",
                "природа знает лучше",
                "так задумано природой",
                "natural means it is better",
                "because it is natural it is safe",
                "natural is always better",
                "if it is natural then it is good",
                "appeal to nature",
                "naturalistic fallacy");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "appeal to nature",
                "naturalistic fallacy",
                "естественно не значит хорошо",
                "естественность не доказательство",
                "natural does not mean good",
                "natural does not imply safe",
                "не выводи ценность из естественности",
                "is-ought gap");
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
