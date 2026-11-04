package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local omission-bias signals.
 * Preferring inaction because "doing nothing is safer" is not a comparison of
 * the harm of acting and the harm of not acting. Hypotheses, not verdicts.
 */
public final class OmissionBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private OmissionBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, omissionAsSafety(a) || omissionAsSafety(b),
                "бездействие объявлено безопаснее без сравнения ущерба от отказа действовать");
        add(conflicts, initial, bothOmissionOnly(a, b),
                "оба эксперта считают бездействие безопасным само по себе");
        add(conflicts, initial, fork(a, b),
                "развилка: бездействие как безопасность против явного ущерба от отказа действовать");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "omission bias (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(36, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean omissionAsSafety(String text) {
        return hasOmissionClaim(text) && !hasHarmComparison(text);
    }

    private static boolean bothOmissionOnly(String a, String b) {
        return omissionAsSafety(a) && omissionAsSafety(b);
    }

    private static boolean fork(String a, String b) {
        return (omissionAsSafety(a) && hasHarmComparison(b))
                || (omissionAsSafety(b) && hasHarmComparison(a));
    }

    private static boolean hasOmissionClaim(String text) {
        return contains(text,
                "бездействие безопаснее", "ничего не делать безопаснее",
                "безопаснее ничего не делать", "не вмешиваться безопаснее",
                "бездействие не причиняет вреда", "лучше не действовать, так безопаснее",
                "не действовать безопаснее", "бездействие — безопасный выбор",
                "doing nothing is safer", "inaction is safer", "safer to do nothing",
                "omission is safer", "not acting is the safe choice",
                "harmless to do nothing", "doing nothing causes no harm");
    }

    private static boolean hasHarmComparison(String text) {
        return contains(text,
                "вред от бездействия", "ущерб от бездействия", "цена бездействия",
                "ущерб от отказа", "сравним действие и бездействие",
                "цена ничегонеделания", "упущенный ущерб",
                "harm of inaction", "cost of doing nothing", "harm of doing nothing",
                "compare action and inaction", "deaths from waiting",
                "damage of not acting");
    }

    private static boolean contains(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
