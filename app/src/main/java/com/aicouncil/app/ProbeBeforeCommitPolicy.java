package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local probe-before-commit signals.
 * An irreversible step without a cheap reversible test that could flip the conclusion
 * is not a reason to act. A demand for more data without the observation that would
 * change the decision is not an argument. Hypotheses, not verdicts.
 */
public final class ProbeBeforeCommitPolicy {
    private static final int MAX_SIGNALS = 2;

    private ProbeBeforeCommitPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, hardCommit(a) || hardCommit(b),
                "необратимый шаг без дешёвой проверки, которая может перевернуть вывод");
        add(conflicts, initial, vagueData(a) || vagueData(b),
                "запрос данных без наблюдения, которое может перевернуть вывод");
        add(conflicts, initial, fork(a, b),
                "развилка: необратимое обязательство против обратимой проверки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "probe before commit (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(36, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean hardCommit(String text) {
        return hasIrreversible(text) && !hasProbe(text);
    }

    private static boolean vagueData(String text) {
        return hasVagueData(text) && !hasFlip(text) && !hasProbe(text);
    }

    private static boolean fork(String a, String b) {
        return (hardCommit(a) && hasProbe(b)) || (hardCommit(b) && hasProbe(a));
    }

    private static boolean hasIrreversible(String text) {
        return contains(text,
                "необратим", "точка невозврата", "точки невозврата",
                "нельзя откатить", "отката не будет", "нельзя отменить",
                "удалить навсегда", "навсегда удалить", "сжечь мосты",
                "irreversible", "can't undo", "cannot undo", "no way back",
                "burn the boats", "point of no return");
    }

    private static boolean hasProbe(String text) {
        return contains(text,
                "пилот", "пилотный", "пробный", "можно откатить", "план отката",
                "есть откат", "обратимый шаг", "обратимая проверка",
                "сначала проверить", "дешёвая проверка", "тестовый период",
                "pilot", "rollback", "can undo", "reversible", "cheap test");
    }

    private static boolean hasVagueData(String text) {
        return contains(text,
                "нужно больше данных", "больше данных", "ещё данных", "еще данных",
                "собрать информацию", "ещё исследований", "еще исследований",
                "недостаточно информации", "подождать данных",
                "need more data", "more research", "gather more information");
    }

    private static boolean hasFlip(String text) {
        return contains(text,
                "переверн", "изменит решение", "изменило бы", "меняет решение",
                "если тест покажет", "если окажется",
                "would flip", "would change the decision", "if the test shows");
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
