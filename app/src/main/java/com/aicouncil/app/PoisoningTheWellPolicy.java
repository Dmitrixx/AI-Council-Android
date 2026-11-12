package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local poisoning-the-well policy.
 * Flags a verdict that treats a source as already discredited by a preemptive
 * attack on character, motive or affiliation, so that later evidence from that
 * source is dismissed without independent examination.
 * Naming the move is not the error. Signals remain hypotheses that require context.
 */
public final class PoisoningTheWellPolicy {
    private static final int MAX_SIGNALS = 2;

    private PoisoningTheWellPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "poisoning the well: заранее дискредитированный источник закрыл доводы");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта отвергают довод из-за заранее атакованного источника");
        add(conflicts, initial, hitA ^ hitB,
                "poisoning the well: один эксперт заранее объявил источник негодным");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "poisoning the well (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "раз он заинтересован, его довод не считается",
                "он из той стороны, поэтому аргумент неверен",
                "заранее известно что источник необъективен",
                "его мотив плохой, значит вывод ложный",
                "раз связан с оппонентами, можно не слушать",
                "он продажный, поэтому факты не важны",
                "because they are biased their evidence is worthless",
                "the source is tainted so the claim fails",
                "we already know they are compromised",
                "their affiliation disproves the argument",
                "poisoned the well so ignore what follows",
                "once you know who funds them you can dismiss it");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "poisoning the well",
                "отравление колодца",
                "заранее дискредитированный источник не опровергает тезис",
                "мотив не отменяет проверку довода",
                "affiliation does not settle the claim",
                "a bad motive is not a refutation",
                "attack the argument not the well");
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
