package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local Texas sharpshooter / post-hoc target fitting signals.
 * Drawing the bullseye around the cluster after seeing the data
 * («подогнал цель под данные», «выбрал группу после наблюдения», «texas sharpshooter», «drew the target after»)
 * is a hypothesis, not independent evidence.
 * Naming the fallacy is not itself the error.
 * The coordinator must not close a verdict on a post-hoc fitted target alone.
 */
public final class TexasSharpshooterPolicy {
    private static final int MAX_SIGNALS = 2;

    private TexasSharpshooterPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, ignores(a) || ignores(b),
                "texas sharpshooter: вывод на основе цели, подогнанной под наблюдённый кластер после факта");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта подгоняют цель под данные вместо фиксированной гипотезы");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "texas sharpshooter (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }

    private static boolean naivePattern(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "подогнал цель под данные",
                "выбрал кластер после наблюдения",
                "нарисовал мишень вокруг попаданий",
                "после факта определил что считать успехом",
                "texas sharpshooter",
                "drew the target after",
                "selected the cluster after seeing",
                "post-hoc fitted the hypothesis",
                "painted the bullseye around the hits",
                "defined the success criterion after the data");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "texas sharpshooter",
                "post-hoc target",
                "подгон цели под данные",
                "не подгоняй гипотезу после данных",
                "do not fit the target after",
                "hypothesis must be fixed in advance");
    }

    private static boolean has(String text, String... phrases) {
        if (text == null) return false;
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
