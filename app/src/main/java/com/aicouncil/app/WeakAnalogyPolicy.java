package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local weak analogy / false analogy policy.
 * Detects a conclusion that something is true or similar merely because of a superficial analogy
 * without independent justification for the relevant similarity ("it's like X, so it must be Y").
 * Naming the error, or saying the analogy is weak, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class WeakAnalogyPolicy {
    private static final int MAX_SIGNALS = 2;

    private WeakAnalogyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "weak analogy: вывод из поверхностной аналогии без независимого основания для релевантного сходства");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта опираются на слабую аналогию как доказательство");
        add(conflicts, initial, hitA ^ hitB,
                "weak analogy: один эксперт считает тезис верным потому что «это как X» без основания");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "weak analogy (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "это как",
                "точно как",
                "аналогично",
                "поэтому так же",
                "it's like",
                "just like",
                "analogous to",
                "therefore the same",
                "weak analogy",
                "false analogy",
                "сравнение не доказательство");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "weak analogy",
                "false analogy",
                "слабая аналогия",
                "аналогия не доказательство",
                "analogy is not evidence",
                "сравнение не равно тождеству",
                "не выводи из аналогии без основания");
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
