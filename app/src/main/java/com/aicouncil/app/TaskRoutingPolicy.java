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
                "validate", "validation", "debug", "find bugs", "find errors", "найди ошибки", "что не так",
                "критик", "оцени", "ревью");
        boolean complex = raw.length() > 280 || containsAny(lower,
                "рассч", "расчет", "расчёт", "сравнен", "спроект", "архитект", "пошаг", "несколько условий", "проанализируй",
                "calculate", "calculation", "compare", "comparison", "design", "architecture", "step by step",
                "multi-step", "multiple constraints", "analyze deeply", "analyse deeply", "evaluate options", "trade-off",
                "порах", "обчисл", "порівня", "спроєкт", "архітект", "покрок", "кілька умов", "проаналізуй",
                "досліди", "план реалізації", "реалізаці", "стратегія", "стратегия", "оптимизируй", "tradeoffs", "компромисс",
                "план", "планиров", "strategy", "roadmap", "дорожная карта", "сценарий", "варианты", "alternatives", "креатив", "идеи", "brainstorm");
        boolean research = containsAny(lower,
                "исслед", "обзор литературы", "literature", "research", "state of the art", "сравни подходы",
                "best practices", "актуальн", "за 202", "latest", "recent papers", "что говорят источники", "systematic review",
                "обзор", "сравнение подходов", "pros and cons", "плюсы и минусы");
        boolean coding = looksLikeCodeTask(lower);
        boolean highStakes = containsAny(lower,
                "медицин", "здоровь", "диагноз", "лекарств", "юридич", "налог", "финанс", "инвестиц", "безопасность", "medical", "legal", "financial", "diagnosis",
                "риск", "безопасности", "критичное решение", "high stakes", "safety");
        boolean planning = containsAny(lower,
                "план действий", "дорожная карта", "roadmap", "этапы", "сроки", "милстоны", "implementation plan", "как сделать", "how to implement");

        if (audit && expert2) return 2;
        if ((complex || research || coding || highStakes || planning) && expert2 && expert3) return 3;
        if ((raw.length() > 120 || complex || research || planning) && expert2 && expert3) return 1;
        if (raw.length() > 80 && expert2) return 1;
        return 0;
    }

    public static String toolAwarenessHint(String task) {
        String lower = task == null ? "" : task.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        if (looksLikeMath(lower)) {
            sb.append("Задача похожа на расчёт: покажи формулы, промежуточные шаги, единицы измерения; отдели арифметику от допущений и укажи точность.\n");
        }
        if (looksLikeCodeTask(lower)) {
            sb.append("Задача связана с кодом: укажи язык, ограничения, краевые случаи, как проверить решение; не утверждай, что код уже выполнен, если исполнения не было.\n");
        }
        if (containsAny(lower, "найди в интернете", "поиск", "search the web", "google", "актуальн", "сегодня", "latest news", "свежие данные", "свежая информация")) {
            sb.append("Если веб-поиск не подключён, не выдавай свежие факты за проверенные; отметь, что нужна проверка источников и укажи дату актуальности.\n");
        }
        if (containsAny(lower, "медицин", "здоровь", "диагноз", "лечени", "medical", "health", "symptom", "симптом")) {
            sb.append("Медицинская тема: это не диагноз и не замена врачу. Укажи, что требует профессиональной консультации и какие факты нужно проверить.\n");
        }
        if (containsAny(lower, "юридич", "закон", "договор", "legal", "contract", "право")) {
            sb.append("Юридическая тема: это не юридическая консультация. Отметь неопределённости и необходимость проверки у специалиста.\n");
        }
        if (containsAny(lower, "план", "strategy", "roadmap", "как сделать", "implementation")) {
            sb.append("Задача на планирование: разбей на этапы, риски, зависимости и метрики успеха; укажи, что требует проверки в реальных условиях.\n");
        }
        return sb.toString().trim();
    }

    public static boolean looksLikeMath(String lower) {
        if (lower == null || lower.isEmpty()) return false;
        return containsAny(lower,
                "посчит", "вычисл", "рассч", "расчет", "расчёт", "формул", "интеграл", "производн",
                "уравнен", "процент", "probability", "calculate", "equation", "derivative", "integral",
                "обчисл", "порах", "рівнян", "сколько будет", "вычисли");
    }

    public static boolean looksLikeCodeTask(String lower) {
        if (lower == null || lower.isEmpty()) return false;
        return containsAny(lower,
                "напиши код", "напиши функцию", "рефактор", "unit test", "junit", "python", "javascript",
                "typescript", "kotlin", "java code", "sql query", "regex", "api endpoint", "bugfix",
                "stack trace", "compile error", "код на", "функцію", "програму", "исправь баг", "debug this",
                "алгоритм", "оптимизируй код", "refactor");
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
