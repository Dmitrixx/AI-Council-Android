package com.aicouncil.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** Autonomous strategy tests for the non-Android council orchestration engine. */
public final class CouncilExecutionEngineSmokeTest {
    private static int passed;

    public static void main(String[] args) {
        testQuickModeUsesOnlyPrimaryExpert();
        testAuditModeUsesTwoSlotsWithoutCoordinator();
        testCouncilRunsTwoExpertsAndCoordinator();
        testStructuredClaimsReachCoordinator();
        testDeepAnalysisAddsMutualCritique();
        testSingleExpertFailureDoesNotSuppressCoordinator();
        testSearchContextIsPassedAndCitedInOutput();
        testCancellationPreservesPartialReport();
        testRequestArraysAreDefensivelyCopied();
        testCallbackToneAndProgressAreReported();
        System.out.println("CouncilExecutionEngineSmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void testQuickModeUsesOnlyPrimaryExpert() {
        Fake f = new Fake();
        try {
            CouncilExecutionEngine.Outcome o = f.engine().execute(f.request(0, false), f);
            check(o.text.startsWith("⚡ БЫСТРЫЙ ОТВЕТ"), "quick mode formats its own title");
            check(f.slotCalls(0) == 1 && f.slotCalls(1) == 0 && f.slotCalls(2) == 0 && f.slotCalls(3) == 0, "quick mode calls only primary expert");
            check(o.usedSources.isEmpty(), "quick mode leaves sources empty when search is disabled");
        } finally { f.close(); }
    }

    private static void testAuditModeUsesTwoSlotsWithoutCoordinator() {
        Fake f = new Fake();
        try {
            CouncilExecutionEngine.Outcome o = f.engine().execute(f.request(2, false), f);
            check(o.text.contains("ИСХОДНЫЙ ОТВЕТ"), "audit report includes the draft answer");
            check(o.text.contains("НЕЗАВИСИМЫЙ АУДИТ"), "audit report includes independent audit");
            check(f.slotCalls(0) == 1 && f.slotCalls(1) == 1 && f.slotCalls(2) == 0 && f.slotCalls(3) == 0, "audit uses slots 1 and 2 only");
        } finally { f.close(); }
    }

    private static void testCouncilRunsTwoExpertsAndCoordinator() {
        Fake f = new Fake();
        try {
            CouncilExecutionEngine.Outcome o = f.engine().execute(f.request(1, false), f);
            check(o.text.contains("ИИ №1") && o.text.contains("ИИ №2") && o.text.contains("АУДИТ ДОКАЗАТЕЛЬСТВ · ИИ №3"), "council output keeps three independent expert answers");
            check(o.text.contains("ИТОГ КООРДИНАТОРА"), "council output has a separate coordinator section");
            check(f.slotCalls(0) == 1 && f.slotCalls(1) == 1 && f.slotCalls(2) == 1 && f.slotCalls(3) == 1, "council uses three experts and the fourth-slot coordinator");
        } finally { f.close(); }
    }

    private static void testStructuredClaimsReachCoordinator() {
        Fake f = new Fake();
        f.emitStructuredClaims = true;
        try {
            CouncilExecutionEngine.Outcome o = f.engine().execute(f.request(1, false), f);
            check(!f.promptsContaining("СТРУКТУРИРОВАННЫЕ КОНФЛИКТЫ").isEmpty(),
                    "structured conflict map reaches coordinator");
            check(!f.promptsContaining("NUMERIC_VALUE").isEmpty(), "numeric disagreement is typed");
            check(!f.promptsContaining("POLARITY").isEmpty(), "polarity disagreement is typed");
            check(o.text.contains("answer A") && !o.text.contains(StructuredClaimPolicy.START_MARKER),
                    "claim payload is hidden from rendered expert answer");
        } finally { f.close(); }
    }

    private static void testDeepAnalysisAddsMutualCritique() {
        Fake f = new Fake();
        try {
            CouncilExecutionEngine.Outcome o = f.engine().execute(f.request(3, false), f);
            check(o.text.contains("ВЗАИМНАЯ КРИТИКА · ИИ №1 проверяет ИИ №2"), "deep analysis has first critique section");
            check(o.text.contains("ВЗАИМНАЯ КРИТИКА · ИИ №2 проверяет ИИ №1"), "deep analysis has second critique section");
            check(f.promptsContaining("взаимная критика").size() >= 1, "coordinator receives critique context");
            check(f.slotCalls(2) >= 2 && f.slotCalls(3) == 1, "deep analysis runs third reviewer and fourth-slot coordinator after critique stage");
        } finally { f.close(); }
    }

    private static void testSingleExpertFailureDoesNotSuppressCoordinator() {
        Fake f = new Fake();
        f.failIndependentSecond = true;
        try {
            CouncilExecutionEngine.Outcome o = f.engine().execute(f.request(1, false), f);
            check(o.text.contains("ИИ №2 · model-b · ОШИБКА"), "failed expert is explicitly marked");
            check(o.text.contains("ВНИМАНИЕ"), "partial council report warns about the failure");
            check(f.slotCalls(3) == 1, "coordinator still runs when one expert fails");
        } finally { f.close(); }
    }

    private static void testSearchContextIsPassedAndCitedInOutput() {
        Fake f = new Fake();
        f.searchResults = "[Brave Search] Test title\nИсточник: https://example.com/test";
        try {
            CouncilExecutionEngine.Outcome o = f.engine().execute(f.request(0, true), f);
            check(f.searchCalls == 1, "enabled search is invoked exactly once");
            check(f.lastPrompt.contains("НАЙДЕННЫЕ МАТЕРИАЛЫ ИЗ ВЕБ-ПОИСКА"), "search context is appended to the model task");
            check(o.text.contains("ИСТОЧНИКИ, ВОЗВРАЩЁННЫЕ ПОИСКОМ"), "returned source block remains attached to report");
            check(o.usedSources.contains("https://example.com/test"), "outcome exposes source block separately");
        } finally { f.close(); }
    }

    private static void testCancellationPreservesPartialReport() {
        Fake f = new Fake();
        f.cancelled.set(true);
        try {
            CouncilExecutionEngine.Outcome o = f.engine().execute(f.request(0, false), f);
            check(o.cancelled, "outcome signals cancellation");
            check(o.text.equals("PARTIAL_REPORT"), "cancelled run returns the saved partial report");
            check(f.calls.isEmpty(), "cancelled run does not invoke a provider");
        } finally { f.close(); }
    }

    private static void testRequestArraysAreDefensivelyCopied() {
        String[] urls = {"u1", "u2", "u3"};
        String[] models = {"m1", "m2", "m3"};
        String[] keys = {"k1", "k2", "k3"};
        String[] roles = {"r1", "r2", "r3"};
        CouncilExecutionEngine.Request request = new CouncilExecutionEngine.Request("task", 0, urls, models, keys, roles, false, 1);
        urls[0] = "changed"; models[0] = "changed"; keys[0] = "changed"; roles[0] = "changed";
        check("u1".equals(request.endpoints[0]) && "m1".equals(request.models[0]), "endpoint/model snapshots are copied");
        check("k1".equals(request.keys[0]) && "r1".equals(request.roles[0]), "key/role snapshots are copied");
        passed += 2;
    }

    private static void testCallbackToneAndProgressAreReported() {
        Fake f = new Fake();
        try {
            f.engine().execute(f.request(0, false), f);
            check(f.progress.stream().anyMatch(x -> x.endsWith(":20")), "engine reports initial quick-mode progress");
            check(f.progress.stream().anyMatch(x -> x.endsWith(":100")), "engine reports completion progress");
            check(f.states.stream().anyMatch(x -> x.contains("✓ Готово")), "engine reports expert completion state");
        } finally { f.close(); }
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError("FAIL: " + label);
        passed++;
        System.out.println("PASS: " + label);
    }

    private static final class Fake implements CouncilExecutionEngine.Callbacks {
        final ExecutorService pool = Executors.newFixedThreadPool(4);
        final AtomicBoolean cancelled = new AtomicBoolean(false);
        final List<String> calls = Collections.synchronizedList(new ArrayList<String>());
        final List<String> progress = Collections.synchronizedList(new ArrayList<String>());
        final List<String> states = Collections.synchronizedList(new ArrayList<String>());
        volatile String lastPrompt = "";
        volatile String searchResults = "";
        volatile boolean failIndependentSecond;
        volatile boolean emitStructuredClaims;
        volatile int searchCalls;

        CouncilExecutionEngine engine() { return new CouncilExecutionEngine(pool); }
        CouncilExecutionEngine.Request request(int mode, boolean search) {
            return new CouncilExecutionEngine.Request("Test task", mode,
                    new String[]{"https://a.example/v1", "https://b.example/v1", "https://c.example/v1"},
                    new String[]{"model-a", "model-b", "model-c"},
                    new String[]{"key-a", "key-b", "key-c"}, new String[]{"role-a", "role-b", "role-c"}, search, 2);
        }
        @Override public boolean isCancelled() { return cancelled.get(); }
        @Override public String callApi(int slot, String endpoint, String model, String key, String prompt, String role) {
            calls.add(slot + "|" + prompt);
            lastPrompt = prompt;
            if (slot == 1 && failIndependentSecond && prompt.startsWith("Независимо реши")) return "Ошибка API: simulated failure";
            if (prompt.startsWith("Проведи независимый аудит")) return "audit result";
            if (emitStructuredClaims && slot == 0 && prompt.startsWith("Реши задачу самостоятельно")) return structuredAnswer("answer A", 10, "positive", "source-a");
            if (emitStructuredClaims && slot == 1 && prompt.startsWith("Независимо реши")) return structuredAnswer("answer B", 8, "negative", "source-b");
            if (prompt.startsWith("Реши задачу самостоятельно")) return "answer A";
            if (prompt.startsWith("Независимо реши")) return "answer B";
            if (prompt.contains("ОТВЕТ ДРУГОГО ЭКСПЕРТА ДЛЯ КРИТИКИ")) return "critique result";
            if (slot == 2) return "coordinator result";
            return "primary answer";
        }
        private String structuredAnswer(String answer, double value, String polarity, String sourceId) {
            String json = "{\"schema_version\":1,\"claims\":[{"
                    + "\"id\":\"revenue\",\"statement\":\"Revenue rises\","
                    + "\"numeric_value\":" + value + ",\"unit\":\"%\","
                    + "\"polarity\":\"" + polarity + "\",\"time_horizon\":\"12 months\","
                    + "\"confidence\":0.8,\"evidence_class\":\"measured_data\","
                    + "\"stakeholders\":[\"investor\"],\"reversible\":true,"
                    + "\"source_quote\":\"Revenue estimate\",\"source_id\":\"" + sourceId + "\"}]}";
            return answer + "\n\n" + StructuredClaimPolicy.START_MARKER + json + StructuredClaimPolicy.END_MARKER;
        }

        @Override public String searchWeb(String task, int contextMode) { searchCalls++; return searchResults; }
        @Override public String partialReport() { return "PARTIAL_REPORT"; }
        @Override public boolean isApiFailure(String value) {
            return value == null || value.trim().isEmpty() || value.startsWith("Ошибка") || value.startsWith("Не удалось");
        }
        @Override public void onProgress(String status, CouncilExecutionEngine.Tone tone, int value) {
            if (value >= 0) progress.add(String.valueOf(status) + ":" + value);
        }
        @Override public void onExpertState(int slot, String status, CouncilExecutionEngine.Tone tone) { states.add(slot + ":" + status + ":" + tone); }
        int slotCalls(int slot) {
            int count = 0;
            synchronized (calls) { for (String call : calls) if (call.startsWith(slot + "|")) count++; }
            return count;
        }
        List<String> promptsContaining(String text) {
            List<String> found = new ArrayList<String>();
            synchronized (calls) { for (String call : calls) if (call.toLowerCase(java.util.Locale.ROOT).contains(text.toLowerCase(java.util.Locale.ROOT))) found.add(call); }
            return found;
        }
        void close() { pool.shutdownNow(); }
    }
}
