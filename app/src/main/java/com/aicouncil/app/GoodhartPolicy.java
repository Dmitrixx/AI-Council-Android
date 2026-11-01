package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local Goodhart's Law and metric-gaming signals.
 * Flags when a decision, forecast or recommendation optimizes or targets a metric
 * (KPI, score, coverage, etc.) without explicit consideration that the measure
 * may cease to be a good proxy once it becomes the goal, or of gaming / distortion risks.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class GoodhartPolicy {
    private static final int MAX_SIGNALS = 5;

    private GoodhartPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, metricAsTargetWithoutProxyCheck(a) || metricAsTargetWithoutProxyCheck(b),
                "метрика как цель: оптимизация/таргет метрики без проверки, остаётся ли она хорошим прокси истинной цели");
        add(conflicts, initial, ignoredGamingRisk(a) || ignoredGamingRisk(b),
                "риск искажения/гейминга метрики: цель по числу без оценки, как её могут накрутить без улучшения истинного исхода");
        add(conflicts, initial, proxyDivergence(a) || proxyDivergence(b),
                "прокси расходится с целью: явная оптимизация показателя без связи с желаемым результатом");
        add(conflicts, initial, bothNoGoodhart(a, b),
                "оба без Goodhart: ни один эксперт не проверил, не становится ли метрика целью самой по себе");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "goodhart (possible signal, requires context): " + core;
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

    private static boolean metricAsTargetWithoutProxyCheck(String t) {
        boolean metric = has(t, "kpi", "метрика", "показатель", "metric", "score", "coverage", "охват", "количество", "цель по", "target", "целевой");
        boolean optimize = has(t, "оптимиз", "максимиз", "увеличить", "повысить", "optimize", "maximize", "increase", "достичь");
        boolean check = has(t, "goodhart", "прокси", "proxy", "искажение", "gaming", "гейминг", "истинная цель", "true goal", "цель сама по себе");
        return metric && optimize && !check;
    }

    private static boolean ignoredGamingRisk(String t) {
        return multi(t,
                new String[]{"метрика", "kpi", "score", "показатель", "target"},
                new String[]{"не важно как", "ignore how", "любым способом", "at any cost", "не смотреть на"});
    }

    private static boolean proxyDivergence(String t) {
        boolean proxy = has(t, "прокси", "proxy", "замещающий показатель", "surrogate");
        boolean diverge = has(t, "не соответствует", "diverges", "расходится", "не отражает", "does not reflect");
        return proxy && diverge;
    }

    private static boolean bothNoGoodhart(String a, String b) {
        boolean aNone = !has(a, "goodhart", "прокси", "proxy", "gaming", "гейминг", "искажение метрики");
        boolean bNone = !has(b, "goodhart", "прокси", "proxy", "gaming", "гейминг", "искажение метрики");
        boolean relevant = has(a + b, "kpi", "метрик", "score", "показател", "target", "оптимиз", "цель");
        return aNone && bNone && relevant;
    }
}
