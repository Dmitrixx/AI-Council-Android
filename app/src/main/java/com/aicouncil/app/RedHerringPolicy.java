package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local red herring / irrelevant distraction policy.
 * Detects when an answer introduces an unrelated topic to divert from the core claim
 * («кстати, а вы знаете что…», «не об этом речь», «red herring», «that's not the point» used as diversion).
 * Naming the fallacy or saying the point is irrelevant is not the error.
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
                "red herring: отвлечение на несвязанную тему / irrelevant diversion");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта отвлекают от основного тезиса");
        add(conflicts, initial, hitA ^ hitB,
                "red herring: один эксперт уводит в сторону");
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
                "кстати а вы знаете что",
                "не об этом речь а о том",
                "давайте лучше поговорим о другом",
                "это отвлекает от сути",
                "red herring",
                "that's not the point but",
                "let's talk about something else",
                "irrelevant diversion",
                "changing the subject to");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "red herring",
                "отвлечение не доказательство",
                "это не ответ на вопрос",
                "irrelevant to the claim",
                "не уводи в сторону");
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
