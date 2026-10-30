package com.aicouncil.app;

public final class LikelihoodRatioPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        String classic = LikelihoodRatioPolicy.evaluateExplicitRequest(
                "Prior 1%, likelihood ratio 20.");
        check(classic.contains("16.81%") || classic.contains("16.80%"), "classic prior * LR posterior");
        check(classic.contains("не диагноз"), "note is calculation not diagnosis");

        String odds = LikelihoodRatioPolicy.evaluateExplicitRequest(
                "Prior odds 0.01, LR+ 10.");
        check(odds.contains("9.09%") || odds.contains("9.091%"), "odds form works");

        String quiet = LikelihoodRatioPolicy.evaluateExplicitRequest(
                "Likelihood ratio is high and prior is low.");
        check(quiet.isEmpty(), "words without numbers stay quiet");

        String missing = LikelihoodRatioPolicy.evaluateExplicitRequest(
                "LR 15, but no prior.");
        check(missing.isEmpty(), "LR alone is not enough");

        String bad = LikelihoodRatioPolicy.evaluateExplicitRequest(
                "Prior 150%, LR 2.");
        check(bad.isEmpty(), "impossible prior rejected");

        if (passed != 6) {
            throw new AssertionError("LikelihoodRatioPolicySmokeTest failed: " + passed + "/6");
        }
        System.out.println("LikelihoodRatioPolicySmokeTest: " + passed + "/6 passed");
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        passed++;
    }
}
