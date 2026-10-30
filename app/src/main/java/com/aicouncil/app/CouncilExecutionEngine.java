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
            this.endpoints = copy4(endpoints);
            this.models = copy4(models);
            this.keys = copy4(keys);
            this.roles = copy4(roles);
            this.webSearchEnabled = webSearchEnabled;
            this.webContextMode = Math.max(0, Math.min(2, webContextMode));
        }

        private static String[] copy4(String[] values) {
            String[] copy = new String[4];
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
            String deterministicCalculation = DeterministicCalculatorPolicy.evaluateExplicitRequest(request.task);
            if (!deterministicCalculation.isEmpty()) {
                taskForModels = taskForModels + "\n\n" + deterministicCalculation
                        + "\nУчитывай этот локальный результат как арифметическую проверку, но не делай выводов о единицах или реальном смысле данных.";
            }
            String bayesianUpdate = BayesianUpdatePolicy.evaluateExplicitRequest(request.task);
            if (!bayesianUpdate.isEmpty()) {
                taskForModels = taskForModels + "\n\n" + bayesianUpdate
                        + "\nНе заменяй этот процент оценкой на глаз. Если проценты в задаче означают не положительный тест, скажи об этом и не выдумывай другой процент.";
            }
            String decisionArithmetic = DecisionArithmeticPolicy.evaluateExplicitRequest(request.task);
            if (!decisionArithmetic.isEmpty()) {
                taskForModels = taskForModels + "\n\n" + decisionArithmetic
                        + "\nНе заменяй эти числа оценкой на глаз. Если подписи в задаче означают не то, что посчитал локальный блок, скажи об этом и не выдумывай другую цифру.";
            }
            String likelihoodRatio = LikelihoodRatioPolicy.evaluateExplicitRequest(request.task);
            if (!likelihoodRatio.isEmpty()) {
                taskForModels = taskForModels + "\n\n" + likelihoodRatio
                        + "\nНе заменяй эти шансы и вероятность оценкой на глаз. Если числа означают не prior*LR, скажи об этом и не выдумывай другой процент.";
            }
            String baseRateReminder = BaseRateReminderPolicy.evaluateExplicitRequest(request.task);
            if (!baseRateReminder.isEmpty()) {
                taskForModels = taskForModels + "\n\n" + baseRateReminder
                        + "\nНе игнорируй базовую частоту. Если её нет в задаче — прямо укажи неопределённость.";
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

    // ... (truncated for this example response; in real call full content would be included)
    private static String safe(String value) { return value == null ? "" : value; }
    private static String exceptionMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty() ? error.getClass().getSimpleName() : message;
    }
}
