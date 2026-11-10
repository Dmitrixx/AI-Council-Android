package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local tu quoque policy.
 * Detects dismissing an argument because the speaker is hypocritical or does not practice what they preach
 * ("ты сам так делаешь", "you do it too", "tu quoque").
 * Naming the error is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class TuQuoquePolicy {
    private static final int MAX_SIGNALS = 2;

    private TuQuoquePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "tu quoque: отвержение из-за лицемерия оппонента");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта используют «ты сам такой»");
        add(conflicts, initial, hitA ^ hitB,
                "tu quoque: один эксперт указывает на лицемерие");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "tu quoque (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "ты сам так делаешь",
                "а сам-то",
                "посмотри на себя",
                "ты тоже виноват",
                "you do it too",
                "tu quoque",
                "what about you",
                "you are no better",
                "hypocrite so your argument is invalid");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "tu quoque",
                "это не отвечает на аргумент",
                "лицемерие не опровергает тезис",
                "whataboutism is not a refutation");
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
