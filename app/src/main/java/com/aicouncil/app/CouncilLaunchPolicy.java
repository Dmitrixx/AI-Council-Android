package com.aicouncil.app;

/**
 * Pure rules for starting a council run: validation, mode resolution, required
 * providers, failure detection and completion status strings.
 * Network/UI stay in the Activity / engine.
 */
public final class CouncilLaunchPolicy {
    public static final int MAX_TASK_CHARS = 100_000;

    public static final String BUSY = "●  Дождитесь завершения текущего запроса.";
    public static final String STOP_REQUESTED = "■  Остановка запрошена…";
    public static final String EMPTY_TASK = "Введите задачу";
    public static final String TASK_TOO_LONG_ERROR = "Сократите задачу до 100 000 символов";
    public static final String TASK_TOO_LONG_STATUS =
            "Задача слишком длинная. Сократите до 100 000 символов.";
    public static final String STOPPED_BY_USER = "Остановлено пользователем. Частичный результат сохранён.";
    public static final String SECRETS_MASKED = "Возможные секреты замаскированы перед отправкой задачи.";

    private CouncilLaunchPolicy() {}

    public static String taskFieldError(String task) {
        if (isTaskEmpty(task)) return EMPTY_TASK;
        if (isTaskTooLong(task)) return TASK_TOO_LONG_ERROR;
        return null;
    }

    public static boolean isTaskTooLong(String task) {
        return task != null && task.length() > MAX_TASK_CHARS;
    }

    public static boolean isTaskEmpty(String task) {
        return task == null || task.trim().isEmpty();
    }

    /**
     * @param selectedMode 0..3 fixed modes, 4 = auto
     * @return effective mode 0..3
     */
    public static int resolveMode(int selectedMode, String task, boolean[] configured,
                                  boolean localOnly, boolean[] freeTier) {
        if (selectedMode >= 0 && selectedMode <= 3) return selectedMode;
        // auto: prefer full council when all three configured, else audit, else quick
        boolean a = configured != null && configured.length > 0 && configured[0];
        boolean b = configured != null && configured.length > 1 && configured[1];
        boolean c = configured != null && configured.length > 2 && configured[2];
        if (a && b && c) return 1; // council
        if (a && b) return 2; // audit
        return 0; // quick
    }

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

    public static boolean skipSensitiveConfirm(String originalTask) {
        return originalTask == null || originalTask.trim().isEmpty();
    }
}
