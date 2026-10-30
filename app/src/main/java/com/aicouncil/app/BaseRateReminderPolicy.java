package com.aicouncil.app;

import java.util.Locale;

/**
 * Local reminder against base-rate neglect when a task discusses a positive test
 * for a rare condition without an explicit prevalence/base-rate number nearby.
 * Conservative: only fires on clear cues and stays silent otherwise.
 * The note is a methodological reminder, not a diagnosis or calculation.
 */
public final class BaseRateReminderPolicy {
    private BaseRateReminderPolicy() {}

    public static String evaluateExplicitRequest(String task) {
        if (task == null || task.trim().isEmpty()) return "";
        String lower = task.toLowerCase(Locale.ROOT);
        boolean rare = containsAny(lower,
                "редк", "rare", "нечасто", "не часто", "маловероят", "малоймовір",
                "редкое заболевание", "rare disease", "редкое состояние",
                "базовая частот", "base rate", "prevalence", "распростран");
        boolean positiveTest = containsAny(lower,
                "положительн", "positive test", "тест положителен", "позитивний",
                "положительный результат", "positive result");
        boolean hasExplicitBase = lower.contains("%") && (lower.contains("распростран")
                || lower.contains("prevalence") || lower.contains("базовая частот")
                || lower.contains("base rate") || lower.contains("априор"));

        if (rare && positiveTest && !hasExplicitBase) {
            return "НАПОМИНАНИЕ О БАЗОВОЙ ЧАСТОТЕ (не ответ ИИ):\n"
                    + "Задача упоминает редкое состояние/заболевание и положительный тест, "
                    + "но явной базовой частоты (prevalence / априорной вероятности в процентах) рядом нет.\n"
                    + "Положительный тест при низкой базовой частоте часто даёт низкую апостериорную вероятность "
                    + "(теорема Байеса). Не делай вывод о высокой вероятности болезни только из факта положительного теста.\n"
                    + "Если базовой частоты нет в задаче — прямо скажи, что без неё надёжный вывод невозможен, "
                    + "и попроси или оцени диапазон. Это не диагноз.";
        }
        return "";
    }

    private static boolean containsAny(String lower, String... cues) {
        for (String cue : cues) {
            if (lower.contains(cue)) return true;
        }
        return false;
    }
}
