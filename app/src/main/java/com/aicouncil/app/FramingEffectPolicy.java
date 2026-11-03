package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local framing-effect signals.
 * Flags when the same information or decision is presented in gain vs loss frames,
 * or relative vs absolute terms, potentially leading to different recommendations without acknowledging the frame.
 * Hypotheses requiring context, not automatic verdicts. The coordinator must not treat them as definitive.
 */
public final class FramingEffectPolicy {
    private static final int MAX_SIGNALS = 3;

    private FramingEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, gainLossFrameClash(a, b),
                "эффект фрейминга: один ответ фреймит как выигрыш/приобретение, другой как потерю/риск без признания влияния фрейма");
        add(conflicts, initial, relativeAbsoluteClash(a, b),
                "эффект фрейминга: относительные проценты против абсолютных величин без сравнения базы");
        add(conflicts, initial, ignoresFrame(a) || ignoresFrame(b),
                "вывод зависит от фрейма (выигрыш/потеря или %/абсолют) без проверки альтернативной формулировки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "framing effect (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
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

    private static boolean gainFrame(String t) {
        return has(t, "выигрыш", "приобретение", "спасено", "получите", "gain", "save", "benefit from", "выгода");
    }

    private static boolean lossFrame(String t) {
        return has(t, "потеря", "риск потерять", "умрет", "потеряете", "loss", "die", "risk of", "убыток");
    }

    private static boolean gainLossFrameClash(String a, String b) {
        boolean aGain = gainFrame(a) && !lossFrame(a);
        boolean aLoss = lossFrame(a) && !gainFrame(a);
        boolean bGain = gainFrame(b) && !lossFrame(b);
        boolean bLoss = lossFrame(b) && !gainFrame(b);
        return (aGain && bLoss) || (aLoss && bGain);
    }

    private static boolean relativeFrame(String t) {
        return has(t, "%", "процент", "относительно", "percent", "relative") && has(t, "лучше", "хуже", "выше", "ниже");
    }

    private static boolean absoluteFrame(String t) {
        return has(t, "человек", "случаев", "абсолютно", "people", "cases", "absolute") && !has(t, "%", "процент");
    }

    private static boolean relativeAbsoluteClash(String a, String b) {
        return (relativeFrame(a) && absoluteFrame(b)) || (absoluteFrame(a) && relativeFrame(b));
    }

    private static boolean ignoresFrame(String t) {
        boolean framed = gainFrame(t) || lossFrame(t) || relativeFrame(t);
        boolean acknowledges = has(t, "фрейм", "формулировка", "frame", "framing", "независимо от формы");
        return framed && !acknowledges && has(t, "следовательно", "рекомендую", "therefore", "recommend");
    }
}
