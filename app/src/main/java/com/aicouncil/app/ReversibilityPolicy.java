package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local one-way-door signals.
 * A recommendation to take a hard-to-undo step (permanent delete, burn the boats,
 * lock-in, "no way back") without naming a pilot, rollback, exit ramp or premortem
 * is a hypothesis, not evidence. Distinct from status-quo bias, sunk cost and action bias:
 * the missing piece is the cost of being wrong when the door only opens one way.
 * The coordinator must not average a reversible pilot with an irreversible commitment.
 */
public final class ReversibilityPolicy {
    private static final int MAX_SIGNALS = 2;

    private ReversibilityPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "односторонняя дверь: необратимый шаг («безвозвратно», «точки невозврата», «нельзя откатить») без пилота, отката или премортема");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта закрывают необратимый шаг, не назвав, как выйти, если решение окажется неверным");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "reversibility (possible signal, requires context): " + core;
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
                "пилот", "pilot",
                "план отката", "rollback", "roll back",
                "можно отменить", "можно откатить",
                "это обратимо", "решение обратимо",
                "reversible", "two-way", "two way",
                "премортем", "premortem", "pre-mortem",
                "сначала на мал", "на небольшой группе",
                "exit ramp", "дорожка выхода", "путь назад",
                "если не сработает, верн");
    }

    private static boolean naivePattern(String t) {
        boolean door = has(t,
                "безвозвратно", "необратим",
                "нельзя откатить", "нельзя отменить",
                "точки невозврата", "точка невозврата",
                "сжечь мосты", "сжигаем мосты",
                "one-way door", "one way door",
                "no way back", "удалить навсегда",
                "удалить безвозвратно", "закрыть навсегда",
                "permanently delete", "irreversible");
        return door && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
