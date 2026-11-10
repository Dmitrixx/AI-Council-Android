package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local red herring policy.
 * Detects when an answer distracts from the main issue by introducing an unrelated topic
 * instead of addressing the argument ("не о том речь", "irrelevant point", "red herring").
 * Naming the error is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class RedHerringPolicy {
    private static final int MAX_SIGNALS = 2;

    private RedHerringPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "red herring: отвлечение от темы / нерелевантный аргумент");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта уводят от основного вопроса");
        add(conflicts, initial, hitA ^ hitB,
                "red herring: один эксперт отвлекает от темы");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "red herring (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "это не о том",
                "давайте поговорим о другом",
                "а вот что скажете на это",
                "это отвлекает от сути",
                "red herring",
                "irrelevant diversion",
                "let's talk about something else",
                "that's not the point but",
                "changing the subject");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "red herring",
                "это отвлечение",
                "не переключай тему",
                "stay on topic",
                "this is a distraction");
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
