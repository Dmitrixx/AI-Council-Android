package com.aicouncil.app;

/**
 * Pure rules for history / project list filtering, naming, size limits and list labels.
 * Persistence and Android views stay in the Activity.
 */
public final class HistoryProjectPolicy {
    public static final int MAX_HISTORY = 20;
    /** Matches MainActivity project store (newest + up to 99 previous). */
    public static final int MAX_PROJECTS = 100;
    public static final int MAX_PROJECT_NAME_LENGTH = 42;

    public static final String EMPTY_HISTORY = "История пока пуста. Выполненные задачи появятся здесь.";
    public static final String EMPTY_PROJECTS_FILTER = "Нет проектов по этому запросу. Сохраните текущую задачу выше.";
    public static final String PROJECTS_READ_ERROR = "Не удалось прочитать проекты.";
    public static final String HISTORY_HINT = "Нажмите, чтобы восстановить ответ";

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

    /** How many older items to keep when prepending one new entry. */
    public static int maxPreviousHistoryItems() {
        return Math.max(0, MAX_HISTORY - 1);
    }

    public static int maxPreviousProjectItems() {
        return Math.max(0, MAX_PROJECTS - 1);
    }

    public static int historyCapacityRemaining(int currentLength) {
        if (currentLength < 0) return MAX_HISTORY;
        return Math.max(0, MAX_HISTORY - currentLength);
    }

    public static int projectsCapacityRemaining(int currentLength) {
        if (currentLength < 0) return MAX_PROJECTS;
        return Math.max(0, MAX_PROJECTS - currentLength);
    }

    /** Counter label e.g. {@code 3/20}. */
    public static String historyCounterLabel(int count) {
        int n = Math.max(0, count);
        return n + "/" + MAX_HISTORY;
    }

    public static String historyTitle(int oneBasedIndex, String task) {
        String t = task == null ? "" : task;
        return oneBasedIndex + ". " + t;
    }

    public static String historyMetaLine(String mode, String date) {
        String m = mode == null ? "" : mode;
        String d = date == null ? "" : date;
        return m + " · " + d + "\n" + HISTORY_HINT;
    }

    /** Single-line task snippet for project cards. */
    public static String projectTaskSnippet(String task) {
        if (task == null) return "";
        return task.replace('\n', ' ').replace('\r', ' ').trim();
    }

    public static String projectOpenedStatus(boolean dialogueRestored) {
        if (dialogueRestored) {
            return "Проект открыт. Диалог восстановлен.";
        }
        return "Проект открыт. Для старого проекта диалог начнётся с текущего результата.";
    }

    /**
     * Resolve a stored mode label to an index in {@code modes}; default 0 if unknown.
     */
    public static int modeIndex(String storedMode, String[] modes) {
        if (modes == null || modes.length == 0) return 0;
        if (storedMode == null) return 0;
        for (int i = 0; i < modes.length; i++) {
            if (storedMode.equals(modes[i])) return i;
        }
        return 0;
    }
}
