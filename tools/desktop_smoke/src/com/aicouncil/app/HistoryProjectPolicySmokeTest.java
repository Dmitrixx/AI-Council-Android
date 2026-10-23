package com.aicouncil.app;

public final class HistoryProjectPolicySmokeTest {
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
        check("empty filter matches all", HistoryProjectPolicy.matchesFilter("", "a", "b"));
        check("filter matches any field", HistoryProjectPolicy.matchesFilter("кот", "собака", "Котёнок"));
        check("filter rejects miss", !HistoryProjectPolicy.matchesFilter("xyz", "alpha", "beta"));
        check("null filter ok", HistoryProjectPolicy.matchesFilter(null, "x"));
        check("project name from explicit", "Мой".equals(HistoryProjectPolicy.resolveProjectName("Мой", "длинная задача")));
        check("project name from task truncated",
                HistoryProjectPolicy.resolveProjectName("  ", new String(new char[50]).replace('\0', 'я')).endsWith("…"));
        check("history capacity", HistoryProjectPolicy.historyCapacityRemaining(18) == 2
                && HistoryProjectPolicy.historyCapacityRemaining(25) == 0);
        check("projects capacity 100", HistoryProjectPolicy.projectsCapacityRemaining(0) == 100
                && HistoryProjectPolicy.maxPreviousProjectItems() == 99);
        check("history counter", "3/20".equals(HistoryProjectPolicy.historyCounterLabel(3)));
        check("history title", "2. Задача".equals(HistoryProjectPolicy.historyTitle(2, "Задача")));
        check("history meta", HistoryProjectPolicy.historyMetaLine("Совет", "01.01.26")
                .contains("Совет · 01.01.26")
                && HistoryProjectPolicy.historyMetaLine("Совет", "01.01.26")
                .contains(HistoryProjectPolicy.HISTORY_HINT));
        check("snippet newlines", "a b".equals(HistoryProjectPolicy.projectTaskSnippet("a\nb")));
        check("mode index", HistoryProjectPolicy.modeIndex("B", new String[]{"A", "B", "C"}) == 1
                && HistoryProjectPolicy.modeIndex("Z", new String[]{"A", "B"}) == 0);
        check("project opened status",
                HistoryProjectPolicy.projectOpenedStatus(true).contains("Диалог восстановлен")
                        && HistoryProjectPolicy.projectOpenedStatus(false).contains("старого проекта"));
        System.out.println("HistoryProjectPolicySmokeTest " + passed + "/" + (passed + failed));
        if (failed > 0) System.exit(1);
    }
}
