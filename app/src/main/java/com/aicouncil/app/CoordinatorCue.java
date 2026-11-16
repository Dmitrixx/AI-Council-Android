package com.aicouncil.app;

import java.util.List;

/**
 * Builds a concise cue for the coordinator from local deliberation signals.
 * Signals are hypotheses that require context; naming a gap is not an error.
 * The coordinator must not close the decision on any single signal alone.
 */
public final class CoordinatorCue {
    private static final int MAX_NOTES = 8;

    private CoordinatorCue() {}

    public static void append(StringBuilder sb, List<String> conflicts) {
        if (sb == null || conflicts == null || conflicts.isEmpty()) return;
        sb.append("\nЛокальные сигналы рассуждений (гипотезы, требуют контекста, не автоматический вердикт):\n");
        int shown = 0;
        for (String signal : conflicts) {
            if (shown >= MAX_NOTES) break;
            sb.append("- ").append(signal).append("\n");
            shown++;
        }
        if (conflicts.size() > MAX_NOTES) {
            sb.append("(+ещё ").append(conflicts.size() - MAX_NOTES).append(" сигналов)\n");
        }
        sb.append("Координатор не закрывает решение на одном сигнале. Назвать пробел — не ошибка.\n");
    }

    public static int noteCount(List<String> conflicts) {
        if (conflicts == null) return 0;
        return Math.min(conflicts.size(), MAX_NOTES);
    }
}
