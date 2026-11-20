package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local feedback / second-order effects check.
 * Flags a verdict that treats an intervention as having only direct first-order effects
 * without considering feedback loops, adaptive responses of agents, second-order consequences,
 * or how the system will react and potentially reverse or amplify the intended outcome.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on first-order expected effects when feedback is material.
 */
public final class FeedbackEffectsPolicy {
    private static final int MAX_SIGNALS = 2;

    private FeedbackEffectsPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesWithoutFeedback(a) || closesWithoutFeedback(b),
                "feedback effects: вердикт закрывает на прямом эффекте первого порядка без учёта петель обратной связи, адаптивных реакций агентов или эффектов второго порядка");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на эффекте первого порядка, игнорируя системную реакцию и обратную связь");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "feedback effects (possible signal, requires context): " + core;
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
                "feedback", "обратная связь", "петля", "second-order", "второго порядка",
                "адаптивн", "adaptive response", "системная реакция", "system response",
                "goodhart", "кобра эффект", "cobra effect", "непреднамеренн");
    }

    private static boolean closesWithoutFeedback(String t) {
        boolean actionLang = has(t,
                "введём", "мы вводим", "действие даст", "the intervention will",
                "прямой эффект", "first-order effect", "сразу получим");
        boolean closing = has(t,
                "поэтому делаем", "therefore we implement", "закрываем на этом эффекте",
                "the outcome is clear", "однозначно улучшит");
        return actionLang && closing && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesWithoutFeedback(a) && closesWithoutFeedback(b);
    }
}
