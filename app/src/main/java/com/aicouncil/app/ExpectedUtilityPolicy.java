package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local signals for expected utility / risk-reward balance.
 * Flags when experts discuss probabilistic decisions without explicit expected value
 * or when one side emphasizes upside and the other only downside without balancing.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class ExpectedUtilityPolicy {
    private static final int MAX_SIGNALS = 6;

    private ExpectedUtilityPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, missingEv(a) || missingEv(b),
                "ожидаемая полезность: вероятности и исходы названы, но EV/взвешенный итог не посчитан");
        add(conflicts, initial, asymmetricRisk(a, b),
                "асимметрия риска: один эксперт фокусируется на upside, другой только на downside без сравнения EV");
        add(conflicts, initial, qualitativeOnly(a) || qualitativeOnly(b),
                "качественный риск без диапазона: «высокий/низкий риск» без вероятности или residual range");
        add(conflicts, initial, certaintyIllusion(a) || certaintyIllusion(b),
                "иллюзия определённости: категоричный вывод при явных неопределённостях или диапазонах");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "ожидаемая полезность (возможный сигнал, требует контекста): " + core;
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

    private static boolean missingEv(String t) {
        boolean hasProb = has(t, "вероятност", "probability", "шанс", "chance", "%", "процент");
        boolean hasOutcome = has(t, "выигрыш", "потеря", "прибыль", "убыток", "payoff", "gain", "loss", "benefit", "cost");
        boolean hasEv = has(t, "ожидаем", "expected value", "ev ", "взвешен", "weighted");
        return hasProb && hasOutcome && !hasEv;
    }

    private static boolean asymmetricRisk(String a, String b) {
        boolean aUp = has(a, "выигрыш", "upside", "прибыль", "gain", "reward") && !has(a, "потеря", "downside", "риск", "risk", "убыток");
        boolean bDown = has(b, "потеря", "downside", "риск", "risk", "убыток") && !has(b, "выигрыш", "upside", "прибыль", "gain");
        boolean bUp = has(b, "выигрыш", "upside", "прибыль", "gain", "reward") && !has(b, "потеря", "downside", "риск", "risk", "убыток");
        boolean aDown = has(a, "потеря", "downside", "риск", "risk", "убыток") && !has(a, "выигрыш", "upside", "прибыль", "gain");
        return (aUp && bDown) || (bUp && aDown);
    }

    private static boolean qualitativeOnly(String t) {
        boolean qualitative = has(t, "высокий риск", "низкий риск", "большой риск", "малый риск",
                "high risk", "low risk", "significant risk", "незначительный риск");
        boolean quantitative = has(t, "%", "процент", "0.", "1–", "диапазон", "range", "вероятность 0");
        return qualitative && !quantitative;
    }

    private static boolean certaintyIllusion(String t) {
        boolean certain = has(t, "точно", "определённо", "гарантированно", "безусловно", "definitely", "certainly", "guaranteed");
        boolean uncertain = has(t, "возможно", "может быть", "неопределён", "uncertain", "might", "could", "диапазон");
        return certain && uncertain;
    }
}
