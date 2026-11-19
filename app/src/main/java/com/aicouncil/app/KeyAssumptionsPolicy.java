package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local key-assumptions signals.
 * Flags a verdict that treats a proposed action or policy as clearly preferable
 * without stating the key assumptions on which that preference depends
 * (e.g. about costs, probabilities, agent responses, external conditions,
 * model validity, or data quality). Naming the assumptions is not itself an error.
 * The coordinator cannot close solely on a recommendation that hides its load-bearing assumptions.
 */
public final class KeyAssumptionsPolicy {
    private static final int MAX_SIGNALS = 2;

    private KeyAssumptionsPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, strongRecommendWithoutAssumptions(a) || strongRecommendWithoutAssumptions(b),
                "key assumptions: вердикт рекомендует действие как ясно лучшее, не называя ключевые предпосылки, от которых оно зависит");
        add(conflicts, initial, strongRecommendWithoutAssumptions(a) && strongRecommendWithoutAssumptions(b),
                "оба эксперта закрывают без перечня ключевых допущений");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "key assumptions (possible signal, requires context): " + core;
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
                "assumption", "допущение", "предпосылка",
                "ключевое допущение", "key assumption",
                "если предполагать", "assuming that",
                "основано на допущении", "depends on the assumption",
                "при условии что", "provided that");
    }

    private static boolean strongRecommendWithoutAssumptions(String t) {
        boolean decisive = has(t,
                "рекомендую", "следует сделать", "нужно делать",
                "лучшее решение", "clearly better", "should do",
                "выбираем", "закрываем на", "поэтому делаем",
                "decision is to", "we should proceed");
        boolean strong = has(t,
                "ясно", "однозначно", "бесспорно",
                "clearly superior", "no doubt", "obviously better",
                "нет сомнений", "единственный правильный");
        return (decisive && strong) && !acknowledges(t);
    }
}
