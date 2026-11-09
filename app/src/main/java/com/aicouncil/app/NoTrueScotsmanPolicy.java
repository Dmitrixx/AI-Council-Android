package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local no-true-Scotsman / purity-redefinition signals.
 * After a counterexample appears, protecting a generalization by
 * narrowing the category ("no true X", "настоящий X так не делает")
 * is a hypothesis, not independent evidence.
 * Naming the fallacy is not itself the error.
 * The coordinator must not close a verdict by redefining membership.
 */
public final class NoTrueScotsmanPolicy {
    private static final int MAX_SIGNALS = 2;

    private NoTrueScotsmanPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, ignores(a) || ignores(b),
                "no true scotsman: обобщение защищается переопределением категории после контрпримера");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта сужают категорию вместо того, чтобы пересмотреть обобщение");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "no true scotsman (possible signal, requires context): " + core;
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
                "настоящий x так не",
                "настоящий не стал бы",
                "настоящий так не поступает",
                "настоящий так не делает",
                "настоящий специалист так не",
                "настоящий эксперт так не",
                "это не настоящий",
                "это не был настоящий",
                "он не был настоящим",
                "она не была настоящей",
                "не настоящий, если",
                "не считается настоящим",
                "тогда это не настоящий",
                "no true",
                "no real",
                "not a true",
                "not a real",
                "no genuine",
                "not a genuine",
                "no proper",
                "not a proper",
                "no true scotsman",
                "that is not a real",
                "that is not a true",
                "he was not a true",
                "she was not a true",
                "they were not true",
                "if they did that they were not real",
                "real ones would never",
                "true ones would never",
                "a real one would never",
                "a true one would never");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "no true scotsman",
                "no-true-scotsman",
                "no true scotsman fallacy",
                "purity redefinition",
                "переопределение категории",
                "настоящий не аргумент",
                "сужение категории не доказательство",
                "переопределение после контрпримера",
                "не сужай категорию",
                "do not redefine membership",
                "redefining the category is not evidence");
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
