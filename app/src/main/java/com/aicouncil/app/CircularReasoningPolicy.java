package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local circular reasoning / begging the question signals.
 * The conclusion is assumed in the premises or the argument goes in a circle
 * ("X is true because X is true", "it's obvious because everyone knows", restating the claim as evidence).
 * This is a hypothesis, not evidence. Distinct from tautology that is explicitly marked as such,
 * or arguments that provide independent support.
 * Coordinator must not treat circularity as automatically invalidating the claim, nor its absence as proof of validity.
 */
public final class CircularReasoningPolicy {
    private static final int MAX_SIGNALS = 2;

    private CircularReasoningPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "circular reasoning: вывод предполагается в посылках или аргумент ходит по кругу");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта используют круговое рассуждение: утверждение служит своим собственным доказательством");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "circular reasoning (possible signal, requires context): " + core;
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
                "circular reasoning",
                "begging the question",
                "круговое рассуждение",
                "порочный круг",
                "this is circular",
                "I am assuming what needs proof");
    }

    private static boolean naivePattern(String t) {
        boolean circular = has(t,
                "потому что так и есть",
                "это очевидно, потому что все знают",
                "верно, потому что верно",
                "само собой разумеется, что",
                "because it is true",
                "it's true because it's true",
                "everyone knows that",
                "it is obvious that",
                "begging the question",
                "circular argument",
                "порочный круг",
                "доказательство через само утверждение");
        boolean noAck = !has(t,
                "это не круговое",
                "независимое основание",
                "not circular",
                "independent evidence");
        return circular && noAck && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
