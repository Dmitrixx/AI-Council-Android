package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local close-quality gate.
 * A verdict that recommends, concludes, or declares the question closed
 * without naming an observation that would overturn it is a hypothesis, not a close.
 * Naming the missing kill-criterion is not itself an error.
 */
public final class KillCriterionPolicy {
    private static final int MAX_SIGNALS = 2;

    private KillCriterionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = closesWithoutKill(answerA);
        boolean hitB = closesWithoutKill(answerB);
        add(conflicts, initial, hitA || hitB,
                "kill criterion: вердикт закрывает вопрос без наблюдения, которое его отменит");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают вывод без критерия опровержения");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "kill criterion (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean closesWithoutKill(String text) {
        String lower = lower(text);
        if (lower.isEmpty() || acknowledges(lower)) return false;
        return closes(lower) && !hasKill(lower);
    }

    private static boolean closes(String t) {
        return has(t,
                "вердикт",
                "вопрос закрыт",
                "закрываю вопрос",
                "рекомендую",
                "итого:",
                "итог:",
                "therefore we should",
                "we should conclude",
                "final answer",
                "the question is closed");
    }

    private static boolean hasKill(String t) {
        return has(t,
                "критерий опровержения",
                "что изменило бы",
                "изменило бы вывод",
                "изменит вывод",
                "would change",
                "would overturn",
                "disconfirm",
                "falsif",
                "если окажется",
                "kill criterion",
                "наблюдение, которое",
                "observation that would");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "kill criterion",
                "без критерия опровержения",
                "вердикт без критерия",
                "missing kill-criterion",
                "does not close without a disconfirming observation");
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
