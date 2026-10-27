package com.aicouncil.app;

public final class DeterministicCalculatorPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        close(14.0, DeterministicCalculatorPolicy.evaluateExpression("2 + 3 * 4"), "operator precedence");
        close(20.0, DeterministicCalculatorPolicy.evaluateExpression("(2 + 3) * 4"), "parentheses");
        close(-7.0, DeterministicCalculatorPolicy.evaluateExpression("-3 - 4"), "unary minus");
        close(2.5, DeterministicCalculatorPolicy.evaluateExpression("5 / 2"), "division");
        close(0.125, DeterministicCalculatorPolicy.evaluateExpression("1.25e-1"), "scientific notation");
        check(DeterministicCalculatorPolicy.evaluateExplicitRequest("Посчитай 125 * 4")
                .contains("Результат: 500"), "Russian explicit cue");
        check(DeterministicCalculatorPolicy.evaluateExplicitRequest("Calculate (12 + 8) / 4?")
                .contains("Результат: 5"), "English cue and trailing punctuation");
        check(DeterministicCalculatorPolicy.evaluateExplicitRequest("Обчисли 9 - 2").contains("Результат: 7"),
                "Ukrainian explicit cue");
        check(DeterministicCalculatorPolicy.evaluateExplicitRequest("What is 2 + 2?").isEmpty(),
                "ordinary prose is not silently interpreted");
        check(DeterministicCalculatorPolicy.evaluateExplicitRequest("Посчитай 1 / 0").isEmpty(),
                "division by zero does not produce a claimed result");
        check(DeterministicCalculatorPolicy.evaluateExplicitRequest("Посчитай System.exit(0)").isEmpty(),
                "arbitrary code rejected");
        rejects("1 / (2 - 2)", "division by zero rejected");
        rejects("1 + (2 * 3", "unbalanced parentheses rejected");
        rejects("1 ** 2", "unsupported operator rejected");
        rejects("9e999", "non-finite number rejected");
        rejects("1000000000001 * 2", "out-of-range result rejected");
        System.out.println("DeterministicCalculatorPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void close(double expected, double actual, String label) {
        check(Math.abs(expected - actual) < 1.0e-9, label);
    }

    private static void rejects(String expression, String label) {
        boolean rejected = false;
        try { DeterministicCalculatorPolicy.evaluateExpression(expression); }
        catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, label);
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
