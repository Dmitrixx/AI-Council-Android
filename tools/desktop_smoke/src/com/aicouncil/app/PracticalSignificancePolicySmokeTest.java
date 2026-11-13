package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class PracticalSignificancePolicySmokeTest {
    private static int checks;

    public static void main(String[] args) {
        flags("Результат статистически значим (p < 0.05), поэтому рекомендую запускать.",
                "It is statistically significant, therefore we should ship.",
                true);
        quiet("Статистически значимо, но размер эффекта мал (d = 0.12). Не рекомендую запускать.",
                "Statistically significant, effect size is small, so we should not ship.");
        quiet("Это practical significance, а не доказательство. Статистическая значимость не практическая.",
                "I am only naming the error: a p-value is not a decision.");
        quiet("Пока рано решать: данных недостаточно.",
                "Need a larger sample before any claim.");
        both("Различие значимо, значит выбираем вариант A.",
                "Statistically significant so we should choose A.");
        if (checks < 5) fail("expected at least 5 checks, got " + checks);
        System.out.println("PracticalSignificancePolicySmokeTest OK " + checks);
    }

    private static void flags(String a, String b, boolean either) {
        List<String> conflicts = new ArrayList<String>();
        PracticalSignificancePolicy.detect(a, b, conflicts);
        check(either && !conflicts.isEmpty(), "expected a practical-significance signal, got " + conflicts);
        check(conflicts.get(0).contains("possible signal"), "signal stays a hypothesis");
    }

    private static void both(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        PracticalSignificancePolicy.detect(a, b, conflicts);
        check(conflicts.size() >= 2, "both experts should add a second signal, got " + conflicts.size());
    }

    private static void quiet(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        PracticalSignificancePolicy.detect(a, b, conflicts);
        check(conflicts.isEmpty(), "magnitude or naming the error must stay quiet, got " + conflicts);
    }

    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) fail(message);
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }
}
