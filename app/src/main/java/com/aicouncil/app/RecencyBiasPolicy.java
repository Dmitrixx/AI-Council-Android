package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local recency bias signals.
 * Flags when a conclusion overweight recent events, "lately", "just happened",
 * short recent streaks or "the last few times" without accounting for longer-term
 * base rates, historical averages, or acknowledging that recent is not the same as typical.
 * Hypotheses requiring context, not automatic verdicts. Coordinator must not average them away.
 */
public final class RecencyBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private RecencyBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresBaseForRecent(a) || ignoresBaseForRecent(b),
                "recency bias: вывод из недавних событий или короткого периода без учёта долгосрочной базовой частоты или исторического среднего");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта опираются на недавнее без называния longer-term base rate или исторической нормы");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "recency bias (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
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

    private static boolean acknowledgesLongerTerm(String t) {
        return has(t,
                "базовая частота", "base rate", "историческ", "historical", "долгосрочн", "long-term",
                "в среднем за годы", "over the years", "типично", "typical", "не только недавнее",
                "реценси не равно типичному", "recent is not typical", "среднее за период");
    }

    private static boolean recentClaim(String t) {
        return has(t,
                "в последнее время", "lately", "recently", "только что", "just happened",
                "последние несколько", "the last few", "в этом году уже", "недавно",
                "short streak", "последняя серия", "за последний месяц", "last month");
    }

    private static boolean ignoresBaseForRecent(String t) {
        return recentClaim(t) && !acknowledgesLongerTerm(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignoresBaseForRecent(a) && ignoresBaseForRecent(b);
    }
}
