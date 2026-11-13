package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local negativity-bias check.
 * Flags a verdict that treats one negative instance, risk, failure or downside as decisive
 * against multiple positive observations or base rates, without independent justification
 * that the negative item outweighs the positives in magnitude or probability.
 * Naming the gap is not itself an error.
 * Signals remain hypotheses requiring context.
 */
public final class NegativityBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private NegativityBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "negativity bias: single negative instance treated as decisive against positives");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают вердикт на одном негативе без проверки баланса");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "negativity bias (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || hasBalanceCheck(t)) return false;
        return hasNegativeMarker(t) && closes(t);
    }

    private static boolean hasNegativeMarker(String t) {
        return has(t,
                "один провал",
                "один негативный",
                "есть риск",
                "может не сработать",
                "одна ошибка",
                "one failure",
                "one negative",
                "single downside",
                "there is a risk",
                "might fail",
                "one bad example",
                "negatives outweigh");
    }

    private static boolean closes(String t) {
        return has(t,
                "поэтому не надо",
                "значит отказываемся",
                "вердикт: нет",
                "выбираем против",
                "therefore we reject",
                "the choice is no",
                "enough to refuse",
                "decides against",
                "so we should not");
    }

    private static boolean hasBalanceCheck(String t) {
        return has(t,
                "сравнил с позитивами",
                "базовая частота негативов",
                "магнитуда риска",
                "compared with positives",
                "base rate of failures",
                "magnitude of downside",
                "net expected value",
                "positives still outweigh");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "negativity bias",
                "негативное смещение",
                "один негатив не решает",
                "single negative does not decide",
                "one failure is not decisive against the base rate");
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
