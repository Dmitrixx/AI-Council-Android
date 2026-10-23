package com.aicouncil.app;

/**
 * Pure rules and user-facing messages for voice input and file/OCR import flows.
 * Intent launching and UI dialogs stay in the Activity.
 */
public final class InputImportPolicy {
    public static final String VOICE_BUSY =
            "Голосовой ввод доступен после завершения текущего запроса.";
    public static final String VOICE_PROMPT = "Продиктуйте задачу для AI Council";
    public static final String VOICE_NO_SERVICE =
            "На устройстве нет доступного сервиса распознавания речи. Можно установить или включить голосовой ввод Google.";
    public static final String VOICE_CANCELLED = "Голосовой ввод отменён.";
    public static final String VOICE_CANCELLED_UNCHANGED =
            "Голосовой ввод отменён, черновик не изменён.";
    public static final String VOICE_EMPTY =
            "Не удалось распознать речь. Попробуйте ещё раз или введите текст.";
    public static final String VOICE_APPLIED =
            "Голосовой текст добавлен в задачу. Проверьте его перед отправкой.";
    public static final String VOICE_TOO_LONG =
            "Распознанный текст превышает лимит задачи 100 000 символов и не добавлен.";
    public static final String VOICE_PICK_TITLE = "Выберите распознанный текст";

    public static final String IMPORT_TEXT_CANCELLED = "Импорт текстового файла отменён.";
    public static final String IMPORT_IMAGE_CANCELLED = "Импорт изображения отменён.";
    public static final String IMPORT_IMAGE_CYRILLIC_CANCELLED =
            "Импорт изображения для OCR кириллицы отменён.";
    public static final String IMPORT_PDF_CYRILLIC_CANCELLED =
            "Импорт сканированного PDF для OCR кириллицы отменён.";
    public static final String IMPORT_BACKUP_CANCELLED = "Импорт резервной копии отменён.";
    public static final String IMPORT_CANCELLED_UNCHANGED = "Импорт отменён — задание не изменено.";
    public static final String IMPORT_ALREADY_RUNNING = "Импорт документа уже выполняется.";
    public static final String OCR_CANCELLED = "Загрузка модели или распознавание OCR отменено.";
    public static final String OCR_PDF_CANCELLED =
            "Загрузка модели или распознавание OCR PDF отменено.";

    private InputImportPolicy() {}

    public static String voiceOpenError(String message) {
        String detail = message == null || message.isEmpty() ? "неизвестная ошибка" : message;
        return "Не удалось открыть голосовой ввод: " + detail;
    }

    public static String pickerOpenError(String kind, String message) {
        String detail = message == null ? "" : message;
        return "Не удалось открыть выбор " + kind + ": " + detail;
    }

    /** Dialog label for a speech alternative (0-based index). */
    public static String candidateLabel(int index, String rawValue) {
        String value = rawValue == null ? "" : rawValue.replaceAll("\\s+", " ").trim();
        if (index == 0) return "Лучший вариант · " + value;
        return "Вариант " + (index + 1) + " · " + value;
    }

    public static String[] candidateLabels(java.util.List<String> candidates) {
        if (candidates == null || candidates.isEmpty()) return new String[0];
        String[] labels = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            labels[i] = candidateLabel(i, candidates.get(i));
        }
        return labels;
    }

    public static String importFailure(String detail) {
        return "Импорт не выполнен: " + (detail == null ? "" : detail);
    }
}
