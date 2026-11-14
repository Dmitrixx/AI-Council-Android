package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local better-than-average / illusory superiority signals.
 * A verdict that treats the decision maker, group or option as above average
 * on a desirable trait without independent evidence or base-rate comparison.
 * Coordinator must not close on the self-flattering ranking alone.
 * Naming the gap is not itself an error.
 */
public final class BetterThanAveragePolicy {
    private static final int MAX_SIGNALS = 2;

    private BetterThanAveragePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, aboveAverage(a) || aboveAverage(b),
                "better-than-average: вердикт закрывает выбор тем, что «мы лучше среднего», «у нас выше шансы», «я/мы выше среднего» без независимого основания или сравнения с базой");
        add(conflicts, initial, bothAbove(a, b),
                "оба эксперта опираются на превосходство над средним без независимой проверки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "better-than-average (possible signal, requires context): " + core;
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
                "better than average", "выше среднего",
                "illusory superiority", "иллюзия превосходства",
                "base rate comparison", "сравнение с базовой",
                "independent evidence of superiority", "независимое основание превосходства");
    }

    private static boolean aboveAverage(String t) {
        boolean pattern = has(t,
                "мы лучше среднего",
                "выше среднего",
                "у нас выше шансы чем у большинства",
                "я лучше большинства",
                "our chances are above average",
                "we are better than average",
                "above the mean",
                "better than most people",
                "most people are worse");
        return pattern && !acknowledges(t);
    }

    private static boolean bothAbove(String a, String b) {
        return aboveAverage(a) && aboveAverage(b);
    }
}
