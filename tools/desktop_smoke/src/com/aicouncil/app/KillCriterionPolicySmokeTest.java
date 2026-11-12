package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class KillCriterionPolicySmokeTest {
    private static int checks;

    public static void main(String[] args) {
        flags("Вердикт: рекомендую запускать сейчас. Вопрос закрыт.",
                "Final answer: therefore we should proceed.",
                true);
        quiet("Рекомендую подождать. Критерий опровержения: если пилот покажет отток выше 5%, вывод отменяется.",
                "No recommendation in this note.");
        quiet("Это kill criterion, а не доказательство. Вердикт без критерия опровержения не закрывает вопрос.",
                "Я только называю ошибку.");
        quiet("Пока рано решать: данных недостаточно.",
                "Нужно ещё посмотреть журнал.");
        both("Итого: рекомендую отказаться.",
                "Therefore we should conclude the purchase is worth it.");
        if (checks < 5) fail("expected at least 5 checks, got " + checks);
        System.out.println("KillCriterionPolicySmokeTest OK " + checks);
    }

    private static void flags(String a, String b, boolean either) {
        List<String> conflicts = new ArrayList<String>();
        KillCriterionPolicy.detect(a, b, conflicts);
        check(either && !conflicts.isEmpty(), "expected a kill-criterion signal");
        check(conflicts.get(0).contains("possible signal"), "signal stays a hypothesis");
    }

    private static void both(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        KillCriterionPolicy.detect(a, b, conflicts);
        check(conflicts.size() >= 2, "both experts should add a second signal, got " + conflicts.size());
    }

    private static void quiet(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        KillCriterionPolicy.detect(a, b, conflicts);
        check(conflicts.isEmpty(), "naming or a real kill-criterion must stay quiet, got " + conflicts);
    }

    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) fail(message);
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }
}
