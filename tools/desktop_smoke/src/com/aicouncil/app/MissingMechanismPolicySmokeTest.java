package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class MissingMechanismPolicySmokeTest {
    private static int checks;

    public static void main(String[] args) {
        flags("Итого: рекомендую запускать, потому что рынок растёт.",
                "The market is flat.");
        quiet("Итого: рекомендую запускать, потому что если пилот удержит отток ниже 5%, шаг 2 — масштаб. Режим отказа: отток выше 8%.",
                "No recommendation here.");
        quiet("Это missing mechanism: промежуточный шаг не назван. Итого не закрываю, потому что причина без механизма.",
                "Я только называю ошибку.");
        quiet("Потому что рынок растёт, но вопрос ещё открыт и данных мало.",
                "Смотрю журнал, без рекомендации.");
        both("Final answer: we should conclude the launch because demand is high.",
                "Итого: рекомендую покупать, так как конкуренты уже сделали это.");
        if (checks < 5) fail("expected at least 5 checks, got " + checks);
        System.out.println("MissingMechanismPolicySmokeTest OK " + checks);
    }

    private static void flags(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        MissingMechanismPolicy.detect(a, b, conflicts);
        check(!conflicts.isEmpty(), "expected a missing-mechanism signal, got " + conflicts);
        check(conflicts.get(0).contains("possible signal"), "signal stays a hypothesis");
    }

    private static void both(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        MissingMechanismPolicy.detect(a, b, conflicts);
        check(conflicts.size() >= 2, "both experts should add a second signal, got " + conflicts.size());
    }

    private static void quiet(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        MissingMechanismPolicy.detect(a, b, conflicts);
        check(conflicts.isEmpty(), "mechanism, open question, or naming the gap must stay quiet, got " + conflicts);
    }

    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) fail(message);
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }
}
