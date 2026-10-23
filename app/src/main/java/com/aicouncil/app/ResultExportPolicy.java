package com.aicouncil.app;

/**
 * Pure helpers for result export filenames and plain-text reports.
 * Keeps credentials out of exported content by design (caller supplies only task/answer/strategy).
 */
public final class ResultExportPolicy {
    public static final String PLACEHOLDER_PREFIX = "Ваш итоговый ответ появится здесь";
    public static final int MAX_FILE_BASE_LENGTH = 48;
    public static final String DEFAULT_FILE_BASE = "AI_Council_result";

    private ResultExportPolicy() {}

    /** Safe filesystem base name derived from the task text. */
    public static String resultFileBase(String task) {
        String base = task == null || task.trim().isEmpty()
                ? DEFAULT_FILE_BASE
                : task.replaceAll("[^a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ0-9_-]+", "_");
        if (base.length() > MAX_FILE_BASE_LENGTH) {
            base = base.substring(0, MAX_FILE_BASE_LENGTH);
        }
        if (base.isEmpty()) {
            base = DEFAULT_FILE_BASE;
        }
        return base;
    }

    /** True when the answer is a real result (not empty / not UI placeholder). */
    public static boolean hasCurrentResult(String answer) {
        if (answer == null) return false;
        String trimmed = answer.trim();
        return !trimmed.isEmpty() && !trimmed.startsWith(PLACEHOLDER_PREFIX);
    }

    /** Plain-text report for SAF text export / share. */
    public static String toPlainText(String task, String strategy, String answer, String createdAt) {
        StringBuilder out = new StringBuilder(256
                + length(task) + length(answer) + length(strategy) + length(createdAt));
        out.append("AI Council · результат\n");
        out.append("Дата: ").append(emptyTo(createdAt, "Не указана")).append('\n');
        out.append("Стратегия: ").append(emptyTo(strategy, "Не указана")).append('\n');
        String cleanTask = trim(task);
        if (!cleanTask.isEmpty()) {
            out.append("\nЗАДАЧА\n").append(cleanTask).append('\n');
        }
        out.append('\n').append(trim(answer)).append("\n\nСоздано в AI Council для Android.\n");
        return out.toString();
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String emptyTo(String value, String fallback) {
        String clean = trim(value);
        return clean.isEmpty() ? fallback : clean;
    }

    private static int length(String value) {
        return value == null ? 0 : value.length();
    }
}
