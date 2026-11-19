package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local inversion (via negativa) signals.
 * Flags a plan that says only what to do and never how it fails or what to
 * avoid. Naming the gap is not itself an error. The coordinator cannot close
 * a recommendation that has no failure path.
 */
public final class InversionPolicy {
    private static final int MAX_SIGNALS = 2;

    private InversionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, planWithoutInversion(a) || planWithoutInversion(b),
                "inversion: план говорит только что делать и не называет, как он провалится или чего избежать");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают план без инверсии");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "inversion (possible signal, requires context): " + core;
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
                "inversion", "инверси", "via negativa",
                "как это провалится", "how this fails",
                "сценарий провала", "failure mode",
                "чего избежать", "what to avoid",
                "не делать:");
    }

    private static boolean planWithoutInversion(String t) {
        boolean plan = has(t,
                "план:", "action plan",
                "следующие шаги", "next steps",
                "надо сделать", "we should do",
                "рекомендую делать");
        boolean decisive = has(t,
                "поэтому делаем", "закрываем в пользу",
                "go ahead", "therefore proceed",
                "решение принято", "делаем это");
        return plan && decisive && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return planWithoutInversion(a) && planWithoutInversion(b);
    }
}
