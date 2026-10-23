package com.aicouncil.app;

import java.util.Locale;

/** Deterministic multilingual routing rules for the Auto team strategy. */
public final class TaskRoutingPolicy {
    private TaskRoutingPolicy() { }

    /**
     * @param task user prompt
     * @param configured true when the provider endpoint, model and required credentials are configured
     * @param freeOnly whether only providers manually marked as free may be used
     * @param markedFree providers marked by the user as free
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

        if (audit && expert2) return 2;
        if (complex && expert2 && expert3) return 3;
        if (raw.length() > 120 && expert2 && expert3) return 1;
        return 0;
    }

    private static boolean usable(int index, boolean[] configured, boolean freeOnly, boolean[] markedFree) {
        boolean present = configured != null && index >= 0 && index < configured.length && configured[index];
        if (!present) return false;
        return !freeOnly || (markedFree != null && index >= 0 && index < markedFree.length && markedFree[index]);
    }

    private static boolean containsAny(String text, String... terms) {
        for (String term : terms) if (text.contains(term)) return true;
        return false;
    }
}
