package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local selection-bias / sampling-bias signals.
 * Flags when a conclusion rests on a non-representative sample (self-selected,
 * convenience, truncated, or volunteer) without acknowledging the selection
 * process, who was excluded, or the population base rate.
 * Hypotheses requiring context, not automatic verdicts. Coordinator must not average them away.
 */
public final class SelectionBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private SelectionBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, selectionWithoutAck(a) || selectionWithoutAck(b),
                "выборка нерепрезентативна: вывод из удобной, самоотобранной или усечённой выборки без учёта процесса отбора или base rate популяции");
        add(conflicts, initial, bothIgnoreSelection(a, b),
                "оба эксперта опираются на отобранную выборку и не называют, кто не попал в наблюдение");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "selection bias (возможный сигнал, требует контекста): " + core;
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

    private static boolean acknowledgesSelection(String t) {
        return has(t,
                "нерепрезентативн", "self-selected", "convenience sample",
                "кто не попал", "excluded from the sample", "selection bias",
                "смещение отбора", "volunteer bias", "усечённая выборка",
                "не вся популяция", "base rate of the population");
    }

    private static boolean selectionWithoutAck(String t) {
        boolean sampleFocus = has(t,
                "опрошенные", "респонденты", "пользователи которые", "those who responded",
                "клиенты которые купили", "люди из нашей базы", "survey respondents",
                "the people we talked to", "те, кто согласился", "volunteer respondents");
        boolean conclude = has(t, "значит", "следовательно", "рекомендую", "показывает что", "therefore", "i recommend", "это доказывает");
        return sampleFocus && conclude && !acknowledgesSelection(t);
    }

    private static boolean bothIgnoreSelection(String a, String b) {
        return selectionWithoutAck(a) && selectionWithoutAck(b);
    }
}
