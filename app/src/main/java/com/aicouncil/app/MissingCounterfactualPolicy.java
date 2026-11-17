package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local missing-counterfactual signals.
 * Flags a verdict that treats one option as clearly superior without stating
 * what would happen under the next-best or rejected alternative (the counterfactual),
 * or without comparing the incremental outcome relative to that baseline.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on the preferred option without a counterfactual.
 */
public final class MissingCounterfactualPolicy {
    private static final int MAX_SIGNALS = 2;

    private MissingCounterfactualPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesWithoutCounterfactual(a) || closesWithoutCounterfactual(b),
                "missing counterfactual: вердикт закрывает выбор на предпочтительном варианте без описания исхода под альтернативой");
        add(conflicts, initial, closesWithoutCounterfactual(a) && closesWithoutCounterfactual(b),
                "оба эксперта закрывают без контрфактуального сравнения с следующим лучшим вариантом");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "missing counterfactual (possible signal, requires context): " + core;
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
                "counterfactual", "контрфактуал",
                "what would have happened", "что было бы под альтернативой",
                "incremental benefit over", "прирост относительно следующего лучшего",
                "compared to the rejected option", "сравнение с отвергнутым вариантом");
    }

    private static boolean closesWithoutCounterfactual(String t) {
        boolean closes = has(t,
                "поэтому выбираем", "значит лучше", "закрываем на",
                "therefore we choose", "so the better option is",
                "we prefer this because", "выбираем этот вариант");
        boolean mentionsCounter = has(t,
                "под альтернативой", "если бы выбрали другое",
                "counterfactual", "what if we did the other",
                "incremental over the next best", "относительно следующего лучшего варианта");
        return closes && !mentionsCounter && !acknowledges(t);
    }
}
