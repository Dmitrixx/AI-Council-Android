package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local sunk-cost fallacy signals.
 * Flags when a decision, forecast or recommendation continues a course of action
 * primarily because of resources already invested (time, money, effort), without
 * explicit focus on future incremental costs and benefits only.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class SunkCostPolicy {
    private static final int MAX_SIGNALS = 5;

    private SunkCostPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, continuesBecauseAlreadyInvested(a) || continuesBecauseAlreadyInvested(b),
                "sunk cost: продолжение из-за уже вложенных ресурсов без фокуса только на будущих затратах/выгодах");
        add(conflicts, initial, pastInvestmentJustifiesFuture(a) || pastInvestmentJustifiesFuture(b),
                "уже потрачено оправдывает продолжение: прошлые инвестиции используются как аргумент продолжать");
        add(conflicts, initial, ignoredFutureOnly(a) || ignoredFutureOnly(b),
                "игнор будущего: решение опирается на sunk cost, а не на маржинальные будущие последствия");
        add(conflicts, initial, bothNoSunkCostCheck(a, b),
                "оба без проверки sunk cost: ни один эксперт не отделил уже понесённые затраты от будущих");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "sunk cost (possible signal, requires context): " + core;
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

    private static boolean continuesBecauseAlreadyInvested(String t) {
        boolean sunk = has(t, "уже вложил", "уже потратил", "уже инвестировал", "sunk cost", "уже вложено", "past investment", "already spent", "уже потерял", "уже потрачено");
        boolean continue_ = has(t, "продолжать", "не бросать", "довести до конца", "continue", "keep going", "не останавливаться", "завершить", "finish what started");
        boolean futureCheck = has(t, "будущ", "future cost", "маржинальн", "incremental", "только вперёд", "going forward", "от сегодня");
        return sunk && continue_ && !futureCheck;
    }

    private static boolean pastInvestmentJustifiesFuture(String t) {
        return multi(t,
                new String[]{"уже", "already", "прошл", "past", "вложен", "spent", "инвестировал"},
                new String[]{"поэтому продолжать", "therefore continue", "нельзя бросать", "can't abandon", "оправдывает", "justifies continuing"});
    }

    private static boolean ignoredFutureOnly(String t) {
        boolean sunk = has(t, "sunk cost", "уже потрачено", "уже вложено", "past cost");
        boolean noFuture = !has(t, "будущие затраты", "future benefits", "только вперёд", "marginal", "от этого момента");
        boolean decision = has(t, "решение", "продолжать", "recommend", "стоит продолжить");
        return sunk && noFuture && decision;
    }

    private static boolean bothNoSunkCostCheck(String a, String b) {
        boolean aNone = !has(a, "sunk cost", "уже вложен", "уже потрачен", "past investment", "маржинальн", "будущие затраты");
        boolean bNone = !has(b, "sunk cost", "уже вложен", "уже потрачен", "past investment", "маржинальн", "будущие затраты");
        boolean relevant = has(a + b, "продолжать", "continue", "инвестиц", "проект", "курс", "не бросать", "довести");
        return aNone && bNone && relevant;
    }
}
