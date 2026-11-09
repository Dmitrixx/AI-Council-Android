package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local cherry-picking / selective evidence signals.
 * Drawing a conclusion from carefully selected supporting cases
 * while ignoring or downplaying contrary data
 * («все исследования подтверждают», «выбрал только удобные», «cherry-picked studies», «only the favorable data»)
 * is a hypothesis, not independent evidence.
 * Naming the fallacy is not itself the error.
 * The coordinator must not close a verdict on a selectively curated sample alone.
 */
public final class CherryPickingPolicy {
    private static final int MAX_SIGNALS = 2;

    private CherryPickingPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, ignores(a) || ignores(b),
                "cherry-picking: вывод на основе избирательно подобранных поддерживающих данных при игнорировании противоречащих");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта опираются на выборочные данные вместо того, чтобы учитывать полную выборку");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "cherry-picking (possible signal, requires context): " + core;
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
                "все исследования подтверждают",
                "все данные говорят за",
                "выбрал только удобные примеры",
                "только положительные результаты считаются",
                "игнорирую неудобные исследования",
                "все кого я слушал согласны",
                "только наши данные важны",
                "all studies confirm",
                "every study shows",
                "cherry-picked",
                "only the favorable",
                "selected only supporting",
                "ignored the contrary",
                "only positive results count",
                "all the data i looked at supports",
                "conveniently selected examples");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "cherry-picking",
                "selective evidence",
                "выборочные данные",
                "избирательный подбор",
                "не учитывает противоречащие",
                "do not cherry-pick",
                "full sample required",
                "selection bias in evidence");
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
