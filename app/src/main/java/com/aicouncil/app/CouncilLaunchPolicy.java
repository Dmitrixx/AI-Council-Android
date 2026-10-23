package com.aicouncil.app;

/**
 * Pure rules for starting and stopping a council/analysis run:
 * task size limits, busy/stop messages, required providers by mode,
 * and post-run failure detection. Network and UI stay in the Activity.
 */
public final class CouncilLaunchPolicy {
    public static final int MAX_TASK_CHARS = 100_000;

    public static final String BUSY = "●  Дождитесь завершения текущего запроса.";
    public static final String STOP_REQUESTED = "■  Остановка запрошена…";
    public static final String EMPTY_TASK = "Введите задачу";
    public static final String TASK_TOO_LONG_ERROR = "Сократите задачу до 100 000 символов";
    public static final String TASK_TOO_LONG_STATUS =
            "Запрос не отправлен: текст задачи превышает лимит 100 000 символов.";
    public static final String STOPPED_BY_USER = "Остановлено пользователем. Частичный результат сохранён.";
    public static final String SECRETS_MASKED = "Возможные секреты замаскированы перед отправкой задачи.";

    private CouncilLaunchPolicy() {}

    /** @return validation error for the prompt field, or null if OK */
    public static String taskFieldError(String task) {
        String t = task == null ? "" : task.trim();
        if (t.isEmpty()) return EMPTY_TASK;
        if (t.length() > MAX_TASK_CHARS) return TASK_TOO_LONG_ERROR;
        return null;
    }

    public static boolean isTaskTooLong(String task) {
        String t = task == null ? "" : task.trim();
        return t.length() > MAX_TASK_CHARS;
    }

    public static boolean isTaskEmpty(String task) {
        return task == null || task.trim().isEmpty();
    }

    /**
     * Resolve analysis mode: auto (4) uses {@link TaskRoutingPolicy}, else the spinner index.
     */
    public static int resolveMode(int selectedMode, String task, boolean[] configured,
                                  boolean freeOnly, boolean[] markedFree) {
        if (selectedMode == 4) {
            return TaskRoutingPolicy.chooseMode(task, configured, freeOnly, markedFree);
        }
        if (selectedMode < 0) return 0;
        if (selectedMode > 3) return 3;
        return selectedMode;
    }

    /** Provider indices required for the given mode (0 fast, 2 audit, else three experts). */
    public static int[] requiredProviderIndices(int mode) {
        if (mode == 0) return new int[]{0};
        if (mode == 2) return new int[]{0, 1};
        return new int[]{0, 1, 2};
    }

    public static int clampWebContextMode(int selected, int modeCount) {
        if (modeCount <= 0) return 0;
        if (selected < 0) return 0;
        if (selected >= modeCount) return modeCount - 1;
        return selected;
    }

    /**
     * Heuristic: final report text indicates failure or cancellation.
     */
    public static boolean looksLikeFailure(String finalResult, boolean cancelRequested) {
        if (cancelRequested) return true;
        if (finalResult == null) return true;
        String r = finalResult;
        return r.startsWith("Не удалось")
                || r.startsWith("Ошибка")
                || r.contains("НЕ ПОЛУЧЕН")
                || r.contains("Оба независимых эксперта не смогли")
                || r.contains("⚠️")
                || r.contains(" · ОШИБКА");
    }

    public static String completionStatus(boolean cancelRequested, boolean hasFailure, int elapsedSeconds) {
        if (cancelRequested) return STOPPED_BY_USER;
        String head = hasFailure ? "Завершено с предупреждением" : "Готово";
        return head + " · " + elapsedSeconds + " сек. · проверьте важные факты";
    }

    /** Whether runCouncil should skip the sensitive-prompt dialog and go straight to request. */
    public static boolean skipSensitiveConfirm(String originalTask) {
        return isTaskEmpty(originalTask) || isTaskTooLong(originalTask);
    }
}
