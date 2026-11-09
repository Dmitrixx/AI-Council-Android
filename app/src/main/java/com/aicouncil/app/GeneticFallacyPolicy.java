package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local genetic fallacy signals.
 * Dismissing or accepting an argument solely because of its origin, source, or history
 * rather than its merits («это плохо, потому что от X», «источник скомпрометирован — значит ложно»,
 * «genetic fallacy», «poisoning the well», «argument from origin»)
 * is a hypothesis, not independent evidence.
 * Naming the fallacy is not itself the error.
 * The coordinator must not close a verdict solely based on the origin of an idea.
 */
public final class GeneticFallacyPolicy {
    private static final int MAX_SIGNALS = 2;

    private GeneticFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, ignores(a) || ignores(b),
                "genetic fallacy: отвергает аргумент из-за его происхождения, а не содержания");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта опираются на происхождение идеи вместо её достоинств");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "genetic fallacy (possible signal, requires context): " + core;
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
                "потому что это от",
                "источник скомпрометирован",
                "раз от такого человека",
                "идея плохая из-за происхождения",
                "genetic fallacy",
                "poisoning the well",
                "argument from origin",
                "dismissed because of source",
                "false because it comes from");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "genetic fallacy",
                "ошибка происхождения",
                "не отвергай из-за источника",
                "origin is not evidence",
                "judge the argument not the source");
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
