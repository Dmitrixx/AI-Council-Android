package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local planning fallacy signals.
 * Flags when time, cost, or effort estimates for a task/project appear optimistic
 * without explicit reference to base rates from similar past projects, outside view,
 * or historical overruns.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class PlanningFallacyPolicy {
    private static final int MAX_SIGNALS = 3;

    private PlanningFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, optimisticEstimateWithoutHistory(a) || optimisticEstimateWithoutHistory(b),
                "planning fallacy: оптимистичная оценка времени/затрат без опоры на историю похожих проектов или outside view");
        add(conflicts, initial, bothIgnorePastOverruns(a, b),
                "общий оптимизм: оба эксперта дают короткие сроки/низкие затраты без упоминания типичных перерасходов");
        add(conflicts, initial, uniqueProjectException(a) || uniqueProjectException(b),
                "исключение «этот проект уникален»: оценка без base rate класса проектов");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "planning fallacy (возможный сигнал, требует контекста): " + core;
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

    private static boolean optimisticEstimateWithoutHistory(String t) {
        boolean estimate = has(t, "займёт", "потребуется", "срок", "время", "дней", "недель", "месяцев",
                "стоимость", "бюджет", "затраты", "estimate", "will take", "days", "weeks", "budget");
        boolean optimistic = has(t, "всего", "всего лишь", "быстро", "недорого", "легко", "просто",
                "only", "quickly", "easily", "cheap");
        boolean history = has(t, "похожие проекты", "в прошлом", "исторически", "base rate",
                "типичный перерасход", "outside view", "reference class", "similar projects");
        return estimate && optimistic && !history;
    }

    private static boolean bothIgnorePastOverruns(String a, String b) {
        boolean aOpt = has(a, "быстро", "всего", "недорого", "легко") && !has(a, "перерасход", "задержка", "overrun", "delay");
        boolean bOpt = has(b, "быстро", "всего", "недорого", "легко") && !has(b, "перерасход", "задержка", "overrun", "delay");
        return aOpt && bOpt;
    }

    private static boolean uniqueProjectException(String t) {
        boolean unique = has(t, "этот проект уникален", "this project is unique", "особый случай", "не как другие");
        boolean noBase = !has(t, "base rate", "похожие", "исторически", "reference");
        return unique && noBase;
    }
}
