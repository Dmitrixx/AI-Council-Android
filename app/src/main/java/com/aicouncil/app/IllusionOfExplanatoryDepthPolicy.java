package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local illusion of explanatory depth policy.
 * Flags a verdict that treats a fluent, high-level description or the feeling of understanding
 * a complex system, mechanism, or process as sufficient evidence that the details are known
 * and the conclusion is solid, without independent checks of causal steps, intermediate variables,
 * or failure modes. Naming the error is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class IllusionOfExplanatoryDepthPolicy {
    private static final int MAX_SIGNALS = 2;

    private IllusionOfExplanatoryDepthPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "illusion of explanatory depth: чувство понимания или беглый обзор принимается за знание механизма без проверки шагов");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта опираются на поверхностное объяснение как на достаточное основание");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "illusion of explanatory depth (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "я понимаю как это работает поэтому",
                "ясно как устроено значит",
                "это просто поэтому можно закрыть",
                "i understand the mechanism therefore",
                "it is clear how it works so we can conclude",
                "high-level description is enough to decide",
                "since I can explain it at a high level the details must be solid");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "illusion of explanatory depth",
                "поверхностное понимание не достаточно",
                "feeling of understanding is not knowledge of the mechanism",
                "explanatory depth requires checking intermediate steps",
                "я не проверял детали механизма");
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
