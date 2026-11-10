package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local false-equivalence / apples-to-oranges policy.
 * Detects treating dissimilar things as if they were equivalent for the purpose of the argument
 * («это то же самое, что…», «ничем не отличается от…», «exactly the same as», «no different from»)
 * when relevant differences are ignored or denied without justification.
 * Naming the fallacy or noting the relevant difference is not the error.
 * Signals remain hypotheses that require context.
 */
public final class FalseEquivalencePolicy {
    private static final int MAX_SIGNALS = 2;

    private FalseEquivalencePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean eqA = equates(a);
        boolean eqB = equates(b);
        add(conflicts, initial, eqA || eqB,
                "false equivalence: уравнивание несходных вещей без учёта релевантных различий");
        add(conflicts, initial, eqA && eqB,
                "оба эксперта уравнивают несходные вещи как будто они эквивалентны");
        add(conflicts, initial, eqA ^ eqB,
                "false equivalence: один эксперт приравнивает несходные случаи");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "false equivalence (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    /** Treating dissimilar things as equivalent, without treating the name of the fallacy as the error. */
    private static boolean equates(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "это то же самое, что",
                "ничем не отличается от",
                "точно такое же, как",
                "эквивалентно тому, что",
                "нет разницы между",
                "одно и то же",
                "равносильно",
                "это идентично",
                "ничем не лучше и не хуже",
                "exactly the same as",
                "no different from",
                "equivalent to",
                "the same thing as",
                "identical to",
                "apples to oranges",
                "false equivalence",
                "treating them as equivalent",
                "no relevant difference between",
                "they're the same for all intents");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "false equivalence",
                "apples to oranges",
                "не эквивалентно",
                "релевантное различие",
                "relevant difference",
                "не приравнивай несходное",
                "это не одно и то же",
                "not the same thing",
                "relevant distinctions remain");
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
