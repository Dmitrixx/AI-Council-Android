package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local argument-from-personal-incredulity policy.
 * Detects a conclusion that something is false, impossible, or not true merely because the speaker cannot imagine or understand how it could be true
 * ("I can't see how that works, so it must be wrong", "if I can't understand it, it can't be real").
 * Naming the error, or saying inability to imagine is not evidence, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class PersonalIncredulityPolicy {
    private static final int MAX_SIGNALS = 2;

    private PersonalIncredulityPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "argument from personal incredulity: вывод из того, что я не могу представить/понять, значит это ложь");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта отвергают тезис, потому что не могут себе представить");
        add(conflicts, initial, hitA ^ hitB,
                "personal incredulity: один эксперт считает тезис ложным, потому что сам не понимает как это может быть");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "personal incredulity (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "я не могу представить как это может быть правдой",
                "раз я не понимаю — значит ложь",
                "не могу себе представить, поэтому это невозможно",
                "если бы это было правдой, я бы понял",
                "i can't imagine how that could be true",
                "since i don't understand it it must be false",
                "if i can't see how it works then it doesn't",
                "personal incredulity",
                "argument from incredulity",
                "because i cannot comprehend it it is wrong",
                "my inability to understand proves it false");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "argument from incredulity",
                "personal incredulity",
                "неспособность понять не доказательство",
                "я не понимаю не значит что ложь",
                "inability to imagine is not evidence",
                "not understanding does not make it false",
                "не выводи ложность из своего непонимания",
                "my confusion does not refute the claim");
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
