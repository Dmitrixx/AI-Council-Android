package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class DunningKrugerPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("self as more competent, others lack knowledge",
                "Я знаю лучше. Очевидно для компетентного. Они просто не понимают тему.",
                "Бюджет 100 руб.");
        signal("english: we are more competent",
                "We know better. Any competent person sees this. Only the incompetent disagree.",
                "Budget 100.");
        noSignal("acknowledges limits",
                "Возможно, я недооцениваю сложность. Нужна независимая калибровка.",
                "Они могут быть правы в своём.");
        noSignal("plain advice stays quiet",
                "Рекомендую проверить unit-экономику.",
                "Бюджет 10 руб.");

        List<String> idempotent = new ArrayList<String>();
        DunningKrugerPolicy.detect(
                "Я знаю лучше. Они не понимают.",
                "Бюджет 100.",
                idempotent);
        int once = idempotent.size();
        DunningKrugerPolicy.detect(
                "Я знаю лучше. Они не понимают.",
                "Бюджет 100.",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Очевидно для компетентного. Мы знаем лучше.",
                "Только некомпетентные сомневаются.",
                viaMap);
        check(contains(viaMap, "dunning-kruger"), "detectAll wires dunning-kruger");

        System.out.println("DunningKrugerPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
        if (!out.isEmpty()) {
            check(out.get(0).contains("possible signal") || out.get(0).contains("возможный сигнал"), label + " is marked as a hypothesis");
        }
    }

    private static void noSignal(String label, String a, String b) {
        check(run(a, b).isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        DunningKrugerPolicy.detect(a, b, out);
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
