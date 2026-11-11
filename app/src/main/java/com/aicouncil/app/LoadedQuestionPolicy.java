package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local loaded-question / complex-question policy.
 * Detects a conclusion that presupposes an unproven or contested claim inside the question itself
 * ("have you stopped doing X?", "why do you still believe the false claim that...").
 * Naming the error, or saying the question begs the question / loads the premise, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class LoadedQuestionPolicy {
    private static final int MAX_SIGNALS = 2;

    private LoadedQuestionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "loaded question: вывод из вопроса, который уже предполагает недоказанный тезис");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта опираются на нагруженный вопрос как на доказательство");
        add(conflicts, initial, hitA ^ hitB,
                "loaded question: один эксперт выводит тезис из вопроса с встроенной посылкой");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "loaded question (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "когда ты перестал",
                "почему ты всё ещё веришь",
                "have you stopped",
                "why do you still believe",
                "the question proves",
                "loaded question",
                "complex question",
                "begging the question by asking",
                "the presupposition makes it true");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "loaded question",
                "complex question",
                "нагруженный вопрос",
                "вопрос уже предполагает",
                "the question begs the question",
                "presupposition is not evidence",
                "не выводи из нагруженного вопроса",
                "asking does not prove the premise");
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
