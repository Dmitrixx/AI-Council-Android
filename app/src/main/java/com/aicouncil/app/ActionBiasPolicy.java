package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local action-bias signals.
 * Preferring to take some action over inaction ("we must do something", "can't just wait",
 * "better to act than do nothing") even when the expected value of waiting / not intervening
 * may be equal or higher, without explicit comparison of the value of inaction, is a hypothesis, not evidence.
 * Distinct from urgency (time pressure), omission bias (preferring inaction to avoid responsibility),
 * and present bias (discounting future).
 * Coordinator must not treat "doing something is better than nothing" as settled without weighing the option of deliberate inaction.
 */
public final class ActionBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private ActionBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "action bias: предпочтение действия бездействию («надо что-то сделать», «нельзя просто ждать», «лучше действовать чем ничего не делать») без сравнения ожидаемой ценности бездействия");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта предпочитают действие бездействию без явного сравнения ценности ожидания");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "action bias (possible signal, requires context): " + core;
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
                "action bias", "предвзятость к действию",
                "value of inaction", "ценность бездействия",
                "doing nothing may be better", "ничего не делать может быть лучше",
                "compare to waiting", "сравнить с ожиданием",
                "inaction has expected value", "бездействие имеет ожидаемую ценность");
    }

    private static boolean naivePattern(String t) {
        boolean action = has(t,
                "надо что-то сделать", "нельзя просто сидеть и ждать",
                "лучше действовать чем ничего не делать",
                "we must do something", "can't just sit and wait",
                "better to do something than nothing",
                "нельзя бездействовать", "inaction is not an option",
                "doing nothing is worse", "бездействие хуже",
                "must act now even if", "надо действовать даже если");
        return action && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
