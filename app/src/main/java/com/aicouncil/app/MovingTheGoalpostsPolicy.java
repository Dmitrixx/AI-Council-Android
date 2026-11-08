package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local moving-the-goalposts signals.
 * After evidence or arguments are presented that meet the original stated criteria, the criteria are shifted
 * to demand more, different, or stronger proof ("that's not enough", "now you must also prove X", "the real test is...")
 * without acknowledging the change. This is a hypothesis, not evidence.
 * Distinct from motte-and-bailey (retreat to weaker claim), slippery slope, and underdetermination.
 * Coordinator must not treat a shifted standard as automatically invalidating prior evidence, nor the absence of a shift as proof the original standard was met.
 */
public final class MovingTheGoalpostsPolicy {
    private static final int MAX_SIGNALS = 2;

    private MovingTheGoalpostsPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "moving the goalposts: смещение критериев доказательства после того, как исходные условия были удовлетворены");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта сдвигают цель: требуют более сильного доказательства, не признавая смену стандарта");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "moving the goalposts (possible signal, requires context): " + core;
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
                "moving the goalposts",
                "сдвиг цели",
                "критерии изменились",
                "стандарт доказательства повышен",
                "the original criteria were met",
                "acknowledging the shift");
    }

    private static boolean naivePattern(String t) {
        boolean shift = has(t,
                "этого недостаточно",
                "теперь нужно ещё доказать",
                "реальный критерий",
                "на самом деле нужно показать",
                "that's not enough",
                "now you must also",
                "the real test is",
                "moving the goalposts",
                "you still haven't proven",
                "criteria have changed",
                "стандарт повысился");
        boolean noAck = !has(t,
                "исходные критерии выполнены",
                "признаю смену стандарта",
                "original standard was satisfied",
                "I am raising the bar explicitly");
        return shift && noAck && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
