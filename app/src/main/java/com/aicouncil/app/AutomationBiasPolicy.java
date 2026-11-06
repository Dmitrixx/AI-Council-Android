package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local automation bias signals.
 * Preferring or accepting a recommendation solely because it came from an AI, algorithm, model or automated system,
 * while dismissing contradictory human, independent or non-automated evidence without acknowledging the limits of the automation,
 * is a hypothesis, not evidence.
 * Distinct from authority bias (status of a person) and availability (ease of recall).
 * Coordinator must not treat "the AI said so" or "the model agrees" as settled without independent verification.
 */
public final class AutomationBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private AutomationBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "automation bias: вывод, что рекомендация верна потому что её дала AI/алгоритм/модель, без независимой проверки или признания ограничений автоматизации");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта принимают рекомендацию только потому что её подтвердила AI/модель, без калибровки на неавтоматические источники");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "automation bias (possible signal, requires context): " + core;
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
                "automation bias", "автоматизационная предвзятость",
                "ограничения модели", "model limitations",
                "независимая проверка", "independent verification",
                "модель может ошибаться", "AI can be wrong",
                "не доверять слепо", "do not blindly trust");
    }

    private static boolean naivePattern(String t) {
        boolean auto = has(t,
                "ии сказал", "ai said", "модель рекомендует", "the model recommends",
                "алгоритм решил", "algorithm decided", "это подтвердил ии",
                "chatgpt согласен", "ии прав", "только потому что ии",
                "автоматическая система рекомендует", "доверять ии",
                "the ai is right", "trust the model", "модель не ошибается");
        return auto && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
