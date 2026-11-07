package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local reactive-devaluation signals.
 * Discounting or rejecting a proposal, idea or concession primarily because it comes from
 * "the other side", an opponent, a disliked source or an out-group ("if they offer it, it must be bad",
 * "this is their plan so it cannot be good", "anything from them is a trap") without independent
 * evaluation of the terms themselves is a hypothesis, not evidence.
 * Distinct from naive cynicism, not-invented-here, reactance and general source bias.
 * Coordinator must not treat source-based dismissal as settled without scoring the content on its merits.
 */
public final class ReactiveDevaluationPolicy {
    private static final int MAX_SIGNALS = 2;

    private ReactiveDevaluationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "reactive devaluation: обесценивание предложения потому что оно от «другой стороны» / оппонента / нежелательного источника без независимой оценки содержания");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта отвергают идею по источнику, а не по существу");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "reactive devaluation (possible signal, requires context): " + core;
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
                "reactive devaluation", "реактивная девальвация",
                "evaluate on merits", "оценивать по существу",
                "source-blind", "независимо от источника",
                "content first", "сначала содержание",
                "who offered is not the test", "кто предложил — не критерий",
                "score the terms", "оценить условия");
    }

    private static boolean naivePattern(String t) {
        boolean deval = has(t,
                "если они предлагают", "раз это от них",
                "их предложение", "это их план",
                "от оппонента", "от противной стороны",
                "if they offer it", "because they proposed",
                "their plan cannot be good", "anything from them",
                "must be a trap", "это ловушка",
                "нельзя принимать от них", "раз это их идея",
                "from the other side so",
                "opponent's proposal is automatically");
        return deval && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
