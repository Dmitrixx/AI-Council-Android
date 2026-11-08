package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class AssumptionLedgerPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("opposite assumptions about the same demand",
                "Допущение: спрос вырастет в следующем квартале. Поэтому имеет смысл готовить запас.",
                "Допущение: спрос упадёт в следующем квартале. Поэтому запас наращивать рано.");
        signal("recommendation with no assumption and no falsifier",
                "Рекомендую делать запуск на следующей неделе без дополнительных проверок. Это лучший шаг для команды и бюджета прямо сейчас.",
                "Бюджет около ста рублей при текущих вводных, без отдельной рекомендации.");
        noSignal("both sides name the assumption and what would flip it",
                "Допущение: канал окупается при текущем CAC. Рекомендую делать пилот. Если окажется, что отток выше 8%, это отменит вывод.",
                "Допущение: канал окупается при текущем CAC. Стоит делать короткий пилот. Если окажется, что CAC выше LTV, это отменит вывод.");
        noSignal("agreement without a recommendation stays quiet",
                "Бюджет 100 руб. при текущих допущениях задачи.",
                "Бюджет 100 рублей. Отдельной рекомендации нет.");
        noSignal("short text stays quiet",
                "Рекомендую делать.",
                "Не стоит.");

        List<String> idempotent = new ArrayList<String>();
        AssumptionLedgerPolicy.detect(
                "Допущение: спрос вырастет в следующем квартале.",
                "Допущение: спрос упадёт в следующем квартале.",
                idempotent);
        int once = idempotent.size();
        AssumptionLedgerPolicy.detect(
                "Допущение: спрос вырастет в следующем квартале.",
                "Допущение: спрос упадёт в следующем квартале.",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Допущение: спрос вырастет в следующем квартале. Готовим запас.",
                "Допущение: спрос упадёт в следующем квартале. Запас не наращиваем.",
                viaMap);
        check(contains(viaMap, "журнал допущений"), "detectAll wires the assumption ledger");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Стоит ли наращивать запас на следующий квартал, если канал ещё не проверен?",
                "Допущение: спрос вырастет в следующем квартале. Рекомендую делать запас сейчас.",
                "Допущение: спрос упадёт в следующем квартале. Не рекомендую наращивать запас.");
        String map = DeliberationIntelligence.formatForCoordinator(ranked);
        check(map.contains("журнал допущений") || map.contains("possible signal"),
                "coordinator map keeps the assumption hypothesis visible");
        check(map.contains("РЕШЕНИЕ КАРТЫ"), "resolution line still leads the map");

        System.out.println("AssumptionLedgerPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        AssumptionLedgerPolicy.detect(a, b, out);
        return out;
    }

    private static boolean contains(List<String> items, String needle) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).contains(needle)) return true;
        }
        return false;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
