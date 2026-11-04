package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local conjunction-fallacy signals.
 * A detailed story is not more probable than the broader class it belongs to.
 * Hypotheses, not verdicts.
 */
public final class ConjunctionFallacyPolicy {
    private static final int MAX_SIGNALS = 2;

    private ConjunctionFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, conjunctionAsProof(a) || conjunctionAsProof(b),
                "детальный сюжет объявлен вероятнее более широкого класса");
        add(conflicts, initial, bothConjunction(a, b),
                "оба эксперта ставят конъюнкцию выше одиночного события");
        add(conflicts, initial, fork(a, b),
                "развилка: сюжет вероятнее класса против явного предела конъюнкции");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "conjunction fallacy (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(36, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean conjunctionAsProof(String text) {
        return hasConjunctionVerdict(text) && !hasSeparation(text);
    }

    private static boolean bothConjunction(String a, String b) {
        return conjunctionAsProof(a) && conjunctionAsProof(b);
    }

    private static boolean fork(String a, String b) {
        return (conjunctionAsProof(a) && hasSeparation(b))
                || (conjunctionAsProof(b) && hasSeparation(a));
    }

    private static boolean hasConjunctionVerdict(String text) {
        return contains(text,
                "и то и другое вероятнее", "оба свойства вместе вероятнее",
                "детальный сценарий вероятнее", "детальный сюжет вероятнее",
                "конкретная история вероятнее общей", "конкретный сценарий вероятнее общего",
                "совпадение двух черт вероятнее", "и кассир, и активистка вероятнее",
                "conjunction is more probable", "the conjunction is likelier",
                "more likely to be both", "specific story is more likely than",
                "specific scenario is likelier than the general",
                "the story is more probable than the class");
    }

    private static boolean hasSeparation(String text) {
        return contains(text,
                "конъюнкция не может быть вероятнее", "детальный сюжет не вероятнее",
                "специфический сценарий не вероятнее общего", "p(a and b) не выше",
                "conjunction cannot be more probable", "a conjunction cannot exceed",
                "the story is not more probable than the class",
                "specific scenario is not likelier");
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
