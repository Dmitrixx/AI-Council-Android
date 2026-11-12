package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local data-dredging / garden-of-forking-paths policy.
 * Flags a verdict that treats a pattern found after searching many hypotheses,
 * subgroups or model specifications without pre-registration or correction as
 * confirmation. Naming the error is not itself an error. Signals remain hypotheses
 * that require context.
 */
public final class DataDredgingPolicy {
    private static final int MAX_SIGNALS = 2;

    private DataDredgingPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "data dredging: паттерн найден после перебора гипотез закрыл вопрос без коррекции");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта принимают результат после множественных сравнений как подтверждение");
        add(conflicts, initial, hitA ^ hitB,
                "data dredging: один эксперт закрыл вывод найденным после поиска разрезом");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "data dredging (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "перебрали подгруппы пока не нашли значимое",
                "пробовали разные модели пока не подошло",
                "после нескольких разрезов нашли паттерн поэтому верно",
                "искали пока не нашли значимость",
                "множественные сравнения без коррекции достаточны",
                "garden of forking paths закрывает вопрос",
                "p-hacking показывает что эффект есть",
                "data dredging confirms the claim",
                "we tried several cuts until it was significant so it is true",
                "looked at many subgroups and found a hit therefore confirmed",
                "no correction needed because we found something",
                "multiple comparisons without adjustment is enough proof");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "data dredging",
                "garden of forking paths",
                "множественные сравнения требуют коррекции",
                "p-hacking не подтверждение",
                "перебор гипотез без пререгистрации не доказательство",
                "a pattern found after searching is not confirmation",
                "uncorrected multiple comparisons are not evidence",
                "forking paths inflate false positives");
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
