package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local publication-bias / file-drawer check.
 * Flags a verdict that treats published successes, success stories, or
 * "every published paper agrees" as the full distribution, without the
 * missing negative results. Naming the gap is not itself an error.
 * Distinct from survivorship (visible winners of a process) and cherry-picking
 * (a hand-picked subset of known observations).
 * The coordinator must not close on the published slice alone.
 */
public final class PublicationBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private PublicationBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "publication bias: published successes treated as the whole distribution");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают эффект на опубликованных успехах без ящика стола");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "publication bias (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || namesMissing(t)) return false;
        return successOnly(t) && closes(t);
    }

    private static boolean successOnly(String t) {
        return has(t,
                "только успешные",
                "только опубликован",
                "все кейсы успешные",
                "все статьи подтверждают",
                "истории успеха доказывают",
                "published studies all",
                "only the successes",
                "success stories prove",
                "every published paper agrees");
    }

    private static boolean closes(String t) {
        return has(t,
                "поэтому это работает",
                "эффект доказан",
                "вердикт: эффект есть",
                "можно внедрять",
                "therefore it works",
                "the effect is real",
                "safe to adopt");
    }

    private static boolean namesMissing(String t) {
        return has(t,
                "неопубликованные неудачи",
                "ящик стола",
                "пропавшие отрицательные",
                "unpublished failures",
                "file drawer",
                "missing negative results");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "publication bias",
                "смещение публикации",
                "publication-bias");
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
