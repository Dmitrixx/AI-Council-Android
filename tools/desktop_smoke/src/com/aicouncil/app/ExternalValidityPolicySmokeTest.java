package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ExternalValidityPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        flags("Результат лаборатории переносим, поэтому внедряем как есть.",
                "Worked in their market so we therefore adopt unchanged.");
        quiet("Это external validity: переносимость не проверена. Результат лаборатории переносим, поэтому внедряем как есть.",
                "Не закрываю выбор.");
        quiet("В другой стране сработало, но здесь механизм может отличаться.",
                "Пока неясно, переносится ли результат.");
        quiet("Поэтому внедряем как есть на наших данных этого города.",
                "Масштабируем после локальной проверки, без чужого кейса.");
        both("Исследование на студентах закрывает вопрос, поэтому внедряем как есть.",
                "Пилотный город решает для всей страны, значит масштабируем без проверки.");

        List<String> idempotent = new ArrayList<String>();
        String hit = "Чужой кейс переносим как есть, рекомендую копировать без адаптации.";
        ExternalValidityPolicy.detect(hit, "заметка", idempotent);
        int once = idempotent.size();
        ExternalValidityPolicy.detect(hit, "заметка", idempotent);
        check(once > 0 && once <= 2, "signal count stays bounded");
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");

        List<String> via = new ArrayList<String>();
        DeliberationSignals.detectAll(hit, "заметка без переноса", via);
        check(contains(via, "external validity"), "detectAll wires external validity");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Стоит ли внедрять метод из чужой лаборатории?",
                hit,
                "На наших данных эффект не измерен.");
        check(contains(ranked.conflicts, "external validity"), "foreign result appears on the disagreement map");
        String map = DeliberationIntelligence.formatForCoordinator(ranked);
        check(map.contains("перенос") || map.contains("внешн") || map.contains("external validity"),
                "coordinator is told not to close on a foreign result");

        String addendum = DeliberationIntelligence.expertAddendum(1,
                "Стоит ли внедрять метод из чужой лаборатории?\n1. Что измеряли на их популяции?");
        check(addendum.contains("перенос") || addendum.contains("ПЕРЕНОС"),
                "experts are told a foreign result does not settle this case");

        System.out.println("ExternalValidityPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void flags(String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), "foreign close should signal");
        if (!out.isEmpty()) check(out.get(0).contains("possible signal"), "signal is marked as a hypothesis");
    }

    private static void both(String a, String b) {
        check(run(a, b).size() >= 2, "both experts should add a second signal");
    }

    private static void quiet(String a, String b) {
        check(run(a, b).isEmpty(), "naming the gap or a local close should stay quiet, got " + run(a, b));
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        ExternalValidityPolicy.detect(a, b, out);
        return out;
    }

    private static boolean contains(List<String> items, String needle) {
        for (String item : items) {
            if (item != null && item.contains(needle)) return true;
        }
        return false;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
