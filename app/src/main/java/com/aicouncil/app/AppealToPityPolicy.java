package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local appeal-to-pity / argumentum ad misericordiam policy.
 * Detects conclusions that something is true, good, or preferable merely because it evokes pity,
 * sympathy, or emotional appeal to the unfortunate («если жалко — значит правильно», «poor so it must be true»).
 * Naming the error, or saying pity is not an argument, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class AppealToPityPolicy {
    private static final int MAX_SIGNALS = 2;

    private AppealToPityPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "appeal to pity: вывод из жалости, сочувствия или эмоционального призыва к несчастным");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта опираются на жалость как доказательство");
        add(conflicts, initial, hitA ^ hitB,
                "appeal to pity: один эксперт выводит тезис из жалости");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "appeal to pity (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "если жалко — значит правильно",
                "раз несчастный, значит прав",
                "жалость доказывает",
                "poor so it is true",
                "appeal to pity",
                "argumentum ad misericordiam",
                "because they suffer it must be right",
                "sympathy proves the claim",
                "if we feel sorry it is correct");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "appeal to pity",
                "argument from pity",
                "жалость не доказательство",
                "pity is not an argument",
                "sympathy does not prove truth",
                "не выводи истину из жалости",
                "pity does not make it true");
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
