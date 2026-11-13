package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local surrogate-endpoint / proxy-metric policy.
 * Flags a verdict that treats a convenient intermediate, proxy or surrogate
 * measure as the real decision-relevant outcome without independent evidence
 * that the proxy reliably tracks the true target (or an explicit note that the link is unproven).
 * Naming that gap is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class SurrogateEndpointPolicy {
    private static final int MAX_SIGNALS = 2;

    private SurrogateEndpointPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "surrogate endpoint: proxy/intermediate metric taken as the real outcome");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают выбор прокси-метрикой без проверки связи с целью");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "surrogate endpoint (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || hasLinkCheck(t)) return false;
        return hasProxyMarker(t) && closes(t);
    }

    private static boolean hasProxyMarker(String t) {
        return has(t,
                "суррогатный показатель",
                "прокси-метрика",
                "промежуточная метрика",
                "по этой метрике выбираем",
                "достаточно смотреть на",
                "surrogate endpoint",
                "proxy metric",
                "intermediate outcome",
                "we decide by the proxy",
                "the metric is enough",
                "as long as the kpi rises");
    }

    private static boolean closes(String t) {
        return has(t,
                "поэтому",
                "значит",
                "рекомендую",
                "вердикт",
                "выбираем",
                "следует выбрать",
                "therefore",
                "we should",
                "the choice is",
                "enough to decide");
    }

    private static boolean hasLinkCheck(String t) {
        return has(t,
                "связь с целью",
                "проверил что прокси отслеживает",
                "независимое обоснование что метрика соответствует",
                "прокси не доказано связано с исходом",
                "surrogate tracks the true endpoint",
                "proxy validated against",
                "link to the real outcome is unproven",
                "intermediate is not the target");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "surrogate endpoint",
                "proxy metric fallacy",
                "суррогатный показатель не исход",
                "прокси не заменяет цель",
                "intermediate metric is not the outcome");
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
