package com.aicouncil.app;

/**
 * Pure rules for backup size limits, trim steps and export/import status messages.
 * JSON assembly and file I/O stay in the Activity.
 */
public final class BackupExportPolicy {
    public static final int MAX_BACKUP_BYTES = 5 * 1024 * 1024;
    public static final int MAX_TEXT_IMPORT_BYTES = 256 * 1024;
    public static final int MAX_DOCUMENT_IMPORT_BYTES = 10 * 1024 * 1024;

    public static final int DEFAULT_TASK_LIMIT = 30_000;
    public static final int DEFAULT_ANSWER_LIMIT = 100_000;
    public static final int DEFAULT_DIALOGUE_LIMIT = 60_000;
    public static final int MIN_TASK_LIMIT = 750;
    public static final int MIN_ANSWER_LIMIT = 3_000;
    public static final int MIN_DIALOGUE_LIMIT = 1_500;
    public static final int RESULT_FILENAME_MAX = 48;
    public static final int PROJECT_NAME_LIMIT = 160;

    public static final String PREPARE_TOO_LARGE =
            "Не удалось подготовить копию до 5 МБ. Удалите лишние большие проекты и повторите экспорт.";
    public static final String PREPARE_FAILED_PREFIX = "Не удалось подготовить резервную копию: ";
    public static final String IMPORT_FAILED_PREFIX = "Не удалось импортировать резервную копию: ";
    public static final String READING_BACKUP = "Читаем резервную копию…";
    public static final String SAVE_CANCELLED = "Сохранение отменено.";
    public static final String SAVED_TRIMMED =
            "Резервная копия сохранена до 5 МБ. Часть очень длинных полей сокращена; API-ключи не включены.";
    public static final String SAVED_FULL =
            "Резервная копия сохранена. API-ключи в неё не включены.";
    public static final String SAVE_ERROR_PREFIX = "Ошибка сохранения файла: ";
    public static final String NEED_RESULT_SAVE =
            "Сначала получите или откройте результат для сохранения.";
    public static final String NEED_RESULT_EXPORT =
            "Сначала получите или откройте результат для экспорта.";
    public static final String OPEN_SHARE =
            "Откройте приложение для отправки или сохранения результата.";
    public static final String RESULT_COPIED = "●  Результат скопирован";
    public static final String BLOCK_COPIED = "Ответ из выбранного блока скопирован.";
    public static final String BACKUP_FILENAME_PREFIX = "AI_Council_backup_";
    public static final String RESULT_FILENAME_DEFAULT = "AI_Council_result";

    private BackupExportPolicy() {}

    public static boolean fitsBackupSize(int utf8Bytes) {
        return utf8Bytes >= 0 && utf8Bytes <= MAX_BACKUP_BYTES;
    }

    public static boolean canShrinkFurther(int taskLimit, int answerLimit, int dialogueLimit) {
        return !(taskLimit <= MIN_TASK_LIMIT
                && answerLimit <= MIN_ANSWER_LIMIT
                && dialogueLimit <= MIN_DIALOGUE_LIMIT);
    }

    public static int nextTaskLimit(int current) {
        return Math.max(MIN_TASK_LIMIT, (int) (current * 0.65));
    }

    public static int nextAnswerLimit(int current) {
        return Math.max(MIN_ANSWER_LIMIT, (int) (current * 0.65));
    }

    public static int nextDialogueLimit(int current) {
        return Math.max(MIN_DIALOGUE_LIMIT, (int) (current * 0.65));
    }

    public static String prepareFailed(String message) {
        return PREPARE_FAILED_PREFIX + (message == null ? "" : message);
    }

    public static String importFailed(String message) {
        return IMPORT_FAILED_PREFIX + (message == null ? "" : message);
    }

    public static String saveError(String message) {
        return SAVE_ERROR_PREFIX + (message == null ? "" : message);
    }

    public static String savedMessage(boolean trimmed) {
        return trimmed ? SAVED_TRIMMED : SAVED_FULL;
    }

    public static String backupFileTitle(String timestampYmdHm) {
        return BACKUP_FILENAME_PREFIX + timestampYmdHm + ".json";
    }

    public static String resultFileBase(String task) {
        String base = task == null || task.trim().isEmpty()
                ? RESULT_FILENAME_DEFAULT
                : task.replaceAll("[^a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ0-9_-]+", "_");
        if (base.length() > RESULT_FILENAME_MAX) base = base.substring(0, RESULT_FILENAME_MAX);
        if (base.isEmpty()) base = RESULT_FILENAME_DEFAULT;
        return base;
    }
}
