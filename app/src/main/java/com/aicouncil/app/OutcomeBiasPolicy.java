package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local outcome-bias signals.
 * Judging a decision solely by its outcome is not the same as judging the quality
 * of the process and information available at decision time. A good process can
 * produce a bad result, and a bad process can get lucky. Hypotheses, not verdicts.
 */
public final class OutcomeBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private OutcomeBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, outcomeAsProof(a) || outcomeAsProof(b),
                "итог используется как доказательство качества решения без оценки процесса");
        add(conflicts, initial, bothOutcomeOnly(a, b),
                "оба эксперта оценивают решение только по результату");
        add(conflicts, initial, fork(a, b),
                "развилка: оценка по исходу против оценки процесса и информации на момент выбора");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "outcome bias (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(36, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean outcomeAsProof(String text) {
        return hasOutcomeVerdict(text) && !hasProcessView(text);
    }

    private static boolean bothOutcomeOnly(String a, String b) {
        return outcomeAsProof(a) && outcomeAsProof(b);
    }

    private static boolean fork(String a, String b) {
        return (outcomeAsProof(a) && hasProcessView(b))
                || (outcomeAsProof(b) && hasProcessView(a));
    }

    private static boolean hasOutcomeVerdict(String text) {
        return contains(text,
                "раз сработало", "раз получилось", "раз провалилось", "раз не вышло",
                "успех доказывает", "провал доказывает", "итог доказывает",
                "раз выиграли", "раз проиграли", "результат показывает что решение",
                "оно сработало значит", "оно не сработало значит",
                "outcome proves", "result proves", "it worked so", "it failed so",
                "success proves", "failure proves", "because it worked",
                "because it failed", "the outcome shows the decision was");
    }

    private static boolean hasProcessView(String text) {
        return contains(text,
                "на момент решения", "при принятии решения", "информация на момент",
                "процесс решения", "качество процесса", "процесс был", "процесс решения был",
                "даже при хорошем процессе", "хороший процесс может", "плохой процесс может",
                "удача", "везение", "не везёт", "не повезло",
                "at the time of the decision", "information available then",
                "decision process", "process quality", "good process can fail",
                "bad process can succeed", "luck", "got lucky");
    }

    private static boolean contains(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
