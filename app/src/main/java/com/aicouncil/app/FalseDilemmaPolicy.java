package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local false-dilemma signals.
 * Two named options are not the whole choice set unless a third path was considered or ruled out.
 * Hypotheses requiring context, not verdicts. The coordinator must not average them away.
 */
public final class FalseDilemmaPolicy {
    private static final int MAX_SIGNALS = 3;

    private FalseDilemmaPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, exhaustiveBinary(a) || exhaustiveBinary(b),
                "ложная дилемма: выбор объявлен исчерпывающим из двух вариантов, третий путь или отказ не названы");
        add(conflicts, initial, bothAcceptBinary(a, b),
                "оба принимают рамку «только два варианта» и не оставляют статус-кво, пилот или иной шаг");
        add(conflicts, initial, forcedPair(a) || forcedPair(b),
                "принудительный выбор из двух без проверки, что промежуточный или обратимый шаг невозможен");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "false dilemma (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(36, core.length()));
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

    private static boolean opensChoice(String t) {
        return has(t,
                "третий вариант", "третьего пути", "оставить как есть", "статус-кво", "status quo",
                "пилот", "промежуточн", "другой шаг", "third option", "do nothing");
    }

    private static boolean binaryFrame(String t) {
        return has(t,
                "только два варианта", "третьего не дано", "либо так, либо никак",
                "других вариантов нет", "only two options", "no third option", "there is no alternative");
    }

    private static boolean exhaustiveBinary(String t) {
        return binaryFrame(t) && !opensChoice(t);
    }

    private static boolean bothAcceptBinary(String a, String b) {
        return binaryFrame(a) && binaryFrame(b) && !opensChoice(a) && !opensChoice(b);
    }

    private static boolean forcedPair(String t) {
        boolean forced = has(t, "одно из двух", "one of the two", "придётся выбрать из двух", "must pick one of two");
        boolean closed = has(t, "придётся", "must choose", "другого выхода нет");
        return forced && closed && !opensChoice(t);
    }
}
