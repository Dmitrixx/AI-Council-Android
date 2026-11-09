package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local nirvana fallacy / perfect solution fallacy signals.
 * Rejecting a realistic or imperfect solution because it is not 100% perfect,
 * or demanding an ideal that does not exist in practice
 * («если не идеально — не стоит делать», «perfect or nothing», «only a perfect solution counts», «nirvana fallacy»)
 * is a hypothesis, not independent evidence.
 * Naming the fallacy is not itself the error.
 * The coordinator must not close a verdict solely because a proposal is imperfect.
 */
public final class NirvanaFallacyPolicy {
    private static final int MAX_SIGNALS = 2;

    private NirvanaFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, ignores(a) || ignores(b),
                "nirvana fallacy: отвергает реалистичное решение, потому что оно не идеально");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта требуют идеального решения и отвергают рабочие варианты");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "nirvana fallacy (possible signal, requires context): " + core;
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
                "если не идеально — не стоит",
                "только идеальное решение",
                "раз не 100% — не делаем",
                "лучше ничего, чем неидеально",
                "perfect or nothing",
                "only a perfect solution is acceptable",
                "if it's not perfect it's useless",
                "nirvana fallacy",
                "perfect solution fallacy",
                "rejecting good because not perfect");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "nirvana fallacy",
                "perfect solution fallacy",
                "ошибка нирваны",
                "идеальное решение не обязательно",
                "не отвергай рабочее из-за неидеальности",
                "good enough is often better than perfect",
                "don't let the perfect be the enemy of the good");
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
