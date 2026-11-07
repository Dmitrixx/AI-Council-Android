package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local focusing-illusion signals.
 * Overweighting a single salient factor (income, status, one metric, one risk, one benefit)
 * as if it dominated overall value, happiness, or decision quality, while ignoring
 * the broader set of factors, adaptation, and trade-offs, is a hypothesis, not evidence.
 * Distinct from affect heuristic (emotion) and scope-insensitivity (magnitude scaling).
 * Coordinator must not treat "this one thing is what matters" as settled without naming omitted factors.
 */
public final class FocusingIllusionPolicy {
    private static final int MAX_SIGNALS = 2;

    private FocusingIllusionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "focusing illusion: вывод, что один яркий фактор (доход, статус, одна метрика, один риск или выгода) определяет общую ценность/счастье/качество решения, без учёта остальных факторов, адаптации и компромиссов");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта переоценивают влияние одного фактора и игнорируют остальные");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "focusing illusion (possible signal, requires context): " + core;
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
                "focusing illusion", "иллюзия фокуса", "фокусирующая иллюзия",
                "one factor is not everything", "один фактор не всё",
                "other factors matter", "другие факторы тоже важны",
                "adaptation", "адаптация", "hedonic treadmill",
                "trade-offs", "компромиссы", "omitted factors", "упущенные факторы");
    }

    private static boolean naivePattern(String t) {
        boolean focus = has(t,
                "только это важно", "это единственное что имеет значение",
                "всё зависит от", "главное это", "only this matters",
                "this is what really matters", "the only thing that counts",
                "everything else is secondary", "остальное не важно",
                "если будет больше денег то", "higher income means happier",
                "этот показатель решает всё", "this metric decides everything",
                "игнорируя остальное", "ignoring everything else");
        return focus && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
