package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local illusory truth effect signals.
 * Flags when a conclusion rests on repetition, familiarity from hearing something many times,
 * or "everyone has heard this" as evidence of truth without independent verification or new data.
 * Hypotheses requiring context, not automatic verdicts. Coordinator must not average them away.
 */
public final class IllusoryTruthBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private IllusoryTruthBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresVerification(a) || ignoresVerification(b),
                "illusory truth: повтор или знакомость принимаются за доказательство без независимой проверки");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта опираются на повторённое/знакомое утверждение и не требуют новых данных");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "illusory truth bias (возможный сигнал, требует контекста): " + core;
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

    private static boolean acknowledgesVerification(String t) {
        return has(t,
                "не потому что повторяли", "знакомость не доказательство", "illusory truth",
                "нужна независимая проверка", "новые данные", "не из-за того что слышал",
                "repetition is not evidence", "familiarity is not truth", "проверял независимо",
                "не полагаюсь на повтор");
    }

    private static boolean repetitionClaim(String t) {
        return has(t,
                "все уже слышали", "давно известно и повторяется", "слышали много раз",
                "все говорят это уже давно", "знакомо всем", "повторяли столько раз",
                "everyone has heard", "it's been said many times", "repeated so often",
                "так часто говорят что правда", "если все повторяют");
    }

    private static boolean ignoresVerification(String t) {
        return repetitionClaim(t) && !acknowledgesVerification(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignoresVerification(a) && ignoresVerification(b);
    }
}
