package com.aicouncil.app;

/** Formats a shareable Markdown report without reading or serializing provider credentials. */
public final class ResultReportFormatter {
    private ResultReportFormatter() {}

    public static String toMarkdown(String task, String strategy, String answer, String appVersion, String createdAt) {
        StringBuilder report = new StringBuilder(256
                + length(task) + length(answer) + length(strategy) + length(appVersion) + length(createdAt));
        report.append("# AI Council — отчёт\n\n");
        report.append("- **Дата:** ").append(singleLine(createdAt, "Не указана")).append('\n');
        report.append("- **Версия приложения:** ").append(singleLine(appVersion, "Не указана")).append('\n');
        report.append("- **Стратегия:** ").append(singleLine(strategy, "Не указана")).append("\n\n");
        report.append("## Задача\n\n");
        String cleanTask = trimToEmpty(task);
        report.append(cleanTask.isEmpty() ? "_Задача не указана._" : cleanTask);
        report.append("\n\n## Результат совета\n\n");
        String cleanAnswer = trimToEmpty(answer);
        report.append(cleanAnswer.isEmpty() ? "_Результат отсутствует._" : cleanAnswer);
        report.append("\n\n---\n\n");
        report.append("_Создано в AI Council для Android. Ответы моделей могут содержать ошибки; проверяйте важные факты и источники. ");
        report.append("Настройки подключений и сохранённые ключи приложения не добавляются автоматически. ");
        report.append("Текст задачи и полный результат включены в отчёт._\n");
        return report.toString();
    }

    private static String singleLine(String value, String fallback) {
        String clean = trimToEmpty(value).replace('\r', ' ').replace('\n', ' ');
        return clean.isEmpty() ? fallback : clean;
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static int length(String value) {
        return value == null ? 0 : value.length();
    }
}
