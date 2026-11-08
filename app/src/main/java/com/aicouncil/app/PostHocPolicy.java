package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local post hoc ergo propter hoc / false cause signals.
 * When temporal sequence or correlation is treated as causation without independent support
 * ("after X, therefore because of X", "since Y followed Z, Z caused Y").
 * This is a hypothesis, not evidence. Distinct from established causal claims with mechanisms,
 * controls, or independent evidence.
 * Coordinator must not treat post hoc as automatically invalidating the claim, nor its absence as proof of causation.
 */
public final class PostHocPolicy {
    private static final int MAX_SIGNALS = 2;

    private PostHocPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "post hoc / false cause: временная последовательность или корреляция принимается за причинность без оснований");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта используют post hoc: после этого — значит по причине этого");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "post hoc (possible signal, requires context): " + core;
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
                "post hoc",
                "false cause",
                "correlation is not causation",
                "post hoc ergo propter hoc",
                "после этого не значит по причине этого",
                "корреляция не есть причинность");
    }

    private static boolean naivePattern(String t) {
        boolean posthoc = has(t,
                "после этого значит по причине этого",
                "с тех пор как, значит из-за",
                "последовало за, поэтому вызвано",
                "after this, therefore because of this",
                "since it followed, it was caused by",
                "post hoc ergo propter hoc",
                "because it happened after",
                "following X caused Y",
                "после этого — значит из-за этого");
        boolean noAck = !has(t,
                "не причина",
                "корреляция не причинность",
                "not causation",
                "independent evidence of cause",
                "mechanism shows");
        return posthoc && noAck && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
