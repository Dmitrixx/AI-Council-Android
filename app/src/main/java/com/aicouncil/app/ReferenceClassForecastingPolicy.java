package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local reference-class forecasting / outside-view signals.
 * Flags a verdict that treats a specific forecast or success probability as decisive
 * without naming a reference class of similar past cases, a base rate from that class,
 * or an explicit note that the inside view is being used alone.
 * Naming the gap is not itself an error.
 * The coordinator cannot close a numeric forecast solely on the inside story.
 */
public final class ReferenceClassForecastingPolicy {
    private static final int MAX_SIGNALS = 2;

    private ReferenceClassForecastingPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesOnInsideView(a) || closesOnInsideView(b),
                "reference class forecasting: прогноз закрывает выбор без референс-класса похожих случаев или базовой частоты");
        add(conflicts, initial, closesOnInsideView(a) && closesOnInsideView(b),
                "оба эксперта закрывают прогноз только внутренним взглядом без outside view");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "reference class forecasting (possible signal, requires context): " + core;
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

    private static boolean accountsForReference(String t) {
        return has(t,
                "reference class", "референс-класс", "outside view", "внешний взгляд",
                "базовая частота похожих", "base rate of similar",
                "похожие проекты заканчивались", "similar projects completed",
                "историческая частота", "historical frequency");
    }

    private static boolean closesOnInsideView(String t) {
        boolean forecast = has(t,
                "вероятность успеха", "probability of success", "шанс завершения",
                "закончим к", "will finish by", "ожидаем доходность",
                "expected return", "прогнозирую");
        boolean conclude = has(t, "следовательно", "рекомендую", "therefore", "i recommend", "стоит выбрать", "делать");
        return forecast && conclude && !accountsForReference(t);
    }
}
