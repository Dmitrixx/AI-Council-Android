package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local causal oversimplification / single-cause fallacy policy.
 * Flags a verdict that attributes a complex multi-factor outcome to one simple cause
 * without independent justification for exclusivity. Naming the error is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class CausalOversimplificationPolicy {
    private static final int MAX_SIGNALS = 2;

    private CausalOversimplificationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "causal oversimplification: сложный исход закрыт одной причиной без основания исключительности");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта сводят сложный результат к одной простой причине");
        add(conflicts, initial, hitA ^ hitB,
                "causal oversimplification: один эксперт закрыл вывод единственной причиной");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "causal oversimplification (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "это всё из-за одной причины",
                "единственная причина поэтому верно",
                "всё сводится к одному фактору следовательно так",
                "нет других причин это она",
                "single cause explains the whole outcome so it is true",
                "the only reason is therefore confirmed",
                "it all comes down to this one factor therefore decided",
                "complex result closed by one simple cause without alternatives");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "causal oversimplification",
                "сложный исход не от одной причины",
                "множественные факторы не сводятся к одной",
                "single cause is not the full story",
                "multiple contributing factors must be considered",
                "oversimplification of causality is not proof");
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
