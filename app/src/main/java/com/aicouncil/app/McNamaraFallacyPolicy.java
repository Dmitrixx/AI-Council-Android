package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local McNamara fallacy / quantification bias policy.
 * Flags a verdict that treats only what is easily measurable as real or decisive,
 * ignoring important but hard-to-quantify factors, without acknowledging the gap.
 * Naming that error is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class McNamaraFallacyPolicy {
    private static final int MAX_SIGNALS = 2;

    private McNamaraFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "mcnamara / quantification: легко измеримое принято за всё важное без учёта неизмеримого");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают выбор только измеримым, игнорируя качественное");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "mcnamara fallacy (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || hasQualitative(t)) return false;
        return hasOnlyMeasurable(t) && closes(t);
    }

    private static boolean hasOnlyMeasurable(String t) {
        return has(t,
                "только цифры",
                "только метрика",
                "только измеримое",
                "что не измерить того не существует",
                "if you can't measure it it doesn't exist",
                "only numbers matter",
                "only metrics count",
                "what gets measured gets managed",
                "только количественное",
                "качественное не важно");
    }

    private static boolean closes(String t) {
        return has(t,
                "поэтому",
                "значит",
                "рекомендую",
                "вердикт",
                "вопрос закрыт",
                "выбираем",
                "therefore",
                "we should",
                "the choice is",
                "so we decide");
    }

    private static boolean hasQualitative(String t) {
        return has(t,
                "качественный",
                "неизмеримый",
                "сложно измерить",
                "субъективный",
                "qualitative",
                "hard to measure",
                "unmeasurable",
                "intangible",
                "учтено неизмеримое",
                "за пределами метрик");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "mcnamara",
                "макнамара",
                "quantification bias",
                "ошибка квантификации",
                "только измеримое не всё",
                "measurable is not all");
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
