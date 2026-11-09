package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local hasty-generalization / insufficient-sample signals.
 * Drawing a broad conclusion from one or a few unrepresentative cases
 * («один пример доказывает», «все кого я знаю», «from this single case»)
 * is a hypothesis, not independent evidence.
 * Naming the fallacy is not itself the error.
 * The coordinator must not close a verdict on a tiny sample alone.
 */
public final class HastyGeneralizationPolicy {
    private static final int MAX_SIGNALS = 2;

    private HastyGeneralizationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, ignores(a) || ignores(b),
                "hasty generalization: широкий вывод из одного или нескольких нерепрезентативных случаев");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта обобщают по маленькой выборке вместо того, чтобы требовать больше данных");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "hasty generalization (possible signal, requires context): " + core;
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
                "один пример доказывает",
                "одного примера достаточно",
                "из этого одного случая следует",
                "в одном случае значит всегда",
                "все кого я знаю",
                "никто из моих знакомых",
                "мой опыт показывает что все",
                "один раз увидел значит так и есть",
                "этот случай доказывает правило",
                "one example proves",
                "a single case shows",
                "from this one instance",
                "this one example means all",
                "everyone i know",
                "nobody i know",
                "my experience shows that all",
                "one time i saw means",
                "this single observation proves",
                "from one data point we conclude",
                "n of 1 is enough",
                "sample of one proves");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "hasty generalization",
                "insufficient sample",
                "поспешное обобщение",
                "недостаточная выборка",
                "один пример не доказательство",
                "маленькая выборка не основание",
                "do not generalize from one case",
                "single observation is not proof",
                "n=1 is not evidence");
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
