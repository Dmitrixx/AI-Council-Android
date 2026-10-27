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
        String rawDraft = cb.callApi(0, r.endpoints[0], r.models[0], r.keys[0],
                StructuredClaimPolicy.addContract("Реши задачу и явно укажи допущения, расчёты и неопределённости. Не утверждай, что использовал внешние источники, если их не было.\n\nЗАДАЧА:\n" + task
                        + DeliberationIntelligence.expertAddendum(0, r.task)),
                r.roles[0]);
        if (cb.isCancelled()) throw new InterruptedException("Выполнение остановлено пользователем");
        if (cb.isApiFailure(rawDraft)) {
            cb.onExpertState(0, "! Ответ не получен", Tone.ERROR);
            cb.onExpertState(1, "— Не запускался", Tone.MUTED);
            cb.onProgress(null, Tone.ERROR, 100);
            return "📝 ИСХОДНЫЙ ОТВЕТ НЕ ПОЛУЧЕН\nМодель: " + r.models[0] + "\n\n" + rawDraft + "\n\nПроверка не запускалась, поскольку нет исходного ответа.";
        }
        StructuredClaimPolicy.ParsedResponse parsedDraft = StructuredClaimPolicy.parseResponse(rawDraft);
        String draft = parsedDraft.answer;
        cb.onExpertState(0, "✓ Ответ готов", Tone.PRIMARY);
        cb.onExpertState(1, "◉ Проверяет", Tone.COORDINATOR);
        cb.onProgress("Этап 2 из 2 · независимый аудит", Tone.COORDINATOR, 55);
        String auditNotes = DeliberationIntelligence.localCheckFooter(draft);
        String auditPrompt = "Проведи независимый аудит ответа. Не соглашайся автоматически. Раздели вывод на: 1) подтверждённые ошибки; 2) спорные утверждения; 3) арифметику и допущения; 4) исправленный вариант. Если не можешь проверить факт без источника, прямо так и напиши.\n\nИСХОДНАЯ ЗАДАЧА:\n" + task + "\n\nОТВЕТ ДЛЯ АУДИТА:\n" + draft
                + (auditNotes.isEmpty() ? "" : "\n\n" + auditNotes + "\nУчти локальный пересчёт: он сделан приложением, а не экспертом.");
        String rawReview = cb.callApi(1, r.endpoints[1], r.models[1], r.keys[1],
                StructuredClaimPolicy.addContract(auditPrompt), r.roles[1]);
        boolean failed = cb.isApiFailure(rawReview);
        StructuredClaimPolicy.ParsedResponse parsedReview = failed ? null : StructuredClaimPolicy.parseResponse(rawReview);
        String review = parsedReview == null ? rawReview : parsedReview.answer;
        cb.onExpertState(1, failed ? "! Ошибка" : "✓ Проверено", failed ? Tone.ERROR : Tone.PRIMARY);
        cb.onProgress(null, Tone.PRIMARY, 100);
        String typedConflicts = StructuredClaimPolicy.formatConflictsForCoordinator(
                parsedDraft.claims, parsedReview == null
                        ? java.util.Collections.<ExpertClaim>emptyList() : parsedReview.claims);
        return "📝 ИСХОДНЫЙ ОТВЕТ\nМодель: " + r.models[0] + "\n\n" + draft + "\n\n🛡 НЕЗАВИСИМЫЙ АУДИТ · " + r.models[1] + "\n\n" + review
                + (typedConflicts.isEmpty() ? "" : "\n\n🧭 СТРУКТУРИРОВАННЫЕ РАЗНОГЛАСИЯ (проверьте перед выводом)\n" + typedConflicts)
                + (failed ? "\n\n⚠️ Независимый аудит не завершился успешно." : "");
    }

    private String executeCouncil(Request r, Callbacks cb, String task) throws InterruptedException {
        cb.onExpertState(0, "◉ Работает", Tone.PRIMARY);
        cb.onExpertState(1, "◉ Работает", Tone.SECONDARY);
        cb.onExpertState(2, "◉ Проверяет доказательства", Tone.SECONDARY);
        cb.onExpertState(3, "— Ожидает синтеза", Tone.MUTED);
        cb.onProgress("Этап 1 из 2 · три независимых эксперта", Tone.PRIMARY, 12);

        Future<String> first = workers.submit(() -> cb.callApi(0, r.endpoints[0], r.models[0], r.keys[0],
                StructuredClaimPolicy.addContract("Реши задачу самостоятельно. Покажи ключевые шаги, допущения и неопределённости.\n\nЗАДАЧА:\n" + task
                        + DeliberationIntelligence.expertAddendum(0, r.task)), r.roles[0]));
        Future<String> second = workers.submit(() -> cb.callApi(1, r.endpoints[1], r.models[1], r.keys[1],
                StructuredClaimPolicy.addContract("Независимо реши задачу другим способом, проверь факты и расчёты. Укажи неопределённости.\n\nЗАДАЧА:\n" + task
                        + DeliberationIntelligence.expertAddendum(1, r.task)), r.roles[1]));
        Future<String> third = workers.submit(() -> cb.callApi(2, r.endpoints[2], r.models[2], r.keys[2],
                StructuredClaimPolicy.addContract("Проведи независимый аудит доказательств и попробуй опровергнуть сильнейшие гипотезы. "
                        + "Не повторяй первые ответы: укажи альтернативные объяснения, критические недостающие данные, источник/измерение, "
                        + "которое изменило бы вывод, и самый дешёвый следующий тест.\n\nЗАДАЧА:\n" + task
                        + DeliberationIntelligence.expertAddendum(2, r.task)), r.roles[2]));

        String rawA = getFuture(first, cb), rawB = getFuture(second, cb), rawC = getFuture(third, cb);
        boolean aFailed = cb.isApiFailure(rawA), bFailed = cb.isApiFailure(rawB), cFailed = cb.isApiFailure(rawC);
        StructuredClaimPolicy.ParsedResponse parsedA = aFailed ? null : StructuredClaimPolicy.parseResponse(rawA);
        StructuredClaimPolicy.ParsedResponse parsedB = bFailed ? null : StructuredClaimPolicy.parseResponse(rawB);
        StructuredClaimPolicy.ParsedResponse parsedC = cFailed ? null : StructuredClaimPolicy.parseResponse(rawC);
        String a = parsedA == null ? rawA : parsedA.answer;
        String b = parsedB == null ? rawB : parsedB.answer;
        String c = parsedC == null ? rawC : parsedC.answer;
        java.util.List<ExpertClaim> claimsA = parsedA == null ? java.util.Collections.<ExpertClaim>emptyList() : parsedA.claims;
        java.util.List<ExpertClaim> claimsB = parsedB == null ? java.util.Collections.<ExpertClaim>emptyList() : parsedB.claims;
        java.util.List<ExpertClaim> claimsC = parsedC == null ? java.util.Collections.<ExpertClaim>emptyList() : parsedC.claims;
        cb.onExpertState(0, aFailed ? "! Ошибка" : "✓ Ответ готов", aFailed ? Tone.ERROR : Tone.PRIMARY);
        cb.onExpertState(1, bFailed ? "! Ошибка" : "✓ Ответ готов", bFailed ? Tone.ERROR : Tone.SECONDARY);
        cb.onExpertState(2, cFailed ? "! Ответ неполный" : "✓ Доказательства проверены", cFailed ? Tone.ERROR : Tone.SECONDARY);
        cb.onProgress(null, Tone.PRIMARY, 48);
        if (cb.isCancelled()) throw new InterruptedException("Выполнение остановлено пользователем");
        if (aFailed && bFailed && cFailed) {
            cb.onExpertState(3, "— Не запускался", Tone.MUTED);
            return "Три независимых эксперта не смогли завершить запрос.\n\nИИ №1 · " + r.models[0] + ":\n" + a
                    + "\n\nИИ №2 · " + r.models[1] + ":\n" + b
                    + "\n\nИИ №3 · аудитор доказательств · " + r.models[2] + ":\n" + c
                    + "\n\nКоординатор не запускался, чтобы не выдавать ошибки API за содержательные ответы.";
        }

        String critiqueA = "", critiqueB = "", critiqueC = "";
        boolean critiqueAFailed = false, critiqueBFailed = false, critiqueCFailed = false;
        if (r.mode == 3) {
            cb.onExpertState(0, aFailed ? "! Критика пропущена" : "◉ Критикует ИИ №2", aFailed ? Tone.ERROR : Tone.PRIMARY);
            cb.onExpertState(1, bFailed ? "! Критика пропущена" : "◉ Критикует ИИ №3", bFailed ? Tone.ERROR : Tone.SECONDARY);
            cb.onExpertState(2, cFailed ? "! Критика пропущена" : "◉ Критикует ИИ №1", cFailed ? Tone.ERROR : Tone.SECONDARY);
            cb.onProgress("Этап 2 из 3 · циклическая критика трёх экспертов", Tone.COORDINATOR, 53);
            final String answerA = a, answerB = b, answerC = c;
            final boolean failedA = aFailed, failedB = bFailed, failedC = cFailed;
            Future<String> critiqueAFuture = workers.submit(() -> (failedA || failedB)
                    ? "Критика пропущена: ответ проверяемого эксперта недоступен."
                    : cb.callApi(0, r.endpoints[0], r.models[0], r.keys[0],
                        CouncilAnalysisPolicy.buildCritiquePrompt(task, answerA, answerB, 1), r.roles[0]));
            Future<String> critiqueBFuture = workers.submit(() -> (failedB || failedC)
                    ? "Критика пропущена: ответ проверяемого эксперта недоступен."
                    : cb.callApi(1, r.endpoints[1], r.models[1], r.keys[1],
                        CouncilAnalysisPolicy.buildCritiquePrompt(task, answerB, answerC, 2), r.roles[1]));
            Future<String> critiqueCFuture = workers.submit(() -> (failedC || failedA)
                    ? "Критика пропущена: ответ проверяемого эксперта недоступен."
                    : cb.callApi(2, r.endpoints[2], r.models[2], r.keys[2],
                        CouncilAnalysisPolicy.buildCritiquePrompt(task, answerC, answerA, 3), r.roles[2]));
            critiqueA = getFuture(critiqueAFuture, cb);
            critiqueB = getFuture(critiqueBFuture, cb);
            critiqueC = getFuture(critiqueCFuture, cb);
            critiqueAFailed = cb.isApiFailure(critiqueA) || critiqueA.startsWith("Критика пропущена");
            critiqueBFailed = cb.isApiFailure(critiqueB) || critiqueB.startsWith("Критика пропущена");
            critiqueCFailed = cb.isApiFailure(critiqueC) || critiqueC.startsWith("Критика пропущена");
            cb.onExpertState(0, critiqueAFailed ? "! Критика неполная" : "✓ Критика готова", critiqueAFailed ? Tone.ERROR : Tone.PRIMARY);
            cb.onExpertState(1, critiqueBFailed ? "! Критика неполная" : "✓ Критика готова", critiqueBFailed ? Tone.ERROR : Tone.SECONDARY);
            cb.onExpertState(2, critiqueCFailed ? "! Критика неполная" : "✓ Критика готова", critiqueCFailed ? Tone.ERROR : Tone.SECONDARY);
            cb.onProgress(null, Tone.PRIMARY, 68);
            if (cb.isCancelled()) throw new InterruptedException("Выполнение остановлено пользователем");
        }

        cb.onExpertState(3, "◉ Сравнивает", Tone.COORDINATOR);
        cb.onProgress(r.mode == 3 ? "Этап 3 из 3 · координатор синтезирует три позиции" : "Этап 2 из 2 · координатор синтезирует три позиции", Tone.COORDINATOR, r.mode == 3 ? 78 : 66);
        String localMap = "";
        if (!aFailed && !bFailed) {
            localMap = DeliberationIntelligence.formatForCoordinator(
                    DeliberationIntelligence.analyze(r.task, a, b));
        }
        if (!bFailed && !cFailed) {
            String mapBC = DeliberationIntelligence.formatForCoordinator(
                    DeliberationIntelligence.analyze(r.task, b, c));
            if (!mapBC.isEmpty()) localMap = localMap.isEmpty() ? "КАРТА ИИ №2 ↔ ИИ №3:\n" + mapBC
                    : localMap + "\n\nКАРТА ИИ №2 ↔ ИИ №3:\n" + mapBC;
        }
        if (!aFailed && !cFailed) {
            String mapAC = StructuredClaimPolicy.formatConflictsForCoordinator(claimsA, claimsC);
            if (!mapAC.isEmpty()) localMap = localMap.isEmpty() ? "КАРТА ИИ №1 ↔ ИИ №3:\n" + mapAC
                    : localMap + "\n\nКАРТА ИИ №1 ↔ ИИ №3:\n" + mapAC;
        }
        String typedAB = StructuredClaimPolicy.formatConflictsForCoordinator(claimsA, claimsB);
        String typedBC = StructuredClaimPolicy.formatConflictsForCoordinator(claimsB, claimsC);
        String typedAC = StructuredClaimPolicy.formatConflictsForCoordinator(claimsA, claimsC);
        if (!typedAB.isEmpty()) localMap = localMap.isEmpty() ? "КАРТА ИИ №1 ↔ ИИ №2:\n" + typedAB
                : localMap + "\n\nКАРТА ИИ №1 ↔ ИИ №2:\n" + typedAB;
        if (!typedBC.isEmpty()) localMap = localMap.isEmpty() ? "КАРТА ИИ №2 ↔ ИИ №3:\n" + typedBC
                : localMap + "\n\nКАРТА ИИ №2 ↔ ИИ №3:\n" + typedBC;
        if (!typedAC.isEmpty()) localMap = localMap.isEmpty() ? "КАРТА ИИ №1 ↔ ИИ №3:\n" + typedAC
                : localMap + "\n\nКАРТА ИИ №1 ↔ ИИ №3:\n" + typedAC;
        String structuredStatus = "СТАТУС СТРУКТУРИРОВАНИЯ: эксперт A="
                + (parsedA != null && parsedA.structured ? claimsA.size() + " утверждений JSON" : "проза / JSON недоступен")
                + "; эксперт B=" + (parsedB != null && parsedB.structured ? claimsB.size() + " утверждений JSON" : "проза / JSON недоступен")
                + "; эксперт C=" + (parsedC != null && parsedC.structured ? claimsC.size() + " утверждений JSON" : "проза / JSON недоступен")
                + ". Отсутствие JSON не означает, что содержательный ответ неверен.";
        String reviewerPrompt = CouncilAnalysisPolicy.buildCoordinatorOpening()
                + (localMap.isEmpty() ? "" : "\n\nЛОКАЛЬНАЯ КАРТА РАЗНОГЛАСИЙ (посчитана приложением, не моделью; проверь её и не выдавай за внешний источник):\n" + localMap)
                + "\n\n" + structuredStatus
                + "\n\nЗАДАЧА ПОЛЬЗОВАТЕЛЯ:\n" + task
                + "\n\nОТВЕТ ИИ №1 (" + r.models[0] + "):\n" + a
                + "\n\nОТВЕТ ИИ №2 (" + r.models[1] + "):\n" + b
                + "\n\nАУДИТОР ДОКАЗАТЕЛЬСТВ · ИИ №3 (" + r.models[2] + "):\n" + c
                + (r.mode == 3 ? "\n\nВЗАИМНАЯ КРИТИКА ИИ №1 → ИИ №2:\n" + critiqueA
                    + "\n\nВЗАИМНАЯ КРИТИКА ИИ №2 → ИИ №3:\n" + critiqueB
                    + "\n\nВЗАИМНАЯ КРИТИКА ИИ №3 → ИИ №1:\n" + critiqueC
                    + "\n\nПроверь критику так же критически: не принимай замечание только потому, что оно высказано. Отдели ошибку от гипотезы и оставь неразрешённое разногласие открытым." : "")
                + "\n\nСохрани критические риски и остаточную неопределённость; не усредняй несовместимые оценки. Если веб-поиск не дал источников, не утверждай, что факты подтверждены интернетом.";
        String review = cb.callApi(3, r.endpoints[3], r.models[3], r.keys[3], reviewerPrompt, r.roles[3]);
        boolean reviewFailed = cb.isApiFailure(review);
        cb.onExpertState(3, reviewFailed ? "! Ошибка" : "✓ Итог готов", reviewFailed ? Tone.ERROR : Tone.COORDINATOR);
        cb.onProgress(null, Tone.COORDINATOR, 100);
        String result = "✦ ИИ №1 · " + r.models[0] + (aFailed ? " · ОШИБКА" : "") + "\n\n" + a
                + "\n\n────────────────────\n\n✦ ИИ №2 · " + r.models[1] + (bFailed ? " · ОШИБКА" : "") + "\n\n" + b
                + "\n\n────────────────────\n\n🔬 АУДИТ ДОКАЗАТЕЛЬСТВ · ИИ №3 · " + r.models[2] + (cFailed ? " · ОШИБКА" : "") + "\n\n" + c;
        if (r.mode == 3) result += "\n\n────────────────────\n\n🔎 ВЗАИМНАЯ КРИТИКА · ИИ №1 проверяет ИИ №2\n\n" + critiqueA
                + "\n\n────────────────────\n\n🔎 ВЗАИМНАЯ КРИТИКА · ИИ №2 проверяет ИИ №3\n\n" + critiqueB
                + "\n\n────────────────────\n\n🔎 ВЗАИМНАЯ КРИТИКА · ИИ №3 проверяет ИИ №1\n\n" + critiqueC;
        result += "\n\n────────────────────\n\n🏁 ИТОГ КООРДИНАТОРА · " + r.models[3] + "\n\n" + review
                + ((aFailed || bFailed || cFailed || reviewFailed || critiqueAFailed || critiqueBFailed || critiqueCFailed)
                ? "\n\n⚠️ ВНИМАНИЕ: один или несколько этапов завершились ошибкой либо были пропущены. Результат неполный и требует дополнительной проверки." : "");
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
