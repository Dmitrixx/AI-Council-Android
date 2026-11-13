package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local practical-significance policy.
 * Flags a verdict that treats statistical significance or a small p-value as
 * enough to choose, without an effect size, a practical threshold, or an
 * explicit statement that the magnitude is unknown.
 * Naming that error is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class PracticalSignificancePolicy {
    private static final int MAX_SIGNALS = 2;

    private PracticalSignificancePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "practical significance: статистическая значимость принята за решение без размера эффекта");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают выбор по p-значению без практической величины");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "practical significance (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || hasMagnitude(t)) return false;
        return hasSignificance(t) && closes(t);
    }

    private static boolean hasSignificance(String t) {
        return has(t,
                "статистически значим",
                "статзначим",
                "statistically significant",
                "p < 0.05",
                "p<0.05",
                "p < 0.01",
                "p<0.01",
                "p-value",
                "p value",
                "достигнута значимость",
                "различие значимо",
                "значимость достаточна",
                "reached significance",
                "significant at the 5%");
    }

    private static boolean closes(String t) {
        return has(t,
                "поэтому",
                "значит",
                "рекомендую",
                "вердикт",
                "вопрос закрыт",
                "выбираем",
                "следует выбрать",
                "therefore",
                "we should",
                "the choice is",
                "so we",
                "enough to decide",
                "достаточно, чтобы");
    }

    private static boolean hasMagnitude(String t) {
        return has(t,
                "размер эффекта",
                "величина эффекта",
                "effect size",
                "cohen",
                "практическая значим",
                "практически значим",
                "практический порог",
                "practically significant",
                "practically meaningful",
                "practical significance",
                "минимально важн",
                "mcid",
                "nnt",
                "абсолютный риск",
                "absolute risk",
                "magnitude",
                "процентных пункт",
                "величина неизвестна",
                "magnitude is unknown",
                "эффект мал",
                "эффект велик",
                "small effect",
                "large effect");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "practical significance",
                "практическая значимость не",
                "статистическая значимость не практическая",
                "statistical significance is not",
                "significance is not a decision",
                "p-значение не решение",
                "p-value is not a decision");
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
