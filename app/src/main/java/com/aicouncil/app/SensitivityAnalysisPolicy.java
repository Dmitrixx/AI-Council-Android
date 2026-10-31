package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local sensitivity-analysis and assumption-fragility signals.
 * Flags when experts give recommendations or forecasts that depend on key assumptions
 * without explicitly testing how the conclusion changes if those assumptions shift,
 * or when robustness is claimed only one way.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class SensitivityAnalysisPolicy {
    private static final int MAX_SIGNALS = 5;

    private SensitivityAnalysisPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, decisionWithoutSensitivity(a) || decisionWithoutSensitivity(b),
                "нет анализа чувствительности: рекомендация/прогноз зависят от ключевых допущений, но не показано, как вывод меняется при их сдвиге");
        add(conflicts, initial, oneSidedRobustness(a) || oneSidedRobustness(b),
                "односторонняя устойчивость: проверена только одна сторона диапазона допущений или только благоприятный сдвиг");
        add(conflicts, initial, ignoredFragility(a) || ignoredFragility(b),
                "игнорируемая хрупкость: названы критичные допущения, но не оценено, насколько вывод ломается при их нарушении");
        add(conflicts, initial, bothNoSensitivity(a, b),
                "оба без sensitivity: ни один эксперт не провёл даже качественный анализ чувствительности ключевых параметров");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "sensitivity (возможный сигнал, требует контекста): " + core;
        for (String existing : conflicts) {
            if (existing.contains(core.substring(0, Math.min(46, core.length())))) return;
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

    private static boolean decisionWithoutSensitivity(String t) {
        boolean decision = has(t, "рекомендую", "стоит", "следует", "прогноз", "ожидаю", "recommend", "should", "forecast", "predict");
        boolean assumptions = has(t, "допущение", "предположение", "assumption", "если", "при условии", "assuming");
        boolean sensitivity = has(t, "чувствительност", "sensitivity", "если изменится", "if the assumption", "±", "плюс-минус", "what if", "что если");
        return decision && assumptions && !sensitivity;
    }

    private static boolean oneSidedRobustness(String t) {
        boolean checked = has(t, "проверено", "устойчив", "robust", "если больше", "if higher");
        boolean oneSide = has(t, "только вверх", "only upside", "только при увеличении", "не проверял уменьшение");
        return checked && oneSide;
    }

    private static boolean ignoredFragility(String t) {
        return multi(t,
                new String[]{"критичное допущение", "key assumption", "если это не так", "if this is wrong"},
                new String[]{"вывод всё равно", "conclusion still holds", "не важно", "does not matter"});
    }

    private static boolean bothNoSensitivity(String a, String b) {
        boolean aNone = !has(a, "чувствительност", "sensitivity", "what if", "что если", "±");
        boolean bNone = !has(b, "чувствительност", "sensitivity", "what if", "что если", "±");
        boolean relevant = has(a + b, "допущение", "assumption", "прогноз", "recommend");
        return aNone && bNone && relevant;
    }
}
