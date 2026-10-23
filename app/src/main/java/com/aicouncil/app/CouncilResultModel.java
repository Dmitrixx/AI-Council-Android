package com.aicouncil.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Structured, serialisation-neutral representation of a council run. */
public final class CouncilResultModel {
    public static final class ExpertResult {
        public final String role;
        public final String heading;
        public final String status;
        public final String text;
        public final String error;
        /** -1 means the caller has not measured this phase separately. */
        public final long durationMs;
        public final boolean truncated;
        public final List<String> markers;
        public final boolean coordinator;

        ExpertResult(String role, String heading, String status, String text, String error,
                     long durationMs, boolean truncated, List<String> markers, boolean coordinator) {
            this.role = role;
            this.heading = heading;
            this.status = status;
            this.text = text;
            this.error = error;
            this.durationMs = durationMs;
            this.truncated = truncated;
            this.markers = Collections.unmodifiableList(new ArrayList<String>(markers));
            this.coordinator = coordinator;
        }
    }

    public final String task;
    public final int mode;
    public final String report;
    public final List<ExpertResult> experts;
    public final ExpertResult coordinator;
    public final boolean interrupted;

    private CouncilResultModel(String task, int mode, String report, List<ExpertResult> experts,
                               ExpertResult coordinator, boolean interrupted) {
        this.task = task == null ? "" : task;
        this.mode = Math.max(0, Math.min(4, mode));
        this.report = report == null ? "" : report;
        this.experts = Collections.unmodifiableList(new ArrayList<ExpertResult>(experts));
        this.coordinator = coordinator;
        this.interrupted = interrupted;
    }

    public static CouncilResultModel parse(String task, int mode, String report) {
        return parse(task, mode, report, null);
    }

    public static CouncilResultModel parse(String task, int mode, String report, long[] durationByExpertMs) {
        String raw = report == null ? "" : report.trim();
        List<ExpertResult> experts = new ArrayList<ExpertResult>();
        ExpertResult coordinator = null;
        boolean interrupted = raw.toLowerCase(Locale.ROOT).contains("прервана системой")
                || raw.toLowerCase(Locale.ROOT).contains("остановлено пользователем");
        String[] blocks = raw.split("\\n\\s*────────────────────\\s*\\n");
        for (int i = 0; i < blocks.length; i++) {
            String block = blocks[i].trim();
            if (block.isEmpty()) continue;
            String[] lines = block.split("\\n", 2);
            String heading = lines[0].trim();
            String body = lines.length > 1 ? lines[1].trim() : "";
            String combined = (heading + "\n" + body).toLowerCase(Locale.ROOT);
            boolean isCoordinator = heading.contains("КООРДИНАТОРА") || heading.contains("КООРДИНАТОР")
                    || heading.startsWith("🏁 ИТОГ") || heading.contains("ИТОГ КООРДИНАТОРА");
            String role = isCoordinator ? "Координатор"
                    : (heading.contains("ИИ №2") || heading.contains("НЕЗАВИСИМЫЙ АУДИТ") ? "Независимый эксперт"
                    : (heading.contains("ИИ №1") || heading.contains("ИСХОДНЫЙ ОТВЕТ") ? "Основной эксперт" : "Этап " + (i + 1)));
            boolean failed = containsAny(combined, "ошибка api", "не смог", "не получен", "не выполнена", "не удалось", "ошибка запроса");
            boolean truncated = containsAny(combined, "обрезан лимитом", "ответ обрезан", "вероятно, обрезан", "частично");
            String status = interrupted ? "interrupted" : (failed ? "error" : (truncated ? "partial" : "complete"));
            String error = failed ? body : "";
            List<String> markers = explicitMarkers(combined);
            int slot = isCoordinator ? 2 : ("Независимый эксперт".equals(role) ? 1 : 0);
            long duration = durationByExpertMs == null || slot >= durationByExpertMs.length || durationByExpertMs[slot] <= 0L ? -1L : durationByExpertMs[slot];
            ExpertResult item = new ExpertResult(role, heading, status, body, error, duration, truncated, markers, isCoordinator);
            if (isCoordinator) coordinator = item;
            else experts.add(item);
        }
        return new CouncilResultModel(task, mode, raw, experts, coordinator, interrupted);
    }

    private static List<String> explicitMarkers(String text) {
        List<String> found = new ArrayList<String>();
        addMarker(found, text, "совпадает", "Совпадает");
        addMarker(found, text, "разноглас", "Спор");
        addMarker(found, text, "источник", "Есть источник");
        addMarker(found, text, "предполож", "Предположение");
        addMarker(found, text, "неизвестно", "Неизвестно");
        return found;
    }

    private static void addMarker(List<String> found, String text, String needle, String label) {
        if (text.contains(needle)) found.add(label);
    }

    private static boolean containsAny(String text, String... terms) {
        for (String term : terms) if (text.contains(term)) return true;
        return false;
    }
}
