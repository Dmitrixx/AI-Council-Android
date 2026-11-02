package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local availability heuristic signals.
 * Flags when probability or frequency judgments appear driven mainly by vivid, recent, or easily recalled examples
 * without reference to base rates, reference classes, or systematic evidence.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class AvailabilityPolicy {
    private static final int MAX_SIGNALS = 3;

    private AvailabilityPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, vividExampleWithoutBaseRate(a) || vividExampleWithoutBaseRate(b),
                "эвристика доступности: оценка вероятности опирается на яркий/легко припоминаемый пример без base rate или reference class");
        add(conflicts, initial, bothRelyOnAnecdote(a, b),
                "общий анекдот: оба эксперта опираются на похожие яркие случаи без систематических данных");
        add(conflicts, initial, missingFrequencyCheck(a) || missingFrequencyCheck(b),
                "нет проверки частоты: утверждение о «часто происходит» без упоминания статистики или базовой частоты");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "эвристика доступности (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean vividExampleWithoutBaseRate(String t) {
        boolean vivid = has(t, "яркий пример", "недавно слышал", "все говорят", "как в том случае",
                "помню случай", "все знают", "classic example", "everyone knows", "recent case",
                "яркий случай", "помню, как");
        boolean base = has(t, "base rate", "базовая частота", "reference class", "статистика",
                "в среднем", "по данным", "systematic", "meta-analysis", "мета-анализ");
        return vivid && !base;
    }

    private static boolean bothRelyOnAnecdote(String a, String b) {
        boolean aAnecdote = has(a, "анекдот", "единичный случай", "один случай", "one case", "anecdote");
        boolean bAnecdote = has(b, "анекдот", "единичный случай", "один случай", "one case", "anecdote");
        return aAnecdote && bAnecdote;
    }

    private static boolean missingFrequencyCheck(String t) {
        boolean frequencyClaim = has(t, "часто происходит", "обычно бывает", "редко", "всегда так",
                "часто встречается", "commonly happens", "usually", "always the case");
        boolean stats = has(t, "статистика", "данные", "base rate", "процент", "%", "измерения");
        return frequencyClaim && !stats;
    }
}
