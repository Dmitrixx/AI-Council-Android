package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local Chesterton's-fence signals.
 * A recommendation to abolish a rule, process, check or team without naming
 * why it exists is a hypothesis, not a reason to tear it down.
 * Distinct from status-quo bias (preferring the default) and from appeal to
 * tradition ("we always did it, so it is right"). Coordinator must not average it away.
 */
public final class ChestertonFencePolicy {
    private static final int MAX_SIGNALS = 2;

    private ChestertonFencePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "chesterton fence: снос правила, процесса или проверки без исходной функции");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта предлагают убрать существующее, не назвав, зачем оно появилось");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "chesterton fence (возможный сигнал, требует контекста): " + core;
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
                "исходная функция", "исходную функцию", "зачем это появилось",
                "зачем правило появилось", "зачем проверка появилась", "зачем процесс появился",
                "why it exists", "why the rule exists", "why the fence exists",
                "original function", "chesterton", "забор честертона",
                "сначала понять, зачем", "сначала выяснить, зачем");
    }

    private static boolean removal(String t) {
        return has(t,
                "отменим регламент", "отменить регламент", "отменим правило", "отменить правило",
                "убрать эту проверку", "убрать правило", "упразднить контроль", "выкинуть регламент",
                "снести процесс", "закрыть отдел", "убрать процесс",
                "abolish the policy", "abolish the rule", "abolish it",
                "remove the checklist", "remove the policy", "delete the process",
                "scrap the rule", "just scrap", "tear down the process");
    }

    private static boolean forgottenAndRemoved(String t) {
        boolean forgotten = has(t,
                "никто не помнит зачем", "никто не знает зачем", "раз никто не помнит",
                "nobody remembers why", "no one knows why we", "no one remembers why");
        boolean act = has(t, "убрать", "отмен", "abolish", "remove", "scrap", "снести", "упраздн");
        return forgotten && act;
    }

    private static boolean triggered(String t) {
        return removal(t) || forgottenAndRemoved(t);
    }

    private static boolean ignores(String t) {
        return triggered(t) && !acknowledges(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
