package com.aicouncil.app;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Coordinates the five council strategies without depending on Android UI classes.
 * The Activity supplies network/search adapters and posts presentation callbacks to the main thread.
 */
public final class CouncilExecutionEngine {
    public enum Tone { PRIMARY, SECONDARY, COORDINATOR, ERROR, MUTED }

    public interface Callbacks {
        boolean isCancelled();
        String callApi(int slot, String endpoint, String model, String key, String prompt, String roleInstruction);
        String searchWeb(String task, int contextMode);
        String partialReport();
        boolean isApiFailure(String value);
        void onProgress(String status, Tone tone, int progress);
        void onExpertState(int slot, String status, Tone tone);
    }

    public static final class Request {
        public final String task;
        public final int mode;
        public final String[] endpoints;
        public final String[] models;
        public final String[] keys;
        public final String[] roles;
        public final boolean webSearchEnabled;
        public final int webContextMode;

        public Request(String task, int mode, String[] endpoints, String[] models, String[] keys,
                       String[] roles, boolean webSearchEnabled, int webContextMode) {
            this.task = task == null ? "" : task;
            this.mode = Math.max(0, Math.min(3, mode));
            this.endpoints = copy3(endpoints);
            this.models = copy3(models);
            this.keys = copy3(keys);
            this.roles = copy3(roles);
            this.webSearchEnabled = webSearchEnabled;
            this.webContextMode = Math.max(0, Math.min(2, webContextMode));
        }

        private static String[] copy3(String[] values) {
            String[] copy = new String[3];
            for (int i = 0; i < copy.length; i++) copy[i] = values != null && i < values.length && values[i] != null ? values[i] : "";
            return copy;
        }
    }

    public static final class Outcome {
        public final String text;
        public final String usedSources;
        public final boolean cancelled;
        Outcome(String text, String usedSources, boolean cancelled) {
            this.text = text == null ? "" : text;
            this.usedSources = usedSources == null ? "" : usedSources;
            this.cancelled = cancelled;
        }
    }

    private final ExecutorService workers;

    public CouncilExecutionEngine(ExecutorService workers) {
        if (workers == null) throw new IllegalArgumentException("workers must not be null");
        this.workers = workers;
    }

    public Outcome execute(Request request, Callbacks callbacks) {
        if (request == null || callbacks == null) throw new IllegalArgumentException("request and callbacks are required");
        String result;
        String taskForModels = request.task;
        String usedSources = "";
        try {
            if (request.webSearchEnabled) {
                callbacks.onProgress("Поиск открытых источников…", Tone.PRIMARY, -1);
                String refs = safe(callbacks.searchWeb(request.task, request.webContextMode));
                usedSources = refs;
                if (!refs.isEmpty()) {
                    taskForModels = request.task + "\n\nНАЙДЕННЫЕ МАТЕРИАЛЫ ИЗ ВЕБ-ПОИСКА (проверь соответствие источников утверждениям, не считай сниппеты доказательством):\n" + refs;
                } else {
                    taskForModels = request.task + "\n\nВеб-поиск не вернул доступных материалов. Не утверждай, что источники были проверены.";
                }
            }
            if (callbacks.isCancelled()) throw new InterruptedException("Выполнение остановлено пользователем");
            String hint = TaskRoutingPolicy.toolAwarenessHint(request.task);
            if (!hint.isEmpty()) {
                taskForModels = taskForModels + "\n\nПОДСКАЗКА ПРИЛОЖЕНИЯ (не источник фактов):\n" + hint;
            }

            if (request.mode == 0) {
                callbacks.onExpertState(0, "◉ Работает", Tone.PRIMARY);
                callbacks.onProgress("Эксперт 1 формирует ответ…", Tone.PRIMARY, 20);
                String quick = callbacks.callApi(0, request.endpoints[0], request.models[0], request.keys[0], taskForModels, request.roles[0]);
                boolean failed = callbacks.isApiFailure(quick);
                callbacks.onExpertState(0, failed ? "! Ошибка" : "✓ Готово", failed ? Tone.ERROR : Tone.PRIMARY);
                callbacks.onProgress(null, Tone.PRIMARY, 100);
                result = "⚡ БЫСТРЫЙ ОТВЕТ — " + request.models[0] + "\n\n" + quick + (failed ? "\n\n⚠️ Запрос не завершился успешно." : "");
            } else if (request.mode == 2) {
                result = executeAudit(request, callbacks, taskForModels);
            } else {
                result = executeCouncil(request, callbacks, taskForModels);
            }
        } catch (Exception error) {
            result = callbacks.isCancelled() ? safe(callbacks.partialReport()) : "Не удалось выполнить задачу: " + exceptionMessage(error);
        }
        if (callbacks.isCancelled() && result.trim().isEmpty()) result = safe(callbacks.partialReport());
        if (request.webSearchEnabled && !usedSources.isEmpty()) result += "\n\n🌐 ИСТОЧНИКИ, ВОЗВРАЩЁННЫЕ ПОИСКОМ\n" + usedSources;
        if (!callbacks.isCancelled()) {
            String localCheck = DeliberationIntelligence.localCheckFooter(result);
            if (!localCheck.isEmpty()) result += "\n\n" + localCheck;
        }
        return new Outcome(result, usedSources, callbacks.isCancelled());
    }

