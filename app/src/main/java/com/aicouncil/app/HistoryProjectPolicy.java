package com.aicouncil.app;

/**
 * Pure rules for history / project list filtering, naming and size limits.
 * Persistence stays in LocalDataStore; this class does not touch Android APIs.
 */
public final class HistoryProjectPolicy {
    public static final int MAX_HISTORY = 20;
    public static final int MAX_PROJECTS = 30;
    public static final int MAX_PROJECT_NAME_LENGTH = 42;

    private HistoryProjectPolicy() {}

    /** Case-insensitive contains match for search filters (null-safe). */
    public static boolean matchesFilter(String filter, String... fields) {
        String q = filter == null ? "" : filter.trim().toLowerCase();
        if (q.isEmpty()) return true;
        if (fields == null) return false;
        for (String field : fields) {
            if (field != null && field.toLowerCase().contains(q)) return true;
        }
        return false;
    }

    /** Truncate a project display name from an explicit name or the task text. */
    public static String resolveProjectName(String explicitName, String task) {
        String name = explicitName == null ? "" : explicitName.trim();
        if (!name.isEmpty()) return name;
        String t = task == null ? "" : task.trim();
        if (t.isEmpty()) return "Проект";
        if (t.length() > MAX_PROJECT_NAME_LENGTH) {
            return t.substring(0, MAX_PROJECT_NAME_LENGTH) + "…";
        }
        return t;
    }

    /** Remaining slots after current length (clamped). */
    public static int historyCapacityRemaining(int currentLength) {
        if (currentLength < 0) return MAX_HISTORY;
        return Math.max(0, MAX_HISTORY - currentLength);
    }

    public static int projectsCapacityRemaining(int currentLength) {
        if (currentLength < 0) return MAX_PROJECTS;
        return Math.max(0, MAX_PROJECTS - currentLength);
    }
}
