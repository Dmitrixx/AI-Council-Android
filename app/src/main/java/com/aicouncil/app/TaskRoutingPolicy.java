package com.aicouncil.app;

import java.util.Locale;

/** Deterministic multilingual routing rules for the Auto team strategy and tool hints. */
public final class TaskRoutingPolicy {
    private TaskRoutingPolicy() { }

    /**
     * @return 0 quick answer, 1 council, 2 audit, 3 deep analysis
     */
    public static int chooseMode(String task, boolean[] configured, boolean freeOnly, boolean[] markedFree) {
        String raw = task == null ? "" : task.trim();
        if (raw.isEmpty()) return 0;
        String lower = raw.toLowerCase(Locale.ROOT);
        boolean expert2 = usable(1, configured, freeOnly, markedFree);
        boolean expert3 = usable(2, configured, freeOnly, markedFree);

        boolean audit = containsAny(lower,
                "проверь", "проверить", "проверка", "аудит", "ошибк", "верифиц", "факт-чек", "fact-check",
                "fact check", "verify", "verify this", "check this", "review this", "audit", "critique",
                "перевір", "перевірити", "перевірка", "помилк", "звір", "фактчек", "аудитуй",
                "validate", "validation", "debug", "find bugs", "find errors");
        boolean complex = raw.length() > 450 || containsAny(lower,
                "рассч", "расчет", "расчёт", "сравнен", "спроект", "архитект", "пошаг", "несколько условий", "проанализируй",
                "calculate", "calculation", "compare", "comparison", "design", "architecture", "step by step",
                "multi-step", "multiple constraints", "analyze deeply", "analyse deeply", "evaluate options", "trade-off",
                "порах", "обчисл", "порівня", "спроєкт", "архітект", "покрок", "кілька умов", "проаналізуй",
                "досліди", "план реалізації", "реалізаці", "стратегія", "стратегия");
        boolean research = containsAny(lower,
                "исслед", "обзор литературы", "literature", "research", "state of the art", "сравни подходы",
                "best practices", "актуальн", "за 202", "latest", "recent papers");
        boolean coding = looksLikeCodeTask(lower);

        if (audit && expert2) return 2;
        if ((complex || research || coding) && expert2 && expert3) return 3;
        if (raw.length() > 120 && expert2 && expert3) return 1;
        return 0;
    }

    public static String toolAwarenessHint(String task) {
        String lower = task == null ? "" : task.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        if (looksLikeMath(lower)) {
            sb.append("Задача похожа на расчёт: покажи формулы, промежуточные шаги и единицы измерения; "
                    + "отдели арифметику от допущений.\n");
        }
        if (looksLikeCodeTask(lower)) {
            sb.append("Задача связана с кодом: укажи язык, ограничения, краевые случаи и как проверить решение; "
                    + "не утверждай, что код уже выполнен, если исполнения не было.\n");
        }
        if (containsAny(lower, "найди в интернете", "поиск", "search the web", "google", "актуальн", "сегодня", "latest news")) {
            sb.append("Если веб-поиск не подключён, не выдавай свежие факты за проверенные; отметь, что нужна проверка источников.\n");
        }
        return sb.toString().trim();
    }

    public static boolean looksLikeMath(String lower) {
        if (lower == null || lower.isEmpty()) return false;
        return containsAny(lower,
                "посчит", "вычисл", "рассч", "расчет", "расчёт", "формул", "интеграл", "производн",
                "уравнен", "процент", "probability", "calculate", "equation", "derivative", "integral",
                "обчисл", "порах", "рівнян");
    }

    public static boolean looksLikeCodeTask(String lower) {
        if (lower == null || lower.isEmpty()) return false;
        return containsAny(lower,
                "напиши код", "напиши функцию", "рефактор", "unit test", "junit", "python", "javascript",
                "typescript", "kotlin", "java code", "sql query", "regex", "api endpoint", "bugfix",
                "stack trace", "compile error", "код на", "функцію", "програму");
    }

    private static boolean usable(int index, boolean[] configured, boolean freeOnly, boolean[] markedFree) {
        boolean present = configured != null && index >= 0 && index < configured.length && configured[index];
        if (!present) return false;
        return !freeOnly || (markedFree != null && index < markedFree.length && markedFree[index]);
    }

    private static boolean containsAny(String lower, String... needles) {
        if (lower == null || needles == null) return false;
        for (String n : needles) {
            if (n != null && !n.isEmpty() && lower.contains(n)) return true;
        }
        return false;
    }
}
