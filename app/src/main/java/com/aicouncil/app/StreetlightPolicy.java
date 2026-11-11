package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local streetlight / McNamara policy.
 * Detects a conclusion that only what is easy to measure is real or decisive
 * ("если нельзя измерить, этого нет", "only what we can measure is real",
 * "смотрим только под фонарём и там принимаем решение").
 * Naming the fallacy, or saying an easy metric is not the whole decision, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class StreetlightPolicy {
    private static final int MAX_SIGNALS = 2;

    private StreetlightPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "streetlight: решение сведено к тому, что легко измерить");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают выбор удобной метрикой");
        add(conflicts, initial, hitA ^ hitB,
                "streetlight: один эксперт считает неизмеримое несуществующим");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "streetlight (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "если нельзя измерить, этого нет",
                "считаем только то, что легко посчитать, остальное отбрасываем",
                "неизмеримое не существует",
                "only what we can measure is real",
                "if it cannot be measured it does not matter",
                "look only under the streetlight and decide from that",
                "смотрим только под фонарём и там принимаем решение",
                "качество не важно, смотрим только на kpi который легко снять");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "ошибка макнамары",
                "mcnamara fallacy",
                "streetlight effect",
                "не всё важное измеряется",
                "what matters is not only what is easy to count",
                "легко измерить не значит важно",
                "удобная метрика не заменяет важное");
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
