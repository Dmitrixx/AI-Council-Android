package com.aicouncil.app;

public final class ResultReportFormatterSmokeTest {
    public static void main(String[] args) {
        String report = ResultReportFormatter.toMarkdown(
                "Сравни два подхода", "🤝 Совет ИИ · 3 ИИ", "Вывод\n\nИсточник: https://example.org/article",
                "1.26.0", "19 окт. 2026 г., 19:00");
        check("Markdown report includes the report title and metadata", report.startsWith("# AI Council — отчёт")
                && report.contains("**Версия приложения:** 1.26.0") && report.contains("**Дата:** 19 окт. 2026 г., 19:00"));
        check("Markdown report preserves task, strategy, answer and source URL", report.contains("## Задача\n\nСравни два подхода")
                && report.contains("🤝 Совет ИИ · 3 ИИ") && report.contains("Вывод\n\nИсточник: https://example.org/article"));
        check("Task and result have separate Markdown sections", report.indexOf("## Задача") < report.indexOf("## Результат совета"));
        check("Export documents limits and credential behavior accurately", report.contains("Ответы моделей могут содержать ошибки")
                && report.contains("ключи приложения не добавляются автоматически"));
        check("Missing fields have safe fallbacks", ResultReportFormatter.toMarkdown(null, "\n", null, null, null)
                .contains("**Дата:** Не указана") && ResultReportFormatter.toMarkdown(null, "\n", null, null, null)
                .contains("_Задача не указана._") && ResultReportFormatter.toMarkdown(null, "\n", null, null, null)
                .contains("_Результат отсутствует._"));
        check("Metadata newlines cannot forge additional metadata rows", !ResultReportFormatter.toMarkdown("t", "line1\n- **Версия:** spoof", "a", "1", "today")
                .contains("**Стратегия:** line1\n- **Версия:** spoof"));
        check("Unicode and long multilingual content are preserved", ResultReportFormatter.toMarkdown("Українська: їжак; русский: ёлка", "Режим", "🔬 Результат", "1.26.0", "сьогодні")
                .contains("Українська: їжак; русский: ёлка") && ResultReportFormatter.toMarkdown("t", "Режим", "🔬 Результат", "1.26.0", "сьогодні").contains("🔬 Результат"));
        System.out.println("Result report formatter smoke tests passed: 7/7");
    }

    private static void check(String label, boolean ok) {
        if (!ok) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
}
