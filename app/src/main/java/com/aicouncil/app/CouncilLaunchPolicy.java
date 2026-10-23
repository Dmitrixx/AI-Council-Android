package com.aicouncil.app;

/**
 * Pure rules for launching a council task: limits, mode resolution, completion status.
 * UI and network stay in MainActivity / CouncilExecutionEngine.
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
    public static final String PARTIAL_SUCCESS =
            "Частичный успех · итог сформирован по доступным ответам экспертов";
    public static final String TOTAL_FAILURE =
            "Не удалось получить содержательные ответы. Проверьте ключи, URL и лимиты провайдеров.";

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
        boolean a = configured != null && configured.length > 0 && configured[0];
        boolean b = configured != null && configured.length > 1 && configured[1];
        boolean c = configured != null && configured.length > 2 && configured[2];
        if (a && b && c) return 1;
        if (a && b) return 2;
        return 0;
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
                || v.startsWith("Выполнение остановлено")
                || v.startsWith("Оба независимых эксперта не смогли")
                || v.contains("НЕ ПОЛУЧЕН")
                || v.contains(" · ОШИБКА");
    }

    /** True when result has useful content despite warnings (⚠️, partial experts). */
    public static boolean looksLikePartialSuccess(String finalResult, boolean cancelRequested) {
        if (cancelRequested) return false;
        if (finalResult == null || finalResult.trim().isEmpty()) return false;
        if (!looksLikeFailure(finalResult, false) && !finalResult.contains("⚠️")) return false;
        String v = finalResult.trim();
        if (v.contains("ИТОГОВЫЙ ВЫВОД") || v.contains("СВОДКА") || v.contains("✦ ")) return true;
        if (v.contains("\n\n") && v.length() > 280) return true;
        return false;
    }

    public static String completionStatus(boolean cancelRequested, boolean hasFailure, long elapsedSeconds) {
        return completionStatus(cancelRequested, hasFailure, false, elapsedSeconds);
    }

    public static String completionStatus(boolean cancelRequested, boolean hasFailure,
                                          boolean partialSuccess, long elapsedSeconds) {
        if (cancelRequested) return STOPPED_BY_USER;
        long sec = Math.max(0L, elapsedSeconds);
        if (partialSuccess) {
            return PARTIAL_SUCCESS + " · " + sec + " сек. · проверьте важные факты";
        }
        if (hasFailure) {
            return "Завершено с предупреждением · " + sec + " сек. · проверьте важные факты";
        }
        return "Готово · " + sec + " сек. · проверьте важные факты";
    }

    public static boolean skipSensitiveConfirm(String originalTask) {
        return originalTask == null || originalTask.trim().isEmpty();
    }

    /** Coordinator prompt suffix when one independent expert failed. */
    public static String partialExpertCoordinatorNote(boolean expert1Failed, boolean expert2Failed) {
        if (expert1Failed && expert2Failed) {
            return "\n\nВНИМАНИЕ: оба независимых эксперта вернули ошибку API. "
                    + "Не выдавай ошибки за содержательные ответы. Кратко укажи вероятные причины "
                    + "(ключ, URL, лимит, модель) и что пользователю проверить. "
                    + "Если по тексту ошибок можно дать частичный совет по задаче — дай его с явной оговоркой о неполноте.";
        }
        if (expert1Failed) {
            return "\n\nВНИМАНИЕ: ответ ИИ №1 недоступен из‑за ошибки. Синтезируй итог только по доступным данным, "
                    + "явно отметь пробел и не выдумывай позицию отсутствующего эксперта.";
        }
        if (expert2Failed) {
            return "\n\nВНИМАНИЕ: ответ ИИ №2 недоступен из‑за ошибки. Синтезируй итог только по доступным данным, "
                    + "явно отметь пробел и не выдумывай позицию отсутствующего эксперта.";
        }
        return "";
    }
}
