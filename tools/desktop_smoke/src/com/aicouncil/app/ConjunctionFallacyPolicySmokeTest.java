package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ConjunctionFallacyPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("detailed story outranks the class",
                "Конкретная история вероятнее общей: и то и другое вероятнее одного факта.",
                "");
        signal("both experts rank the conjunction higher",
                "Оба свойства вместе вероятнее, чем одно.",
                "The conjunction is more probable than either event alone.");
        signal("conjunction versus an explicit bound",
                "Детальный сценарий вероятнее класса.",
                "Конъюнкция не может быть вероятнее любой своей части.");
        noSignal("bound is named",
                "Кажется, что детальный сюжет вероятнее, но конъюнкция не может быть вероятнее класса.",
                "");
        noSignal("ordinary likelihood stays quiet",
                "Базовая частота кассиров выше, чем частота редкого сочетания.",
                "Согласен сравнить класс, а не сюжет.");
        noSignal("named fallacy plus separation stays quiet",
                "Это conjunction fallacy: a conjunction cannot exceed either event.",
                "");

        List<String> idempotent = new ArrayList<String>();
        ConjunctionFallacyPolicy.detect(
                "И то и другое вероятнее. Конкретный сценарий вероятнее общего.",
                "",
                idempotent);
        int once = idempotent.size();
        ConjunctionFallacyPolicy.detect(
                "И то и другое вероятнее. Конкретный сценарий вероятнее общего.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("ConjunctionFallacyPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
        if (!out.isEmpty()) {
            check(out.get(0).contains("возможный сигнал"), label + " is marked as a hypothesis");
        }
    }

    private static void noSignal(String label, String a, String b) {
        check(run(a, b).isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        ConjunctionFallacyPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
