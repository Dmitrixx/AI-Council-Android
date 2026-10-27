package com.aicouncil.app;

/**
 * Pure rules for live streaming expert cards: titles, buffer limits, UI throttle,
 * placeholder text and heading truncation. View updates stay in the Activity.
 */
public final class StreamingResultPolicy {
    public static final int MAX_BUFFER_CHARS = 180_000;
    public static final long UI_THROTTLE_MS = 80L;
    public static final int HEADING_MAX_CHARS = 110;

    public static final String WAITING_STREAM = "Ожидает потокового ответа…";
    public static final String PLACEHOLDER_RESULT =
            "Отдельные ответы экспертов появятся здесь после выполнения задачи.";
    public static final String EMPTY_STAGE_BODY = "Текст для этого этапа отсутствует.";
    public static final String STATUS_ERROR = "Ошибка этапа";
    public static final String STATUS_INTERRUPTED = "Задача прервана";
    public static final String STATUS_PARTIAL = "Ответ частичный / возможна обрезка";
    public static final String DEFAULT_HEADING = "Ответ эксперта";

    private static final String[] STREAM_TITLES = {
            "ИИ №1 · основной эксперт",
            "ИИ №2 · независимый эксперт",
            "ИИ №3 · аудитор доказательств",
            "ИИ №4 · координатор"
    };

    private StreamingResultPolicy() {}

    /**
     * How many streaming expert cards to show for analysis mode.
     * 0 = fast (1), 2 = audit (2), else council/deep/auto (4: three experts + coordinator).
     */
    public static int streamingCardCount(int mode) {
        if (mode == 0) return 1;
        if (mode == 2) return 2;
        return 4;
    }

    public static String streamTitle(int index) {
        if (index < 0 || index >= STREAM_TITLES.length) return DEFAULT_HEADING;
        return STREAM_TITLES[index];
    }

    /** Append token into buffer respecting the hard cap; returns chars actually appended. */
    public static int appendToken(StringBuilder buffer, String token) {
        if (buffer == null) return 0;
        if (token == null || token.isEmpty()) return 0;
        if (buffer.length() >= MAX_BUFFER_CHARS) return 0;
        int available = MAX_BUFFER_CHARS - buffer.length();
        if (token.length() <= available) {
            buffer.append(token);
            return token.length();
        }
        buffer.append(token, 0, available);
        return available;
    }

    /**
     * Whether the UI should refresh for this token event.
     * Empty tokens force a refresh (flush); otherwise throttle by {@link #UI_THROTTLE_MS}.
     */
    public static boolean shouldRefreshUi(String token, long nowMs, long lastRefreshMs) {
        if (token == null || token.isEmpty()) return true;
        return nowMs - lastRefreshMs >= UI_THROTTLE_MS;
    }

    /** Body text for the streaming card detail view. */
    public static String streamingBodyDisplay(CharSequence buffer) {
        if (buffer == null || buffer.length() == 0) return WAITING_STREAM;
        return buffer.toString();
    }

    public static String truncateHeading(String heading) {
        String h = heading == null || heading.isEmpty() ? DEFAULT_HEADING : heading;
        if (h.length() > HEADING_MAX_CHARS) {
            return h.substring(0, HEADING_MAX_CHARS) + "…";
        }
        return h;
    }

    public static String stageStatusLabel(String status) {
        if ("error".equals(status)) return STATUS_ERROR;
        if ("interrupted".equals(status)) return STATUS_INTERRUPTED;
        if ("partial".equals(status)) return STATUS_PARTIAL;
        return null;
    }

    public static String stageBody(String text) {
        if (text == null || text.isEmpty()) return EMPTY_STAGE_BODY;
        return text;
    }

    /** Format duration in seconds with one decimal place. */
    public static String durationLabel(long durationMs) {
        if (durationMs < 0) return null;
        double seconds = durationMs / 1000.0;
        return "Время запросов: " + String.format(java.util.Locale.ROOT, "%.1f с", seconds);
    }

    public static boolean isPlaceholderResult(String full) {
        return full != null && full.startsWith("Ваш итоговый ответ появится здесь");
    }
}
