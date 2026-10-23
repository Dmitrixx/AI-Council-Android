package com.aicouncil.app;

public final class ResultExportPolicySmokeTest {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }

    public static void main(String[] args) {
        check("empty task → default base",
                "AI_Council_result".equals(ResultExportPolicy.resultFileBase(null))
                        && "AI_Council_result".equals(ResultExportPolicy.resultFileBase("   ")));
        check("sanitizes special chars",
                ResultExportPolicy.resultFileBase("Привет, мир! #1").matches("[A-Za-zА-Яа-яёЁіІїЇєЄґҐ0-9_-]+"));
        String longTask = new String(new char[80]).replace('\0', 'a');
        check("truncates long base", ResultExportPolicy.resultFileBase(longTask).length() <= 48);
        check("placeholder is not a result",
                !ResultExportPolicy.hasCurrentResult("Ваш итоговый ответ появится здесь…")
                        && !ResultExportPolicy.hasCurrentResult("  ")
                        && !ResultExportPolicy.hasCurrentResult(null));
        check("real answer is a result", ResultExportPolicy.hasCurrentResult("Вывод совета"));
        String plain = ResultExportPolicy.toPlainText("Задача X", "🤝 Совет", "Ответ Y", "23.10.2026");
        check("plain text includes sections",
                plain.contains("Стратегия: 🤝 Совет")
                        && plain.contains("ЗАДАЧА")
                        && plain.contains("Задача X")
                        && plain.contains("Ответ Y")
                        && plain.contains("AI Council"));
        System.out.println("ResultExportPolicySmokeTest " + passed + "/" + (passed + failed));
        if (failed > 0) System.exit(1);
    }
}
