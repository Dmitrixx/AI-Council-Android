package com.aicouncil.app;

/**
 * Transparent expert/progress labels for the council UI.
 * Pure strings — coloring stays in the Activity.
 */
public final class ExpertStatusPolicy {
    public static final String IDLE = "— Готов";
    public static final String WORKING = "◉ Работает";
    public static final String STREAMING = "◉ Поток…";
    public static final String DONE = "✓ Готово";
    public static final String ERROR = "! Ошибка";
    public static final String PARTIAL = "◇ Частично";
    public static final String STOPPED = "■ Остановлено";
    public static final String SKIPPED = "— Не запускался";
    public static final String COMPARING = "◉ Сравнивает";
    public static final String DIAGNOSING = "◉ Разбор сбоев";
    public static final String AUDITING = "◉ Проверяет";

    private ExpertStatusPolicy() {}

    public static String afterCall(boolean failed, boolean truncated) {
        if (failed) return ERROR;
        if (truncated) return PARTIAL;
        return DONE;
    }

    public static String progressStage(int mode, int stageIndex) {
        if (mode == 0) return "Эксперт формирует ответ…";
        if (mode == 2) {
            if (stageIndex <= 1) return "Этап 1 из 2 · исходный ответ";
            return "Этап 2 из 2 · независимый аудит";
        }
        if (mode == 3) {
            if (stageIndex <= 1) return "Этап 1 из 3 · три независимых эксперта";
            if (stageIndex == 2) return "Этап 2 из 3 · циклическая критика трёх экспертов";
            return "Этап 3 из 3 · координатор (ИИ №4)";
        }
        if (stageIndex <= 1) return "Этап 1 из 2 · три независимых эксперта";
        return "Этап 2 из 2 · координатор (ИИ №4) синтезирует ответы";
    }

    public static String finalBanner(boolean cancel, boolean totalFailure, boolean partialSuccess) {
        if (cancel) return "Остановлено · сохранён частичный текст";
        if (partialSuccess) return "Частичный успех · проверьте предупреждения";
        if (totalFailure) return "Сбой · итог может быть только диагностикой";
        return "Готово · перепроверьте важные факты";
    }
}
