package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local second-order thinking signals.
 * A first-order effect (immediate benefit or cost) is not the full story:
 * downstream consequences, feedback loops, adaptation and second-order effects matter.
 * Hypotheses, not verdicts.
 */
public final class SecondOrderThinkingPolicy {
    private static final int MAX_SIGNALS = 2;

    private SecondOrderThinkingPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, firstOrderOnly(a) || firstOrderOnly(b),
                "рекомендация опирается только на прямой эффект, без вторых последствий или адаптации");
        add(conflicts, initial, bothFirstOrderOnly(a, b),
                "оба эксперта закрывают вывод первым порядком, игнорируя downstream effects");
        add(conflicts, initial, fork(a, b),
                "развилка: вердикт из прямого эффекта против явного учёта вторых порядков");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "second-order thinking (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(36, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean firstOrderOnly(String text) {
        return hasFirstOrderVerdict(text) && !hasSecondOrder(text);
    }

    private static boolean bothFirstOrderOnly(String a, String b) {
        return firstOrderOnly(a) && firstOrderOnly(b);
    }

    private static boolean fork(String a, String b) {
        return (firstOrderOnly(a) && hasSecondOrder(b))
                || (firstOrderOnly(b) && hasSecondOrder(a));
    }

    private static boolean hasFirstOrderVerdict(String text) {
        return contains(text,
                "прямо выгодно", "немедленная выгода", "сразу окупится",
                "прямой эффект положительный", "first-order benefit",
                "immediate gain outweighs", "direct effect is positive",
                "the immediate result is good enough", "short-term gain decides",
                "только прямой результат", "достаточно первого порядка");
    }

    private static boolean hasSecondOrder(String text) {
        return contains(text,
                "второй порядок", "downstream", "вторые последствия",
                "адаптация", "feedback loop", "побочные эффекты",
                "что будет потом", "последствия последствий",
                "second-order effect", "second order", "what happens next",
                "adaptation", "unintended consequences", "long-term feedback");
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
