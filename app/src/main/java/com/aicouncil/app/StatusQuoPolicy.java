package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local status-quo bias signals.
 * Flags when the current state or "do nothing" is preferred mainly because it is the default,
 * without explicit comparison of switching costs, expected value of change, or opportunity of action.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class StatusQuoPolicy {
    private static final int MAX_SIGNALS = 3;

    private StatusQuoPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, prefersStatusQuoWithoutJustification(a) || prefersStatusQuoWithoutJustification(b),
                "status quo bias: предпочтение текущего положения или «ничего не делать» без явного сравнения выгод изменения и затрат на переключение");
        add(conflicts, initial, bothDefaultToInaction(a, b),
                "оба склоняются к бездействию: рекомендация оставить как есть без разбора ожидаемой ценности действия");
        add(conflicts, initial, ignoresSwitchingCost(a) || ignoresSwitchingCost(b),
                "игнорированы затраты переключения: обсуждают изменение, но не называют стоимость перехода или потери от status quo");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "status quo (возможный сигнал, требует контекста): " + core;
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

    private static boolean prefersStatusQuoWithoutJustification(String t) {
        boolean status = has(t, "оставить как есть", "ничего не делать", "status quo", "текущее положение",
                "не менять", "do nothing", "keep as is", "не трогать");
        boolean justification = has(t, "затраты на переключение", "switching cost", "выгода от изменения",
                "expected value of change", "сравнил с", "альтернатива лучше");
        return status && !justification;
    }

    private static boolean bothDefaultToInaction(String a, String b) {
        boolean aInaction = has(a, "оставить", "не делать", "подождать", "не менять");
        boolean bInaction = has(b, "оставить", "не делать", "подождать", "не менять");
        boolean relevant = has(a + b, "решени", "выбор", "стоит ли", "запускать");
        return aInaction && bInaction && relevant && !has(a + b, "потому что", "выгода", "риск изменения");
    }

    private static boolean ignoresSwitchingCost(String t) {
        boolean change = has(t, "изменить", "перейти", "switch", "новый подход", "заменить");
        boolean cost = has(t, "затраты перехода", "switching cost", "стоимость смены", "потери от перехода");
        return change && !cost;
    }
}
