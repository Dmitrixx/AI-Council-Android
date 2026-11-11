package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local Bastiat / seen-and-unseen policy.
 * Detects a conclusion that counts only the visible benefit or cost and treats
 * what is not seen as irrelevant ("считаем только то, что видно",
 * "only the seen effect matters", "broken window creates jobs so it is good").
 * Naming the unseen cost, or saying it still counts, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class SeenAndUnseenPolicy {
    private static final int MAX_SIGNALS = 2;

    private SeenAndUnseenPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "seen and unseen: вывод только по видимому эффекту, невидимая цена отброшена");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают выбор тем, что видно, и игнорируют невидимое");
        add(conflicts, initial, hitA ^ hitB,
                "seen and unseen: один эксперт считает только видимый эффект");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "seen and unseen (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "видно только выгоду, остальное не считается",
                "считаем только то, что видно",
                "того, чего не видно, для решения нет",
                "разбитое окно создаёт работу, значит это полезно",
                "only the seen effect matters",
                "ignore what is not seen",
                "unseen costs do not count",
                "broken window creates jobs so it is good",
                "what you cannot see does not affect the choice");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "невидимая цена тоже считается",
                "то, чего не видно, тоже цена",
                "бастиа",
                "what is not seen still counts",
                "the unseen is still a cost",
                "разбитое окно не создаёт богатства",
                "seen and the unseen",
                "не закрывай выбор только увиденным");
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