    private String executeAudit(Request r, Callbacks cb, String task) throws InterruptedException {
        cb.onExpertState(0, "◉ Решает задачу", Tone.PRIMARY);
        cb.onProgress("Этап 1 из 2 · исходный ответ", Tone.PRIMARY, 15);
        String draft = cb.callApi(0, r.endpoints[0], r.models[0], r.keys[0],
                "Реши задачу и явно укажи допущения, расчёты и неопределённости. Не утверждай, что использовал внешние источники, если их не было.\n\nЗАДАЧА:\n" + task
                        + DeliberationIntelligence.expertAddendum(0, r.task),
                r.roles[0]);
        if (cb.isCancelled()) throw new InterruptedException("Выполнение остановлено пользователем");
        if (cb.isApiFailure(draft)) {
            cb.onExpertState(0, "! Ответ не получен", Tone.ERROR);
            cb.onExpertState(1, "— Не запускался", Tone.MUTED);
            cb.onProgress(null, Tone.ERROR, 100);
            return "📝 ИСХОДНЫЙ ОТВЕТ НЕ ПОЛУЧЕН\nМодель: " + r.models[0] + "\n\n" + draft + "\n\nПроверка не запускалась, поскольку нет исходного ответа.";
        }
        cb.onExpertState(0, "✓ Ответ готов", Tone.PRIMARY);
        cb.onExpertState(1, "◉ Проверяет", Tone.COORDINATOR);
        cb.onProgress("Этап 2 из 2 · независимый аудит", Tone.COORDINATOR, 55);
        String auditNotes = DeliberationIntelligence.localCheckFooter(draft);
        String review = cb.callApi(1, r.endpoints[1], r.models[1], r.keys[1],
                "Проведи независимый аудит ответа. Не соглашайся автоматически. Раздели вывод на: 1) подтверждённые ошибки; 2) спорные утверждения; 3) арифметику и допущения; 4) исправленный вариант. Если не можешь проверить факт без источника, прямо так и напиши.\n\nИСХОДНАЯ ЗАДАЧА:\n" + task + "\n\nОТВЕТ ДЛЯ АУДИТА:\n" + draft
                        + (auditNotes.isEmpty() ? "" : "\n\n" + auditNotes + "\nУчти локальный пересчёт: он сделан приложением, а не экспертом."),
                r.roles[1]);
        boolean failed = cb.isApiFailure(review);
        cb.onExpertState(1, failed ? "! Ошибка" : "✓ Проверено", failed ? Tone.ERROR : Tone.PRIMARY);
        cb.onProgress(null, Tone.PRIMARY, 100);
        return "📝 ИСХОДНЫЙ ОТВЕТ\nМодель: " + r.models[0] + "\n\n" + draft + "\n\n🛡 НЕЗАВИСИМЫЙ АУДИТ · " + r.models[1] + "\n\n" + review
                + (failed ? "\n\n⚠️ Независимый аудит не завершился успешно." : "");
    }

