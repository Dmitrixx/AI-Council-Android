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
        check("projects capacity", HistoryProjectPolicy.projectsCapacityRemaining(0) == 30);
        System.out.println("HistoryProjectPolicySmokeTest " + passed + "/" + (passed + failed));
        if (failed > 0) System.exit(1);
    }
}
