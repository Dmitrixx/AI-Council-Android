package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local tu quoque / you too / appeal to hypocrisy policy.
 * Detects dismissing a claim because the speaker does not practice it or is hypocritical
 * («а сам тоже так делаешь», «вы тоже грешны», «tu quoque», «you do it too»).
 * Pointing out hypocrisy as a side note is not automatically the fallacy if the claim is still addressed.
 * Signals remain hypotheses that require context.
 */
public final class TuQuoquePolicy {
    private static final int MAX_SIGNALS = 2;

    private TuQuoquePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "tu quoque: отклонение из-за лицемерия / а сам так же");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта отклоняют аргумент через лицемерие");
        add(conflicts, initial, hitA ^ hitB,
                "tu quoque: один эксперт опирается на «а ты сам»");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "tu quoque (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "а сам тоже так делаешь",
                "вы сами так поступаете",
                "а ты сам грешен",
                "лицемерие опровергает аргумент",
                "tu quoque",
                "you do it too",
                "hypocrisy invalidates the claim",
                "practice what you preach so your point is void",
                "а сам такой же");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "tu quoque",
                "лицемерие не опровергает тезис",
                "hypocrisy does not refute the argument",
                "даже если сам так делает аргумент может быть верным");
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
