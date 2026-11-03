package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local survivorship-bias signals.
 * Flags when conclusions rest on visible successes without accounting for the
 * invisible failures, dropout rate, or selection into the observed sample.
 * Hypotheses requiring context, not verdicts. The coordinator must not average them.
 */
public final class SurvivorshipBiasPolicy {
    private static final int MAX_SIGNALS = 3;

    private SurvivorshipBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, successesWithoutFailures(a) || successesWithoutFailures(b),
                "смещение выжившего: вывод строится на видимых успехах без учёта невидимых провалов, отсева или доли выживших");
        add(conflicts, initial, bothIgnoreBaseFailures(a, b),
                "оба опираются на истории успеха и не называют базовую частоту провалов в том же классе");
        add(conflicts, initial, selectedSampleClaim(a) || selectedSampleClaim(b),
                "утверждение о «тех, кто добился» без поправки на то, что недобившиеся не попали в выборку");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "survivorship bias (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(38, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (affirmed(text, word)) return true;
        return false;
    }

    private static boolean affirmed(String text, String phrase) {
        int from = 0;
        while (from < text.length()) {
            int i = text.indexOf(phrase, from);
            if (i < 0) return false;
            int start = Math.max(0, i - 4);
            String prefix = text.substring(start, i);
            if (!prefix.endsWith("не ") && !prefix.endsWith("not ")) return true;
            from = i + Math.max(1, phrase.length());
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean accountsForFailures(String t) {
        return has(t,
                "невидимые провалы", "доля отсева", "базовая частота провалов", "survivorship",
                "те, кто не добился", "failed attempts", "dropout rate", "selection bias",
                "не попали в выборку", "все попытки");
    }

    private static boolean successesWithoutFailures(String t) {
        boolean successFocus = has(t,
                "истории успеха", "успешные примеры", "те, кто добился", "победители",
                "successful cases", "those who made it", "winners show");
        boolean conclude = has(t, "значит", "следовательно", "рекомендую", "стоит делать так", "therefore", "i recommend");
        return successFocus && conclude && !accountsForFailures(t);
    }

    private static boolean bothIgnoreBaseFailures(String a, String b) {
        return successesWithoutFailures(a) && successesWithoutFailures(b);
    }

    private static boolean selectedSampleClaim(String t) {
        boolean selected = has(t,
                "среди тех, кто", "look at the successful", "the ones who succeeded",
                "все известные случаи");
        boolean noBase = !accountsForFailures(t);
        return selected && noBase;
    }
}
