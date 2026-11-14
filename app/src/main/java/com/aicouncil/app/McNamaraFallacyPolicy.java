package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local McNamara fallacy / quantitative bias signals.
 * Flags a verdict that treats only what is easy to measure (counts, rates, scores)
 * as decisive while dismissing or ignoring hard-to-quantify factors, qualitative
 * evidence, or the risk that the metric does not capture the goal.
 * Naming the gap is not itself an error.
 * The coordinator must not close on the measurable proxy alone.
 */
public final class McNamaraFallacyPolicy {
    private static final int MAX_SIGNALS = 2;

    private McNamaraFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, mcNamara(a) || mcNamara(b),
                "mcnamara: вердикт закрывает выбор тем, что «если нельзя посчитать — не важно» или опирается только на измеримые метрики, игнорируя качественные факторы или риск, что метрика не отражает цель");
        add(conflicts, initial, bothMcNamara(a, b),
                "оба эксперта закрывают на том, что измеримое важнее неизмеримого без независимой проверки прокси");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "mcnamara (possible signal, requires context): " + core;
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
                "mcnamara fallacy", "эффект макнамары",
                "quantitative bias", "количественный уклон",
                "what gets measured", "что измеряется",
                "metric may not capture", "метрика может не отражать",
                "qualitative factors", "качественные факторы");
    }

    private static boolean mcNamara(String t) {
        boolean measurable = has(t,
                "только цифры", "если не можно посчитать",
                "неизмеримое не важно", "только метрики",
                "only numbers matter", "if you can't measure it",
                "unmeasurable doesn't count", "only the metrics");
        boolean dismisses = has(t,
                "качество не важно", "ощущения не считаются",
                "quality doesn't matter", "feelings don't count",
                "ignore the soft factors", "мягкие факторы не решают");
        return (measurable || dismisses) && !acknowledges(t);
    }

    private static boolean bothMcNamara(String a, String b) {
        return mcNamara(a) && mcNamara(b);
    }
}
