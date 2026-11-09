package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local burden-of-proof policy.
 * Detects improper shifts of the burden of proof («докажи, что я не прав»,
 * «вы не можете опровергнуть — значит верно», «prove me wrong», «absence of evidence
 * is evidence of absence» used as positive proof).
 * Naming the burden or demanding evidence is not the error; declaring the claim
 * true because the opponent has not disproved it is.
 * Signals remain hypotheses that require context.
 */
public final class BurdenOfProofPolicy {
    private static final int MAX_SIGNALS = 2;

    private BurdenOfProofPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, shifts(a) || shifts(b),
                "burden of proof: вывод держится на том, что оппонент не опроверг");
        boolean shiftA = shifts(a);
        boolean shiftB = shifts(b);
        add(conflicts, initial, shiftA && shiftB,
                "оба эксперта перекладывают бремя доказательства вместо обоснования");
        add(conflicts, initial, shiftA ^ shiftB,
                "burden of proof: один эксперт требует опровержения вместо аргументов");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "burden of proof (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    /** Improper shift: claim is treated as true because it has not been disproved. */
    private static boolean shifts(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "докажи, что я не прав",
                "докажите обратное",
                "вы не можете опровергнуть",
                "не можете опровергнуть — значит",
                "отсутствие опровержения",
                "prove me wrong",
                "you can't disprove",
                "can't disprove it so",
                "absence of evidence is evidence",
                "if you can't refute",
                "burden is on the critic",
                "бремя на критике",
                "пока не опровергнуто — верно");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "бремя доказательства",
                "burden of proof",
                "кто утверждает, тот и доказывает",
                "claimant bears the burden",
                "не перекладывай бремя",
                "absence of evidence is not evidence of absence");
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
