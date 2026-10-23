package com.aicouncil.app;

/**
 * Pure rules for launching a council run: task validation, busy/stop messages,
 * required provider indices by mode, and completion status text.
 */
public final class CouncilLaunchPolicy {
    public static final int MAX_TASK_CHARS = 12000;

    public static final String BUSY = "●  Дождитесь завершения текущего запроса";
    public static final String STOP_REQUESTED = "●  Остановка…";
    public static final String EMPTY_TASK = "Сначала введите задачу.";
    public static final String TASK_TOO_LONG_PREFIX = "Слишком длинный текст. Сократите до ";
    public static final String TASK_TOO_LONG_SUFFIX = " символов.";
    public static final String STOPPED_BY_USER = "Остановлено пользователем. Частичный результат сохранён.";

    private CouncilLaunchPolicy() {}

    public static boolean isTaskEmpty(String task) {
        return task == null || task.trim().isEmpty();
    }

    public static boolean isTaskTooLong(String task) {
        return task != null && task.length() > MAX_TASK_CHARS;
    }

    public static String taskTooLongMessage() {
        return TASK_TOO_LONG_PREFIX + MAX_TASK_CHARS + TASK_TOO_LONG_SUFFIX;
    }

    /** Returns required expert indices for the mode, or empty for all/auto. */
    public static int[] requiredProviderIndices(int mode) {
        if (mode == 0) return new int[]{0}; // quick
        if (mode == 2) return new int[]{0, 1}; // check
        return new int[]{0, 1, 2}; // council / analysis / auto
    }

    public static boolean looksLikeFailure(String finalResult, boolean cancelRequested) {
        if (cancelRequested) return true;
        if (finalResult == null || finalResult.trim().isEmpty()) return true;
        String v = finalResult.trim();
        return v.startsWith("Ошибка")
                || v.startsWith("Ошибка настроек")
                || v.startsWith("Не удалось")
                || v.startsWith("Остановлено пользователем.")
                || v.startsWith("Ответ API получен, но формат")
                || v.startsWith("Выполнение остановлено");
    }

    public static String completionStatus(boolean cancelRequested, boolean hasFailure, long elapsedSeconds) {
        if (cancelRequested) return STOPPED_BY_USER;
        String head = hasFailure ? "Завершено с предупреждением" : "Готово";
        long sec = Math.max(0L, elapsedSeconds);
        return head + " · " + sec + " сек. · проверьте важные факты";
    }
}
