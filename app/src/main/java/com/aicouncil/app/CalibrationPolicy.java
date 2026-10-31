package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local calibration and overconfidence signals.
 * Flags when experts claim high or absolute confidence without an explicit residual-risk range
 * or when a single vivid case inflates certainty.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class CalibrationPolicy {
    private static final int MAX_SIGNALS = 5;

    private CalibrationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, highConfidenceNoRange(a) || highConfidenceNoRange(b),
                "высокая уверенность без residual-risk диапазона: заявлена высокая/абсолютная уверенность, но количественный residual risk отсутствует");
        add(conflicts, initial, certaintyWithUncertainty(a) || certaintyWithUncertainty(b),
                "иллюзия определённости при неопределённости: категоричный язык рядом с явными диапазонами или «возможно»");
        add(conflicts, initial, anecdoteInflation(a) || anecdoteInflation(b),
                "инфляция уверенности от анекдота: единичный яркий случай поднимает уверенность до «точно» без reference class");
        add(conflicts, initial, confidenceWithoutEvidenceDensity(a, b),
                "уверенность без опоры на evidence density: оба эксперта дают высокую уверенность при слабой количественной опоре");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "калибровка (возможный сигнал, требует контекста): " + core;
        for (String existing : conflicts) {
            if (existing.contains(core.substring(0, Math.min(48, core.length())))) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static boolean multi(String text, String[] groupA, String[] groupB) {
        return has(text, groupA) && has(text, groupB);
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean highConfidenceNoRange(String t) {
        boolean high = has(t, "уверен на 90", "90% уверен", "почти уверен", "абсолютно уверен",
                "high confidence", "very confident", "90 percent sure", "definitely");
        boolean range = has(t, "residual risk", "residual-risk", "диапазон риска", "0–", "1–",
                "confidence interval", "интервал уверенности");
        return high && !range;
    }

    private static boolean certaintyWithUncertainty(String t) {
        boolean certain = has(t, "точно", "определённо", "гарантированно", "безусловно",
                "definitely", "certainly", "guaranteed", "absolutely");
        boolean uncertain = has(t, "возможно", "может быть", "неопределён", "uncertain",
                "might", "could", "диапазон", "range", "примерно");
        return certain && uncertain;
    }

    private static boolean anecdoteInflation(String t) {
        return multi(t,
                new String[]{"один случай", "one case", "яркий пример", "анекдот", "один випадок"},
                new String[]{"значит точно", "therefore certain", "100%", "абсолютно", "точно так"});
    }

    private static boolean confidenceWithoutEvidenceDensity(String a, String b) {
        boolean aHigh = has(a, "уверен", "confident", "точно");
        boolean bHigh = has(b, "уверен", "confident", "точно");
        boolean weak = !has(a, "%", "процент", "meta", "rct", "систематич") &&
                       !has(b, "%", "процент", "meta", "rct", "систематич");
        return aHigh && bHigh && weak;
    }
}
