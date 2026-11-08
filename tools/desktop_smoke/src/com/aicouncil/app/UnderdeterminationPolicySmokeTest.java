package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class UnderdeterminationPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("unique mechanism without a rival",
                "Это единственное возможное объяснение спада: другого объяснения нет.",
                "Бюджет обычный.");
        signal("both experts close rival mechanisms",
                "Наблюдения доказывают только это. Альтернативных механизмов нет.",
                "The only possible explanation. No other explanation fits these observations.");
        noSignal("naming a rival mechanism closes the gap",
                "Спад совместим с ценой, но другой механизм совместим с теми же данными.",
                "Нейтральный комментарий.");
        noSignal("ordinary recommendation stays quiet",
                "Рекомендую замерить отток на пилоте и решить после двух недель.",
                "Можно отложить запуск, пока канал не проверен.");
        noSignal("bare certainty is not an identifiability claim",
                "Я уверен, однозначно запускаем.",
                "Я уверен, однозначно запускаем.");

        List<String> idempotent = new ArrayList<String>();
        UnderdeterminationPolicy.detect(
                "Это единственное возможное объяснение. Другого объяснения нет.",
                "",
                idempotent);
        int once = idempotent.size();
        UnderdeterminationPolicy.detect(
                "Это единственное возможное объяснение. Другого объяснения нет.",
                "",
                idempotent);
        check(once > 0 && once <= 2, "signal count stays bounded");
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(idempotent.get(0).contains("possible signal"), "signal is marked as a hypothesis");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Это единственное возможное объяснение спада.",
                "Нейтральный ответ.",
                viaMap);
        check(contains(viaMap, "underdetermination"), "detectAll wires underdetermination");

        System.out.println("UnderdeterminationPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        UnderdeterminationPolicy.detect(a, b, out);
        return out;
    }

    private static boolean contains(List<String> lines, String part) {
        for (String line : lines) {
            if (line.contains(part)) return true;
        }
        return false;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
