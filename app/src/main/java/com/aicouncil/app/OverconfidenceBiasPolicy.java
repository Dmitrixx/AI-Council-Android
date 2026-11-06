package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local overconfidence / under-calibration signals.
 * Extreme certainty language ("definitely", "100%", "no doubt", "guaranteed") without
 * hedging, base-rate language, or an explicit calibration statement is a hypothesis,
 * not evidence that the claim is well-calibrated. Distinct from belief-bias (wanting
 * the conclusion) and from authority-bias. Coordinator must not average it away.
 */
public final class OverconfidenceBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private OverconfidenceBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "overconfidence: крайняя уверенность без калибровки, оговорок или базовой частоты");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта используют язык абсолютной уверенности без явной калибровки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "overconfidence (возможный сигнал, требует контекста): " + core;
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
                "overconfidence", "переоценка уверенности", "калибровк",
                "я не уверен на 100", "моя уверенность", "оценка уверенности",
                "confidence interval", "uncertainty", "неопределённость",
                "с вероятностью", "примерно с", "порядка",
                "base rate", "базовая частота", "априор",
                "i may be wrong", "я могу ошибаться", "возможна ошибка",
                "calibration", "well-calibrated", "poorly calibrated");
    }

    private static boolean extremeCertainty(String t) {
        return has(t,
                "однозначно", "безусловно", "гарантированно", "100%", "сто процентов",
                "нет сомнений", "без всяких сомнений", "совершенно точно",
                "definitely", "certainly", "absolutely certain", "guaranteed",
                "no doubt", "without a doubt", "beyond any doubt",
                "i am certain", "we can be sure", "it is certain that",
                "наверняка", "точно будет", "точно не",
                "impossible that", "невозможно, что", "исключено, что",
                "zero chance", "нулевой шанс", "никакого шанса");
    }

    private static boolean triggered(String t) {
        return extremeCertainty(t);
    }

    private static boolean ignores(String t) {
        return triggered(t) && !acknowledges(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
