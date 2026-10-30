package com.aicouncil.app;

public final class BayesianUpdatePolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        String classic = BayesianUpdatePolicy.evaluateExplicitRequest(
                "Тест положительный. Распространённость 1%, чувствительность 99%, специфичность 95%.");
        check(classic.contains("16.67%") || classic.contains("16.68%"), "classic positive test posterior");
        check(classic.contains("не диагноз"), "note stays a hypothesis about the formula, not a diagnosis");

        String quiet = BayesianUpdatePolicy.evaluateExplicitRequest(
                "Чувствительность 99% и специфичность 95%, но базовой частоты нет.");
        check(quiet.isEmpty(), "two cues are not enough");

        String prose = BayesianUpdatePolicy.evaluateExplicitRequest(
                "Около 1% людей, высокая чувствительность и хорошая специфичность.");
        check(prose.isEmpty(), "words without a percent next to each cue stay quiet");

        String swapped = BayesianUpdatePolicy.evaluateExplicitRequest(
                "Prevalence 2%, sensitivity 90%, false positive 10%.");
        check(swapped.contains("15.52%"), "false-positive rate fills missing specificity");

        String nonsense = BayesianUpdatePolicy.evaluateExplicitRequest(
                "Распространённость 140%, чувствительность 90%, специфичность 90%.");
        check(nonsense.isEmpty(), "impossible percent is rejected");

        if (passed != 6) {
            throw new AssertionError("BayesianUpdatePolicySmokeTest failed: " + passed + "/6");
        }
        System.out.println("BayesianUpdatePolicySmokeTest: " + passed + "/6 passed");
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        passed++;
    }
}
