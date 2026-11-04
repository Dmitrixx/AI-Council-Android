package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local illusion-of-control signals.
 * Overestimating personal influence over random, external or system-level outcomes
 * is not evidence of control. A skill at one domain does not imply control over
 * unrelated variance. Hypotheses, not verdicts.
 */
public final class IllusionOfControlPolicy {
    private static final int MAX_SIGNALS = 2;

    private IllusionOfControlPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, claimsControlOverRandom(a) || claimsControlOverRandom(b),
                "иллюзия контроля: утверждение влияния на случайный или внешний исход без механизма");
        add(conflicts, initial, bothClaimPersonalMastery(a, b),
                "оба эксперта приписывают себе контроль над внешними факторами");
        add(conflicts, initial, skillTransfersToUnrelated(a) || skillTransfersToUnrelated(b),
                "навык в одной области переносится на контроль несвязанной вариативности");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "illusion of control (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean claimsControlOverRandom(String text) {
        return has(text,
                "я контролирую", "мы контролируем исход", "могу управлять удачей",
                "контролирую рынок", "управляю случаем", "моя воля решает",
                "i control the outcome", "i can control luck", "we control the market",
                "my decision controls the result")
                && !has(text, "механизм", "причинная цепочка", "контролируемые факторы",
                        "mechanism", "causal path", "controllable factors");
    }

    private static boolean bothClaimPersonalMastery(String a, String b) {
        return claimsControlOverRandom(a) && claimsControlOverRandom(b);
    }

    private static boolean skillTransfersToUnrelated(String text) {
        return has(text,
                "раз я хорош в", "раз я эксперт в", "мой опыт в", "since i am good at",
                "my expertise in")
                && has(text, "значит я контролирую", "значит успех гарантирован",
                        "so i control", "so success is guaranteed")
                && !has(text, "только в этой области", "не переносится", "domain-specific",
                        "does not transfer");
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
}
