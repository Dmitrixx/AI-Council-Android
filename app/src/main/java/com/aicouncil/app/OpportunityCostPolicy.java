package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local opportunity-cost and forgone-alternatives signals.
 * Flags when a decision, recommendation or forecast evaluates absolute benefits
 * or a single option without explicit consideration of opportunity cost,
 * the next-best alternative, or what is given up by choosing it.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class OpportunityCostPolicy {
    private static final int MAX_SIGNALS = 5;

    private OpportunityCostPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, absoluteBenefitWithoutOpportunity(a) || absoluteBenefitWithoutOpportunity(b),
                "нет анализа opportunity cost: выгоды оценены абсолютно без учёта лучшей альтернативы или того, от чего отказываются");
        add(conflicts, initial, singleOptionNoAlternative(a) || singleOptionNoAlternative(b),
                "один вариант без сравнения: рекомендация одного выбора без явного разбора следующего лучшего варианта");
        add(conflicts, initial, ignoredForgone(a) || ignoredForgone(b),
                "игнорирована упущенная выгода: названа выгода, но не оценено, что теряется при выборе");
        add(conflicts, initial, bothNoOpportunityCost(a, b),
                "оба без opportunity cost: ни один эксперт не рассмотрел альтернативную стоимость или упущенные возможности");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "opportunity cost (возможный сигнал, требует контекста): " + core;
        for (String existing : conflicts) {
            if (existing.contains(core.substring(0, Math.min(40, core.length())))) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static boolean multi(String text, String[] groupA, String[] groupB) {
        return has(text, groupA) && has(text, groupB);
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean absoluteBenefitWithoutOpportunity(String t) {
        boolean benefit = has(t, "выгода", "benefit", "прибыль", "profit", "польза", "advantage", "плюс");
        boolean opportunity = has(t, "opportunity cost", "альтернативная стоимость", "упущенная выгода", "что теряем", "от чего отказываемся", "next best", "лучшая альтернатива");
        boolean decision = has(t, "выбрать", "рекомендую", "стоит", "should choose", "go with", "план");
        return benefit && decision && !opportunity;
    }

    private static boolean singleOptionNoAlternative(String t) {
        boolean recommend = has(t, "рекомендую", "выбери", "choose this", "лучший вариант", "optimal");
        boolean alternative = has(t, "альтернатива", "alternative", "вместо этого", "другой вариант", "или можно");
        return recommend && !alternative;
    }

    private static boolean ignoredForgone(String t) {
        return multi(t,
                new String[]{"выгода", "benefit", "прибыль"},
                new String[]{"не важно что теряем", "ignore what we give up", "не учитываем альтернативы"});
    }

    private static boolean bothNoOpportunityCost(String a, String b) {
        boolean aNone = !has(a, "opportunity cost", "альтернативная стоимость", "упущенн", "что теряем", "next best");
        boolean bNone = !has(b, "opportunity cost", "альтернативная стоимость", "упущенн", "что теряем", "next best");
        boolean relevant = has(a + b, "выбор", "решение", "recommend", "рекоменд", "план");
        return aNone && bNone && relevant;
    }
}