    private String executeCouncil(Request r, Callbacks cb, String task) throws InterruptedException {
        cb.onExpertState(0, "◉ Работает", Tone.PRIMARY);
        cb.onExpertState(1, "◉ Работает", Tone.SECONDARY);
        cb.onProgress("Этап 1 из 3 · независимые ответы", Tone.PRIMARY, 15);
        Future<String> first = workers.submit(() -> cb.callApi(0, r.endpoints[0], r.models[0], r.keys[0],
                "Реши задачу самостоятельно. Покажи ключевые шаги, допущения и неопределённости.\n\nЗАДАЧА:\n" + task
                        + DeliberationIntelligence.expertAddendum(0, r.task), r.roles[0]));
        Future<String> second = workers.submit(() -> cb.callApi(1, r.endpoints[1], r.models[1], r.keys[1],
                "Независимо реши задачу другим способом, проверь факты и расчёты. Укажи неопределённости.\n\nЗАДАЧА:\n" + task
                        + DeliberationIntelligence.expertAddendum(1, r.task), r.roles[1]));
        String a = getFuture(first, cb), b = getFuture(second, cb);
        boolean aFailed = cb.isApiFailure(a), bFailed = cb.isApiFailure(b);
        cb.onExpertState(0, aFailed ? "! Ошибка" : "✓ Ответ готов", aFailed ? Tone.ERROR : Tone.PRIMARY);
        cb.onExpertState(1, bFailed ? "! Ошибка" : "✓ Ответ готов", bFailed ? Tone.ERROR : Tone.SECONDARY);
        cb.onProgress(null, Tone.PRIMARY, 55);
        if (cb.isCancelled()) throw new InterruptedException("Выполнение остановлено пользователем");

        String critiqueA = "", critiqueB = "";
        boolean critiqueAFailed = false, critiqueBFailed = false;
        if (aFailed && bFailed) {
            return "Оба независимых эксперта не смогли завершить запрос.\n\nИИ №1 · " + r.models[0] + ":\n" + a
                    + "\n\nИИ №2 · " + r.models[1] + ":\n" + b
                    + "\n\nКоординатор не запускался, чтобы не выдавать ошибки API за содержательные ответы.";
        }
        if (r.mode == 3) {
            cb.onExpertState(0, aFailed ? "! Ответ неполный" : "◉ Критикует ответ №2", aFailed ? Tone.ERROR : Tone.PRIMARY);
            cb.onExpertState(1, bFailed ? "! Ответ неполный" : "◉ Критикует ответ №1", bFailed ? Tone.ERROR : Tone.SECONDARY);
            cb.onProgress("Этап 2 из 3 · взаимная критика независимых ответов", Tone.COORDINATOR, 55);
            final String answerA = a, answerB = b;
            Future<String> critiqueAFuture = workers.submit(() -> aFailed
                    ? "Критика ИИ №2 не выполнена: исходный ответ ИИ №1 недоступен."
                    : cb.callApi(0, r.endpoints[0], r.models[0], r.keys[0], CouncilAnalysisPolicy.buildCritiquePrompt(task, answerA, answerB, 1), r.roles[0]));
            Future<String> critiqueBFuture = workers.submit(() -> bFailed
                    ? "Критика ИИ №1 не выполнена: исходный ответ ИИ №2 недоступен."
                    : cb.callApi(1, r.endpoints[1], r.models[1], r.keys[1], CouncilAnalysisPolicy.buildCritiquePrompt(task, answerB, answerA, 2), r.roles[1]));
            critiqueA = getFuture(critiqueAFuture, cb);
            critiqueB = getFuture(critiqueBFuture, cb);
            critiqueAFailed = cb.isApiFailure(critiqueA) || critiqueA.startsWith("Критика ИИ №2 не выполнена");
            critiqueBFailed = cb.isApiFailure(critiqueB) || critiqueB.startsWith("Критика ИИ №1 не выполнена");
            cb.onExpertState(0, critiqueAFailed ? "! Критика неполная" : "✓ Критика готова", critiqueAFailed ? Tone.ERROR : Tone.PRIMARY);
            cb.onExpertState(1, critiqueBFailed ? "! Критика неполная" : "✓ Критика готова", critiqueBFailed ? Tone.ERROR : Tone.SECONDARY);
            cb.onProgress(null, Tone.PRIMARY, 68);
            if (cb.isCancelled()) throw new InterruptedException("Выполнение остановлено пользователем");
        }
        cb.onExpertState(2, "◉ Сравнивает", Tone.COORDINATOR);
        cb.onProgress(r.mode == 3 ? "Этап 3 из 3 · координатор проверяет ответы и критику" : "Этап 2 из 2 · координатор сравнивает ответы", Tone.COORDINATOR, r.mode == 3 ? 78 : 70);
        String localMap = "";
        if (!(aFailed && bFailed)) {
            localMap = DeliberationIntelligence.formatForCoordinator(
                    DeliberationIntelligence.analyze(r.task, aFailed ? "" : a, bFailed ? "" : b));
            String graphNote = ClaimGraph.noteForCoordinator(
                    ClaimGraph.fromAnswers(aFailed ? "" : a, bFailed ? "" : b));
            if (!graphNote.isEmpty()) {
                localMap = localMap.isEmpty() ? graphNote : localMap + "\n\n" + graphNote;
            }
        }
        String reviewerPrompt = CouncilAnalysisPolicy.buildCoordinatorOpening() +
                (localMap.isEmpty() ? "" : "\n\nЛОКАЛЬНАЯ КАРТА РАЗНОГЛАСИЙ (посчитана приложением, не моделью; проверь её и не выдавай за внешний источник):\n" + localMap) +
                "\n\nЗАДАЧА ПОЛЬЗОВАТЕЛЯ:\n" + task + "\n\nОТВЕТ ИИ №1 (" + r.models[0] + "):\n" + a + "\n\nОТВЕТ ИИ №2 (" + r.models[1] + "):\n" + b +
                (r.mode == 3 ? "\n\nВЗАИМНАЯ КРИТИКА ИИ №1:\n" + critiqueA + "\n\nВЗАИМНАЯ КРИТИКА ИИ №2:\n" + critiqueB +
                        "\n\nОцени критику так же критически: не принимай замечание только потому, что оно высказано. Отдели найденную ошибку от гипотезы, а неразрешённое разногласие оставь явно открытым. Учитывай base rates и то, что изменило бы позицию." : "") +
                "\n\nЕсли веб-поиск не дал источников, не заявляй, что факты подтверждены интернетом. Явно укажи остаточный риск и что изменило бы итоговый вывод.";
        String review = cb.callApi(2, r.endpoints[2], r.models[2], r.keys[2], reviewerPrompt, r.roles[2]);
        boolean reviewFailed = cb.isApiFailure(review);
        cb.onExpertState(2, reviewFailed ? "! Ошибка" : "✓ Итог готов", reviewFailed ? Tone.ERROR : Tone.COORDINATOR);
        cb.onProgress(null, Tone.COORDINATOR, 100);
        String result = "✦ ИИ №1 · " + r.models[0] + (aFailed ? " · ОШИБКА" : "") + "\n\n" + a
                + "\n\n────────────────────\n\n✦ ИИ №2 · " + r.models[1] + (bFailed ? " · ОШИБКА" : "") + "\n\n" + b;
        if (r.mode == 3) result += "\n\n────────────────────\n\n🔎 ВЗАИМНАЯ КРИТИКА · ИИ №1 проверяет ИИ №2\n\n" + critiqueA
                + "\n\n────────────────────\n\n🔎 ВЗАИМНАЯ КРИТИКА · ИИ №2 проверяет ИИ №1\n\n" + critiqueB;
        result += "\n\n────────────────────\n\n🏁 ИТОГ КООРДИНАТОРА · " + r.models[2] + "\n\n" + review
                + ((aFailed || bFailed || reviewFailed || critiqueAFailed || critiqueBFailed)
                ? "\n\n⚠️ ВНИМАНИЕ: один или несколько этапов завершились ошибкой либо пропущены. Результат неполный и требует дополнительной проверки." : "");
        return result;
    }

    private static String getFuture(Future<String> future, Callbacks callbacks) {
        try {
            return future.get(100, TimeUnit.SECONDS);
        } catch (InterruptedException interrupted) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            return callbacks.isCancelled() ? "Остановлено пользователем." : "Не удалось получить ответ: ожидание прервано.";
        } catch (TimeoutException timeout) {
            future.cancel(true);
            return callbacks.isCancelled() ? "Остановлено пользователем." : "Не удалось получить ответ: ожидание превысило 100 секунд.";
        } catch (CancellationException cancelled) {
            return callbacks.isCancelled() ? "Остановлено пользователем." : "Не удалось получить ответ: запрос был отменён.";
        } catch (ExecutionException error) {
            Throwable cause = error.getCause();
            String detail = cause == null ? "неизвестная ошибка" : cause.getMessage();
            return callbacks.isCancelled() ? "Остановлено пользователем." : "Не удалось получить ответ: "
                    + (detail == null || detail.trim().isEmpty() ? "ошибка фоновой задачи" : detail);
        }
    }

    private static String safe(String value) { return value == null ? "" : value; }
    private static String exceptionMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty() ? error.getClass().getSimpleName() : message;
    }
}
