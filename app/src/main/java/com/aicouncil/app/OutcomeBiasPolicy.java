package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local outcome-bias signals.
 * Flags when a verdict treats the quality of a decision as determined solely by its outcome
 * (success or failure) without evaluating the decision process, information available at the time,
 * or base rates of success.
 * Hypotheses requiring context, not verdicts. The coordinator must not average them.
 */
public final class OutcomeBiasPolicy {
    private static final int MAX_SIGNALS = 3;

    private OutcomeBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, outcomeDeterminesQuality(a) || outcomeDeterminesQuality(b),
                "outcome bias: качество решения оценивается только по исходу без оценки процесса и информации на момент решения");
        add(conflicts, initial, bothJudgeByOutcome(a, b),
                "оба оценивают прошлые решения только по результату, игнорируя качество процесса и доступную тогда информацию");
        add(conflicts, initial, successImpliesGoodProcess(a) || successImpliesGoodProcess(b),
                "успех или провал трактуется как доказательство хорошего/плохого процесса без оценки альтернатив и базовой частоты");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "outcome bias (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(38, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (affirmed(text, word)) return true;
        return false;
    }

    private static boolean affirmed(String text, String phrase) {
        int from = 0;
        while (from < text.length()) {
            int i = text.indexOf(phrase, from);
            if (i < 0) return false;
            int start = Math.max(0, i - 4);
            String prefix = text.substring(start, i);
            if (!prefix.endsWith("не ") && !prefix.endsWith("not ")) return true;
            from = i + Math.max(1, phrase.length());
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean accountsForProcess(String t) {
        return has(t,
                "качество процесса", "информация на момент", "decision process", "available information",
                "базовая частота успеха", "ex ante", "до результата", "процесс решения");
    }

    private static boolean outcomeDeterminesQuality(String t) {
        boolean outcomeFocus = has(t,
                "решение было хорошим потому что успешно", "плохое решение потому что провалилось",
                "good decision because it worked", "bad decision since it failed", "outcome proves");
        boolean conclude = has(t, "значит", "следовательно", "рекомендую", "стоит делать так", "therefore", "i recommend");
        return outcomeFocus && conclude && !accountsForProcess(t);
    }

    private static boolean bothJudgeByOutcome(String a, String b) {
        return outcomeDeterminesQuality(a) && outcomeDeterminesQuality(b);
    }

    private static boolean successImpliesGoodProcess(String t) {
        boolean success = has(t, "успешный исход значит хорошее решение", "failed so bad process", "it worked so it was right");
        boolean noProcess = !accountsForProcess(t);
        return success && noProcess;
    }
}
