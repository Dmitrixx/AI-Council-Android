package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local crux signal.
 * Opposite recommendations that never name the observation which would flip the conclusion
 * are a hypothesis, not a finished decision. Distinct from probe-before-commit
 * (irreversible step) and from overconfidence (certainty words).
 * Coordinator must not close DO until the switch is named.
 */
public final class CruxPolicy {
    private static final int MAX_SIGNALS = 2;

    private CruxPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        boolean opposed = (recommends(a) && rejects(b)) || (recommends(b) && rejects(a));
        add(conflicts, initial, opposed && !namesCrux(a) && !namesCrux(b),
                "разногласие без переключателя: противоположные рекомендации и ни одного наблюдения, которое перевернёт вывод");
        add(conflicts, initial, opposed && namesCrux(a) != namesCrux(b),
                "переключатель назван только одной стороной — вторая рекомендация не говорит, что её опровергнет");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "crux (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(42, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean namesCrux(String t) {
        return has(t,
                "crux", "переключатель", "что перевернёт", "что перевернет",
                "вывод изменится если", "вывод изменится, если",
                "если окажется", "если выяснится",
                "what would change my mind", "what would flip",
                "observation that would flip", "ключевое допущение",
                "допущение, которое перевернёт", "допущение, которое перевернет");
    }

    private static boolean recommends(String t) {
        return has(t,
                "рекомендую делать", "стоит делать", "нужно делать", "надо делать",
                "рекомендую запуск", "стоит запустить",
                "should do it", "recommend doing", "go ahead",
                "я за то, чтобы делать");
    }

    private static boolean rejects(String t) {
        return has(t,
                "не рекомендую", "не стоит делать", "не надо делать", "не нужно делать",
                "не стоит запускать", "рекомендую не делать",
                "should not", "do not proceed", "recommend against",
                "я против того, чтобы делать");
    }
}
