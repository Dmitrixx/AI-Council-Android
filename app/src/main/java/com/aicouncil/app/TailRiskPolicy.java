package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local tail-risk and extreme-outcome signals.
 * Flags when forecasts or decisions discuss typical/expected outcomes
 * without explicit consideration of low-probability high-impact tails,
 * black swans, or fat-tailed distributions.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class TailRiskPolicy {
    private static final int MAX_SIGNALS = 5;

    private TailRiskPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, forecastWithoutTail(a) || forecastWithoutTail(b),
                "нет анализа хвостов: прогноз/оценка опираются на типичный исход без учёта редких высокоимпактных сценариев");
        add(conflicts, initial, ignoredBlackSwan(a) || ignoredBlackSwan(b),
                "чёрный лебедь проигнорирован: названа низкая вероятность, но не оценён ущерб или необратимость при реализации");
        add(conflicts, initial, normalOnlyAssumption(a) || normalOnlyAssumption(b),
                "только нормальное распределение: используется среднее/медиана без учёта fat tails или extreme value");
        add(conflicts, initial, bothNoTailRisk(a, b),
                "оба без tail risk: ни один эксперт не рассмотрел редкие но разрушительные исходы");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "tail risk (возможный сигнал, требует контекста): " + core;
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

    private static boolean forecastWithoutTail(String t) {
        boolean forecast = has(t, "прогноз", "ожидаю", "вероятность", "forecast", "predict", "likely", "expected");
        boolean typical = has(t, "типичный", "среднее", "median", "average", "обычно", "base case");
        boolean tail = has(t, "хвост", "tail", "чёрный лебедь", "black swan", "extreme", "редкий случай", "fat tail", "worst case");
        return forecast && typical && !tail;
    }

    private static boolean ignoredBlackSwan(String t) {
        return multi(t,
                new String[]{"низкая вероятность", "unlikely", "маловероятно", "rare"},
                new String[]{"не важно", "does not matter", "можно игнорировать", "ignore"});
    }

    private static boolean normalOnlyAssumption(String t) {
        boolean normal = has(t, "нормальное распределение", "normal distribution", "gaussian", "гаусс");
        boolean extreme = has(t, "fat tail", "heavy tail", "extreme value", "тяжёлый хвост", "power law");
        return normal && !extreme;
    }

    private static boolean bothNoTailRisk(String a, String b) {
        boolean aNone = !has(a, "хвост", "tail", "чёрный лебедь", "black swan", "extreme", "worst case");
        boolean bNone = !has(b, "хвост", "tail", "чёрный лебедь", "black swan", "extreme", "worst case");
        boolean relevant = has(a + b, "прогноз", "forecast", "риск", "risk", "решение", "decision");
        return aNone && bNone && relevant;
    }
}
