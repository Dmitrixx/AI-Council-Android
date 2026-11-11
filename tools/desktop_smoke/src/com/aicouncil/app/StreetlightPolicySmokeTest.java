package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class StreetlightPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("unmeasurable is treated as absent",
                "Если нельзя измерить, этого нет, поэтому показатель и есть решение.",
                "");
        signal("easy count replaces the rest",
                "Считаем только то, что легко посчитать, остальное отбрасываем.",
                "");
        signal("english measurement ontology",
                "Only what we can measure is real, so the rest can be ignored.",
                "");
        signal("streetlight decides the choice",
                "Look only under the streetlight and decide from that.",
                "");
        signal("kpi that is easy to collect",
                "Качество не важно, смотрим только на kpi который легко снять.",
                "");
        signal("both experts score the easy metric",
                "Неизмеримое не существует.",
                "If it cannot be measured it does not matter.");
        noSignal("the fallacy is named and refused",
                "Это ошибка Макнамары: не всё важное измеряется, kpi не закрывает выбор.",
                "");
        noSignal("easy is not the same as important",
                "Легко измерить не значит важно: качество остаётся, даже если его трудно снять.",
                "");
        noSignal("a metric used with an explicit gap",
                "Смотрим на kpi, но удобная метрика не заменяет важное, неизмеренный риск назван.",
                "");
        noSignal("ordinary measurement is not the fallacy",
                "В отчёте 12 процентов, диапазон 10–14, источник — внутренний учёт.",
                "");

        List<String> idempotent = new ArrayList<String>();
        StreetlightPolicy.detect("Если нельзя измерить, этого нет.", "", idempotent);
        int once = idempotent.size();
        StreetlightPolicy.detect("Если нельзя измерить, этого нет.", "", idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll("Если нельзя измерить, этого нет. Бюджет 100 руб.", "Бюджет 10 руб.", viaMap);
        check(contains(viaMap, "streetlight"), "detectAll wires streetlight");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Стоит ли выбирать подрядчика только по тому показателю, который легко снять?",
                "Если нельзя измерить, этого нет. Бюджет 100 руб.",
                "Бюджет 400 руб и другой срок окупаемости.");
        check(!ranked.conflicts.isEmpty() && ranked.conflicts.get(0).contains("streetlight"),
                "streetlight hypothesis outranks a bare number clash");
        String map = DeliberationIntelligence.formatForCoordinator(ranked);
        check(map.contains("неизмеренным") || map.contains("удобно посчитать"),
                "coordinator map refuses to close on an easy metric");

        System.out.println("StreetlightPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
        if (!out.isEmpty()) {
            check(out.get(0).contains("possible signal"), label + " is marked as a hypothesis");
        }
    }

    private static void noSignal(String label, String a, String b) {
        check(run(a, b).isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        StreetlightPolicy.detect(a, b, out);
        return out;
    }

    private static boolean contains(List<String> items, String needle) {
        for (String item : items) {
            if (item.contains(needle)) return true;
        }
        return false;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
