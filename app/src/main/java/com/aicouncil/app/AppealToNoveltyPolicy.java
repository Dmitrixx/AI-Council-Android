package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local appeal-to-novelty policy.
 * Detects a conclusion that something is good, correct, better, or preferable merely because it is new,
 * modern, innovative, or "the latest" ("новое лучше старого", "раз современное — значит лучше",
 * "new is always better", "because it is modern it is superior").
 * Naming the error, or saying novelty is not an argument, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class AppealToNoveltyPolicy {
    private static final int MAX_SIGNALS = 2;

    private AppealToNoveltyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "appeal to novelty: вывод из того, что это новое / modern / innovative");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта опираются на новизну как доказательство");
        add(conflicts, initial, hitA ^ hitB,
                "appeal to novelty: один эксперт выводит тезис из новизны");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "appeal to novelty (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "новое лучше старого",
                "раз современное — значит лучше",
                "инновационное всегда превосходит",
                "потому что это новое — значит правильно",
                "latest technology is always superior",
                "new is better than old",
                "because it is modern it is better",
                "innovation proves it is correct",
                "appeal to novelty",
                "argumentum ad novitatem",
                "cutting-edge so it must be right");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "appeal to novelty",
                "argument from novelty",
                "новизна не доказательство",
                "то, что новое, не значит лучше",
                "novelty does not make it true",
                "novelty is not an argument",
                "не выводи истину из новизны",
                "being new is not evidence of quality");
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
