package com.aicouncil.app;

/**
 * Pure rules for follow-up dialogue: validation messages, expert index clamp,
 * turn limits and the fixed follow-up user prompt prefix.
 * JSON assembly and network stay in the Activity.
 */
public final class FollowUpDialoguePolicy {
    public static final int MAX_STORED_TURNS = 22;
    public static final int MAX_REQUEST_TURNS = 20;
    public static final int MAX_DIALOGUE_CHARS = 60_000;
    public static final int MAX_TURN_CONTENT_CHARS = 80_000;

    public static final String EMPTY_QUESTION = "Введите уточняющий вопрос";
    public static final String NO_RESULT_CONTEXT =
            "Сначала получите или откройте результат, по которому нужен уточняющий вопрос.";
    public static final String LOCAL_ONLY_BLOCKS_CLOUD =
            "Режим «Только локальные ИИ» блокирует облачный follow-up. Выберите настроенную локальную модель.";
    public static final String SECRETS_MASKED =
            "Возможные секреты замаскированы перед отправкой уточнения.";
    public static final String BUSY = "Дождитесь завершения текущего запроса.";
    public static final String RUNNING_BUTTON = "Выполняется…";
    public static final String EXPERT_WORKING = "◉ Уточняет ответ";

    public static final String FOLLOW_INSTRUCTION_PREFIX =
            "Продолжи обсуждение, сохраняя контекст. Перепроверь спорные места, "
                    + "не выдумывай источники и явно обозначь ограничения.\n\nВопрос пользователя: ";

    private FollowUpDialoguePolicy() {}

    public static int clampExpertIndex(int spinnerPosition) {
        if (spinnerPosition < 0) return 0;
        if (spinnerPosition > 2) return 2;
        return spinnerPosition;
    }

    public static boolean isQuestionEmpty(String question) {
        return question == null || question.trim().isEmpty();
    }

    public static boolean hasValidResultContext(String contextAnswer) {
        if (contextAnswer == null || contextAnswer.trim().isEmpty()) return false;
        return !StreamingResultPolicy.isPlaceholderResult(contextAnswer.trim());
    }

    public static String buildFollowUserContent(String question) {
        String q = question == null ? "" : question.trim();
        return FOLLOW_INSTRUCTION_PREFIX + q;
    }

    public static String configureExpertMessage(int expertIndex) {
        int n = clampExpertIndex(expertIndex) + 1;
        return "Настройте ИИ №" + n + " и проверьте его доступность в текущем режиме.";
    }

    public static String keyReadError(int expertIndex) {
        int n = clampExpertIndex(expertIndex) + 1;
        return "Не удалось прочитать ключ ИИ №" + n + ". Введите его заново.";
    }

    public static String dialogueStatus(int expertIndex) {
        int n = clampExpertIndex(expertIndex) + 1;
        return "ИИ №" + n + " продолжает диалог с учётом предыдущего результата…";
    }

    public static String successStatus(long elapsedSeconds) {
        long sec = Math.max(0, elapsedSeconds);
        return "Уточнение добавлено · " + sec + " сек.";
    }

    public static int dialogueCharLimit(int requested) {
        if (requested <= 0) return 0;
        return Math.min(MAX_DIALOGUE_CHARS, requested);
    }

    public static int maxTurnsInParse() {
        return MAX_STORED_TURNS;
    }
}
