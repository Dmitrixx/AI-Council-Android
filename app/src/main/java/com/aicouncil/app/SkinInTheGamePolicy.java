package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local skin-in-the-game check.
 * Flags a recommendation that proceeds while the downside is explicitly
 * someone else's, without naming who bears the loss if the call is wrong.
 * Naming the gap is not itself an error.
 * Distinct from principal-agent (hidden incentives) and omission bias.
 * The coordinator must not close while the recommender is insulated from the loss.
 */
public final class SkinInTheGamePolicy {
    private static final int MAX_SIGNALS = 2;

    private SkinInTheGamePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "skin in the game: downside sits on someone else while the choice is closed");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают выбор, не назвав, кто несёт убыток");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "skin in the game (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || namesDownsideOwner(t)) return false;
        return shiftsDownside(t) && recommends(t);
    }

    private static boolean shiftsDownside(String t) {
        return has(t,
                "риск несёт другой",
                "риск несут они",
                "потери на них",
                "чужими деньгами",
                "не наши деньги",
                "пострадают они",
                "downside falls on others",
                "we bear no downside",
                "someone else pays",
                "not our money");
    }

    private static boolean recommends(String t) {
        return has(t,
                "поэтому делаем",
                "рекомендую",
                "вердикт: да",
                "внедряем",
                "стоит сделать",
                "we should proceed",
                "the choice is yes",
                "recommend going ahead");
    }

    private static boolean namesDownsideOwner(String t) {
        return has(t,
                "кто несёт убыток",
                "носитель риска",
                "downside owner",
                "recommender bears",
                "убыток несёт рекомендатель",
                "loss sits with");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "skin in the game",
                "кожа в игре",
                "кожей в игре");
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
