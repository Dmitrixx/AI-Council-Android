package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local outside-view and reference-class signals.
 * Flags when experts give specific predictions or recommendations without
 * anchoring to a reference class of similar cases, historical base rates,
 * or explicit outside view, or when one expert uses only inside view.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class OutsideViewPolicy {
    private static final int MAX_SIGNALS = 5;

    private OutsideViewPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, predictionWithoutReference(a) || predictionWithoutReference(b),
                "прогноз без reference class: конкретный прогноз/оценка даны без явного сравнения с классом похожих случаев или исторической базовой частоты");
        add(conflicts, initial, insideViewOnly(a, b),
                "только inside view: оба эксперта рассуждают от деталей этого случая, не упоминая outside view, base rate или аналоги");
        add(conflicts, initial, uniqueCaseException(a) || uniqueCaseException(b),
                "исключение без критерия: случай объявлен уникальным/особым без явного отличия от reference class");
        add(conflicts, initial, missingHistoricalAnchor(a) || missingHistoricalAnchor(b),
                "нет исторического якоря: решение/прогноз без упоминания похожих прошлых исходов или статистики класса");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "outside view (возможный сигнал, требует контекста): " + core;
        for (String existing : conflicts) {
            if (existing.contains(core.substring(0, Math.min(48, core.length())))) return;
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

    private static boolean predictionWithoutReference(String t) {
        boolean prediction = has(t, "прогноз", "ожидаю", "вероятно будет", "forecast", "predict", "likely to",
                "оценка", "estimate", "шанс", "вероятность");
        boolean reference = has(t, "reference class", "класс похожих", "похожие случаи", "исторически",
                "base rate", "базовая частота", "outside view", "аналоги", "статистика класса",
                "в прошлом такие");
        return prediction && !reference;
    }

    private static boolean insideViewOnly(String a, String b) {
        boolean aInside = has(a, "этот случай", "в данном случае", "this case", "here", "конкретно здесь")
                && !has(a, "outside view", "reference class", "базовая", "похожие", "исторически");
        boolean bInside = has(b, "этот случай", "в данном случае", "this case", "here", "конкретно здесь")
                && !has(b, "outside view", "reference class", "базовая", "похожие", "исторически");
        return aInside && bInside;
    }

    private static boolean uniqueCaseException(String t) {
        return multi(t,
                new String[]{"этот случай уникален", "this case is unique", "особый случай", "исключение"},
                new String[]{"поэтому обычная статистика не", "therefore usual statistics do not",
                        "тому звичайна статистика не", "base rate не применим"});
    }

    private static boolean missingHistoricalAnchor(String t) {
        boolean decision = has(t, "рекомендую", "стоит выбрать", "plan", "решение", "should", "recommend");
        boolean historical = has(t, "в прошлом", "historically", "похожие проекты", "similar cases",
                "статистика", "base rate", "reference");
        return decision && !historical;
    }
}
