package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local optimism bias signals.
 * Systematically overestimating the probability of positive outcomes and underestimating
 * negative ones (especially for oneself, one's plan, or preferred option) without
 * acknowledging base rates, downside, or independent calibration is a hypothesis,
 * not evidence. Distinct from planning fallacy (time/cost), overconfidence (certainty language),
 * and present bias.
 * Coordinator must not treat "it will work out" or "we will succeed" as settled.
 */
public final class OptimismBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private OptimismBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "optimism bias: переоценка вероятности успеха / хорошего исхода и недооценка рисков без базовой частоты или калибровки");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта заявляют, что «всё получится» / «риски малы» без независимой оценки вероятностей");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "optimism bias (возможный сигнал, требует контекста): " + core;
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
                "optimism bias", "оптимистическое смещение",
                "базовая частота неудач", "base rate of failure",
                "возможны и плохие исходы", "downside is real",
                "не гарантировано", "not guaranteed",
                "нужна независимая оценка вероятности", "need independent probability estimate",
                "исторически такие планы часто проваливаются", "historically such plans often fail");
    }

    private static boolean naivePattern(String t) {
        boolean positiveOverestimate = has(t,
                "всё получится", "обязательно сработает", "успех гарантирован",
                "мы справимся", "риски минимальны", "почти нет шансов провала",
                "it will work out", "we will succeed", "success is guaranteed",
                "risks are minimal", "almost no chance of failure",
                "это точно удастся", "failure is unlikely");
        boolean dismissesDownside = has(t,
                "плохого не случится", "провала не будет", "не стоит волноваться о рисках",
                "nothing bad will happen", "failure won't occur",
                "don't worry about the downside"
        ) && !has(t, "но возможны", "but possible");

        return (positiveOverestimate || dismissesDownside) && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
