package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local appeal-to-tradition policy.
 * Detects a conclusion that something is good, correct, or preferable merely because it is traditional,
 * longstanding, or "the way it has always been done"
 * («если так делали всегда — значит хорошо», «традиция доказывает», «we've always done it this way so it is right»).
 * Naming the error, or saying tradition is not an argument, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class AppealToTraditionPolicy {
    private static final int MAX_SIGNALS = 2;

    private AppealToTraditionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "appeal to tradition: вывод из того, что так делали всегда / traditional");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта опираются на традицию как доказательство");
        add(conflicts, initial, hitA ^ hitB,
                "appeal to tradition: один эксперт выводит тезис из традиции");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "appeal to tradition (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "если так делали всегда — значит хорошо",
                "раз так делали всегда, значит верно",
                "традиция доказывает правильность",
                "всегда так было, значит так и надо",
                "старый способ лучше нового",
                "we've always done it this way so it is right",
                "tradition proves it is correct",
                "because it is traditional it is better",
                "the old way is always better",
                "appeal to tradition",
                "argumentum ad antiquitatem");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "appeal to tradition",
                "argument from tradition",
                "традиция не доказательство",
                "то, что так делали всегда, не значит хорошо",
                "tradition does not make it true",
                "tradition is not an argument",
                "не выводи истину из традиции",
                "age of a practice is not evidence of quality");
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
