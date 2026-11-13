package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local mechanism gate.
 * A verdict that gives a cause ("because") and closes the question without an intermediate
 * step, a conditional, or a failure mode is a sketch, not a mechanism.
 * Naming that gap is not itself an error. Signals stay hypotheses.
 */
public final class MissingMechanismPolicy {
    private static final int MAX_SIGNALS = 2;

    private MissingMechanismPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = shallow(answerA);
        boolean hitB = shallow(answerB);
        add(conflicts, initial, hitA || hitB,
                "missing mechanism: причинная рекомендация без промежуточного шага, условия или режима отказа");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают вывод причинной связкой без механизма");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "missing mechanism (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean shallow(String text) {
        String lower = lower(text);
        if (lower.isEmpty() || acknowledges(lower)) return false;
        return closes(lower) && causal(lower) && !hasMechanism(lower);
    }

    private static boolean closes(String t) {
        return has(t,
                "вердикт",
                "вопрос закрыт",
                "закрываю вопрос",
                "рекомендую",
                "итого:",
                "итог:",
                "therefore we should",
                "we should conclude",
                "final answer",
                "the question is closed");
    }

    private static boolean causal(String t) {
        return has(t, "потому что", "поэтому", "так как", "because", "due to", "значит,");
    }

    private static boolean hasMechanism(String t) {
        boolean stepped = (t.contains("1.") && t.contains("2.")) || (t.contains("1)") && t.contains("2)"));
        return stepped || has(t,
                "если ",
                "if ",
                "шаг ",
                "step ",
                "режим отказа",
                "failure mode",
                "промежуточ",
                "intermediate");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "missing mechanism",
                "промежуточный шаг не назван",
                "без промежуточного шага",
                "high-level sketch is not a mechanism",
                "причина без механизма");
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
