package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local appeal-to-force / argumentum ad baculum policy.
 * Detects a conclusion that something is true or must be accepted merely because of a threat of force, punishment, or coercion
 * ("agree or else", "if you don't accept this you will be punished", "the threat proves the point").
 * Naming the error, or saying force is not evidence, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class AppealToForcePolicy {
    private static final int MAX_SIGNALS = 2;

    private AppealToForcePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "appeal to force: вывод из угрозы силы, наказания или принуждения без доказательств");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта опираются на угрозу силы как доказательство");
        add(conflicts, initial, hitA ^ hitB,
                "appeal to force: один эксперт выводит тезис из угрозы или принуждения");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "appeal to force (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "соглашайся или будет плохо",
                "если не примешь — накажем",
                "угроза доказывает",
                "сила заставляет согласиться",
                "agree or else",
                "if you don't accept you will be punished",
                "the threat proves the point",
                "appeal to force",
                "argumentum ad baculum",
                "coercion makes it true",
                "you must accept because of the consequences of refusal");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "appeal to force",
                "argumentum ad baculum",
                "угроза не доказательство",
                "сила не делает тезис истинным",
                "force is not evidence",
                "coercion is not an argument",
                "не выводи истину из угрозы",
                "being threatened does not make it true");
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
