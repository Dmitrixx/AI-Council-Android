package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local projection bias signals.
 * Assuming that one's current preferences, tastes, emotional state or constraints will remain
 * unchanged in the future (or that others share the current state) without acknowledging
 * adaptation, habit formation, or situational change is a hypothesis, not evidence.
 * Distinct from present bias (time preference), optimism bias and planning fallacy.
 * Coordinator must not treat "I will always feel this way" or "everyone wants what I want now" as settled.
 */
public final class ProjectionBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private ProjectionBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "projection bias: проекция текущих предпочтений/состояния на будущее без учёта адаптации или изменения ситуации");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта считают, что текущие вкусы или ограничения останутся неизменными без калибровки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "projection bias (possible signal, requires context): " + core;
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

    private static boolean acknowledges(String t) {
        return has(t,
                "projection bias", "смещение проекции",
                "предпочтения могут измениться", "preferences may change",
                "адаптация", "adaptation",
                "в будущем я могу хотеть другого", "future self may differ",
                "ситуация изменится", "situation will change");
    }

    private static boolean naivePattern(String t) {
        boolean projectsCurrent = has(t,
                "я всегда буду хотеть", "мне всегда будет нравиться",
                "все хотят того же, что и я сейчас", "everyone wants what I want now",
                "это навсегда", "this will always be true",
                "предпочтения не меняются", "preferences never change",
                "я не изменюсь", "I won't change");
        return projectsCurrent && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
