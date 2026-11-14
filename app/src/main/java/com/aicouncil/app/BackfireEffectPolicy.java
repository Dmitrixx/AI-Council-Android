package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local backfire-effect / belief-polarization signals.
 * When presented with evidence that contradicts a prior belief, the belief is strengthened
 * rather than updated or held constant. Distinct from simple belief perseverance (clinging)
 * and confirmation bias (selective search).
 * Coordinator must not treat the strengthened prior as settled once contrary evidence is named.
 * Naming the gap is not itself an error.
 */
public final class BackfireEffectPolicy {
    private static final int MAX_SIGNALS = 2;

    private BackfireEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, strengthens(a) || strengthens(b),
                "backfire effect: противоречащие данные усиливают исходное убеждение («теперь я ещё больше уверен», «это только подтверждает мою правоту») вместо обновления или признания силы доказательств");
        add(conflicts, initial, bothStrengthen(a, b),
                "оба эксперта усиливают исходную позицию именно из-за названных противоречащих данных");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "backfire effect (possible signal, requires context): " + core;
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
                "backfire effect", "эффект обратного огня",
                "evidence strengthened the belief", "данные усилили убеждение",
                "update despite contrary", "обновить несмотря на",
                "acknowledge the strength of counter", "признать силу контраргументов");
    }

    private static boolean strengthens(String t) {
        boolean pattern = has(t,
                "теперь я ещё больше уверен",
                "это только подтверждает мою правоту",
                "вопреки этому я теперь точно знаю",
                "данные против только укрепляют мою позицию",
                "now I am even more convinced",
                "this only proves me right",
                "despite the evidence I am surer",
                "counterevidence strengthens my view",
                "the more they show against the more I believe",
                "чем больше против тем увереннее");
        return pattern && !acknowledges(t);
    }

    private static boolean bothStrengthen(String a, String b) {
        return strengthens(a) && strengthens(b);
    }
}
