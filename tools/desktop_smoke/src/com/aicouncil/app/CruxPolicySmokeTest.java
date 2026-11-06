package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class CruxPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("opposed recommendations without a switch",
                "Рекомендую делать: запускать на этой неделе.",
                "Не рекомендую. Не стоит делать этот запуск.");
        signal("english opposition without a flip",
                "I recommend doing it. Go ahead.",
                "I recommend against it. Do not proceed.");
        noSignal("both sides name the switch",
                "Стоит делать, но если окажется, что отток выше 8%, вывод изменится.",
                "Не стоит делать, если выяснится, что канал не окупается. Это и есть crux.");
        noSignal("agreement stays quiet",
                "Бюджет 100 руб. при текущих допущениях.",
                "Бюджет 100 рублей.");
        noSignal("one-sided advice stays quiet",
                "Рекомендую делать после проверки unit-экономики.",
                "Бюджет 10 руб.");

        List<String> idempotent = new ArrayList<String>();
        CruxPolicy.detect(
                "Рекомендую делать.",
                "Не рекомендую. Не стоит делать.",
                idempotent);
        int once = idempotent.size();
        CruxPolicy.detect(
                "Рекомендую делать.",
                "Не рекомендую. Не стоит делать.",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Рекомендую делать запуск.",
                "Не стоит делать. Не рекомендую.",
                viaMap);
        check(contains(viaMap, "crux"), "detectAll wires crux");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Стоит ли запускать продукт на следующей неделе без проверки канала?",
                "Рекомендую делать запуск сейчас. Бюджет 100 руб.",
                "Не рекомендую. Не стоит делать, пока канал не проверен. Бюджет 400 руб.");
        check(!ranked.conflicts.isEmpty(), "crux hypothesis appears in map");
        String map = DeliberationIntelligence.formatForCoordinator(ranked);
        check(map.contains("возможный сигнал") || map.contains("crux"),
                "coordinator map keeps the hypothesis visible");

        System.out.println("CruxPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        CruxPolicy.detect(a, b, out);
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
