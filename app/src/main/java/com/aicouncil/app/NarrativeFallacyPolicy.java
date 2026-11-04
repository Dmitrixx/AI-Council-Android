package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local narrative-fallacy signals.
 * A coherent, vivid story is not stronger evidence than base rates, statistics or messy data.
 * Hypotheses, not verdicts.
 */
public final class NarrativeFallacyPolicy {
    private static final int MAX_SIGNALS = 2;

    private NarrativeFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, narrativeAsProof(a) || narrativeAsProof(b),
                "связная история принимается за доказательство вместо base rate или статистики");
        add(conflicts, initial, bothNarrativeOnly(a, b),
                "оба эксперта закрывают вывод историей, а не частотой или данными");
        add(conflicts, initial, fork(a, b),
                "развилка: вердикт из сюжета против явной опоры на base rate или статистику");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "narrative fallacy (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(36, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean narrativeAsProof(String text) {
        return hasNarrativeVerdict(text) && !hasSeparation(text);
    }

    private static boolean bothNarrativeOnly(String a, String b) {
        return narrativeAsProof(a) && narrativeAsProof(b);
    }

    private static boolean fork(String a, String b) {
        return (narrativeAsProof(a) && hasSeparation(b))
                || (narrativeAsProof(b) && hasSeparation(a));
    }

    private static boolean hasNarrativeVerdict(String text) {
        return contains(text,
                "история доказывает", "сюжет доказывает", "рассказ показывает, что",
                "по этой истории ясно", "из этой истории следует",
                "красивая история доказывает", "убедительная история значит",
                "история убедительнее цифр", "сюжет убедительнее статистики",
                "история важнее данных", "сюжет важнее base rate",
                "the story proves", "this narrative shows that",
                "the story is better evidence", "narrative outweighs the data",
                "the coherent story means", "the story is stronger than the stats",
                "story is more convincing than base rate",
                "vivid story proves", "compelling story shows");
    }

    private static boolean hasSeparation(String text) {
        return contains(text,
                "история не доказательство", "сюжет не заменяет base rate",
                "история не статистика", "отделим сюжет от частоты",
                "base rate важнее истории", "статистика важнее сюжета",
                "narrative is not evidence", "story is not a base rate",
                "story does not outweigh statistics", "separate the narrative from the data",
                "base rate first", "prefer the reference class over the story");
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
