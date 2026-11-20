package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local scenario / stress-test check.
 * Flags a verdict that closes a decision on a single expected or base-case forecast
 * without naming alternative plausible scenarios, stress cases, or how robust the conclusion
 * is under different futures (best/base/worst, or key uncertainties).
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on one point forecast when residual uncertainty is material
 * and different scenarios would reverse or materially change the ranking.
 */
public final class ScenarioStressTestPolicy {
    private static final int MAX_SIGNALS = 2;

    private ScenarioStressTestPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesWithoutScenarios(a) || closesWithoutScenarios(b),
                "scenario stress-test: вердикт закрывает на одном базовом прогнозе без названия альтернативных сценариев, стресс-кейсов или робастности вывода при разных будущих");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на единственном сценарии, не проверив робастность");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "scenario stress-test (possible signal, requires context): " + core;
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
                "scenario", "сценарий", "стресс-тест", "stress test",
                "робастность", "robustness", "альтернативные будущие",
                "best case", "worst case", "base case",
                "лучший случай", "худший случай", "базовый сценарий",
                "чувствительность к сценариям", "scenario sensitivity");
    }

    private static boolean closesWithoutScenarios(String t) {
        boolean forecastLang = has(t,
                "ожидается", "expected", "прогноз", "forecast",
                "базовый сценарий показывает", "base case shows",
                "скорее всего", "most likely");
        boolean closing = has(t,
                "поэтому выбираем", "therefore we choose", "закрываем на этом прогнозе",
                "so the decision is clear", "однозначно стоит", "the choice is closed");
        return forecastLang && closing && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesWithoutScenarios(a) && closesWithoutScenarios(b);
    }
}
