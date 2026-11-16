package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local choice-overload / paradox-of-choice signals.
 * Flags a verdict that treats the mere number of options as decisive — either "too many so we cannot choose / defer" or "more options are always better" — without stating decision criteria, quality comparison across options, or that quantity alone does not determine value.
 * Naming the gap is not itself an error.
 * The coordinator cannot close (or refuse to close) on option count alone.
 */
public final class ChoiceOverloadPolicy {
    private static final int MAX_SIGNALS = 2;

    private ChoiceOverloadPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, overloadWithoutCriteria(a) || overloadWithoutCriteria(b),
                "choice overload: вердикт закрывает или откладывает выбор только из-за количества вариантов, без критериев решения или сравнения качества");
        add(conflicts, initial, bothOverload(a, b),
                "оба эксперта закрывают на числе вариантов без критериев или сравнения ценности");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "choice overload (possible signal, requires context): " + core;
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
                "choice overload", "парадокс выбора",
                "paradox of choice", "слишком много вариантов не решает",
                "criteria for selection", "критерии выбора",
                "quality comparison", "сравнение качества вариантов",
                "number of options is not decisive", "количество не решает");
    }

    private static boolean overloadWithoutCriteria(String t) {
        boolean overloadLang = has(t,
                "слишком много вариантов", "не можем выбрать из-за количества",
                "больше вариантов лучше", "too many choices",
                "paradox of choice so defer", "cannot decide because of options",
                "количество вариантов решает", "more is better just because more",
                "отложим из-за выбора", "analysis paralysis from options");
        boolean decisive = has(t,
                "поэтому не выбираем", "значит откладываем",
                "закрываем без выбора", "recommend not choosing",
                "therefore we defer", "so we cannot pick",
                "этого достаточно для отказа", "решает число опций");
        boolean mentionsCriteria = has(t,
                "критерии выбора", "decision criteria",
                "сравнение качества", "quality of options",
                "по ценности каждого", "value comparison",
                "независимо от количества", "regardless of count");
        return overloadLang && decisive && !mentionsCriteria && !acknowledges(t);
    }

    private static boolean bothOverload(String a, String b) {
        return overloadWithoutCriteria(a) && overloadWithoutCriteria(b);
    }
}
