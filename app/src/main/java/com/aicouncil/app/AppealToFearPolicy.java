package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local appeal-to-fear / argumentum ad metum policy.
 * Detects a conclusion that something is true, good, necessary, or preferable merely because it is scary,
 * threatening, or because failing to act would be catastrophic ("because it's terrifying it must be true",
 * "if we don't do this, disaster will follow", "fear proves the point").
 * Naming the error, or saying fear is not evidence, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class AppealToFearPolicy {
    private static final int MAX_SIGNALS = 2;

    private AppealToFearPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "appeal to fear: вывод из страха, угрозы или катастрофы без доказательств");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта опираются на страх как доказательство");
        add(conflicts, initial, hitA ^ hitB,
                "appeal to fear: один эксперт выводит тезис из страха или угрозы");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "appeal to fear (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "потому что это страшно — значит правда",
                "если не сделаем, будет катастрофа",
                "страх доказывает",
                "раз это пугает — значит нужно",
                "because it is terrifying it must be true",
                "if we don't act disaster will follow",
                "fear proves the point",
                "appeal to fear",
                "argumentum ad metum",
                "the threat itself shows it is necessary",
                "only fear of consequences makes it correct");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "appeal to fear",
                "argument from fear",
                "страх не доказательство",
                "угроза не делает тезис истинным",
                "fear is not evidence",
                "fear is not an argument",
                "не выводи истину из страха",
                "being scary does not make it true");
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
