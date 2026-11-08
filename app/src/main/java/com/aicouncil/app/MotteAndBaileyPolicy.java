package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local motte-and-bailey signals.
 * Advancing a strong, controversial claim (bailey) and, when challenged, retreating
 * to a weaker, more defensible claim (motte) without acknowledging the retreat or
 * that the strong claim was the one under discussion is a hypothesis, not evidence.
 * Distinct from straw man (distorting the opponent), reactive devaluation (source),
 * and false dilemma: here the speaker themselves oscillates between strength levels
 * of the same position without flagging the switch.
 * Coordinator must not treat the motte as having vindicated the bailey.
 */
public final class MotteAndBaileyPolicy {
    private static final int MAX_SIGNALS = 2;

    private MotteAndBaileyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "motte-and-bailey: сильный спорный тезис при отсутствии давления сменяется слабым защитимым без признания отступления");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта переключаются между сильной и слабой формулировкой одной позиции без явного разграничения");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "motte-and-bailey (possible signal, requires context): " + core;
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
                "motte and bailey", "motte-and-bailey", "motte/bailey",
                "сильный тезис vs слабый", "сильная формулировка vs слабая",
                "отступаю к более слабому", "отступление к мотте",
                "я имел в виду более скромное", "речь шла о более слабом",
                "разграничиваю сильный и слабый",
                "strong claim vs weak claim", "retreat to the weaker claim");
    }

    private static boolean naivePattern(String t) {
        boolean switchLanguage = has(t,
                "на самом деле я имел в виду лишь",
                "в сущности это просто",
                "я всего лишь говорю, что",
                "это всего лишь означает",
                "я не утверждаю ничего радикального",
                "это просто здравый смысл",
                "никто не спорит с тем, что",
                "obviously i only meant",
                "i'm just saying that",
                "all i mean is",
                "it's really just the claim that",
                "nobody disputes the modest version",
                "сильный тезис, но если возражают",
                "когда возражают, сводится к",
                "bailey", "motte");
        // Also catch classic pair: strong absolute + soft retreat in same answer
        boolean strongAbsolute = has(t,
                "всегда и везде", "в любом случае верно",
                "единственно правильный", "неоспоримо",
                "must always", "is always true",
                "there is no alternative",
                "единственный разумный вывод");
        boolean softRetreat = has(t,
                "ну, по крайней мере", "хотя бы в том смысле",
                "в широком смысле",
                "можно понимать как",
                "at least in the sense",
                "in a manner of speaking",
                "loosely speaking");
        boolean combo = strongAbsolute && softRetreat;
        return (switchLanguage || combo) && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
