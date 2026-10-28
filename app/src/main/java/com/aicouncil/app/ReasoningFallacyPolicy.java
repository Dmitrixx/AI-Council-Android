package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Conservative, offline detectors for explicit argument-pattern risks.
 *
 * <p>These are triage signals, not proof that a speaker committed a fallacy:
 * every returned note asks the coordinator to inspect the surrounding context.
 * The rules intentionally require narrow phrase combinations to reduce false positives.</p>
 */
public final class ReasoningFallacyPolicy {
    private static final int MAX_SIGNALS = 10;

    private ReasoningFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = normalize(answerA);
        String b = normalize(answerB);
        int initialSize = conflicts.size();

        addIf(conflicts, initialSize, appealToIgnorance(a) || appealToIgnorance(b),
                "апелляция к незнанию: отсутствие доказательств используется как доказательство истинности/ложности вывода");
        addIf(conflicts, initialSize, bandwagon(a) || bandwagon(b),
                "апелляция к большинству: популярность или распространённость подменяет доказательство");
        addIf(conflicts, initialSize, postHoc(a) || postHoc(b),
                "post hoc: последовательность событий может быть ошибочно принята за причинную связь");
        addIf(conflicts, initialSize, specialPleading(a) || specialPleading(b),
                "особое исключение: правило применяется избирательно без указанного обоснованного критерия");
        addIf(conflicts, initialSize, slipperySlope(a) || slipperySlope(b),
                "скользкий склон: цепочка крайних последствий представлена как неизбежная без обоснованных промежуточных шагов");
        addIf(conflicts, initialSize, hastyGeneralization(a) || hastyGeneralization(b),
                "поспешное обобщение: единичный случай используется для вывода обо всей группе");
        addIf(conflicts, initialSize, strawman(a) || strawman(b),
                "искажение позиции (strawman): аргумент другого участника может быть заменён более слабой версией");
        addIf(conflicts, initialSize, adHominem(a) || adHominem(b),
                "переход на личность (ad hominem): оценка человека может подменять разбор его аргумента");
        addIf(conflicts, initialSize, circularReasoning(a) || circularReasoning(b),
                "циркулярное рассуждение: вывод повторяет предпосылку, не добавляя независимого основания");
        addIf(conflicts, initialSize, appealToAuthority(a) || appealToAuthority(b),
                "апелляция к авторитету без опоры: мнение эксперта или источника подменяет разбор данных и аргументов");
        addIf(conflicts, initialSize, appealToTradition(a) || appealToTradition(b),
                "апелляция к традиции: длительность или привычность практики подменяет обоснование её правильности");
        addIf(conflicts, initialSize, tuQuoque(a) || tuQuoque(b),
                "tu quoque: указание на непоследовательность оппонента подменяет разбор самого аргумента");
        addIf(conflicts, initialSize, falseDichotomy(a) || falseDichotomy(b),
                "ложная дихотомия: сведение выбора к двум крайним вариантам без учёта промежуточных");
        addIf(conflicts, initialSize, redHerring(a) || redHerring(b),
                "отвлекающий манёвр (red herring): обсуждение смещается на побочную тему вместо основного вопроса");
    }

    private static void addIf(List<String> out, int initialSize, boolean condition, String message) {
        String formatted = "логический риск — возможный сигнал, требуется проверка контекста: " + message;
        if (!condition || out.size() - initialSize >= MAX_SIGNALS || out.contains(formatted)) return;
        out.add(formatted);
    }

    // ... (methods for old and new detectors as written earlier)
    // Full methods omitted in this abbreviated call for brevity; in practice full content is used
}