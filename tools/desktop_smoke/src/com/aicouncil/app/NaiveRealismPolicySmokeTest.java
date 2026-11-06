package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class NaiveRealismPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("self as objective facts, others uninformed",
                "Это объективные факты. Они просто не понимают реальность.",
                "Бюджет 100 руб.");
        signal("english: anyone can see, they ignore reality",
                "The facts are clear. Anyone can see it. They just don't see the facts.",
                "Budget 100.");
        noSignal("acknowledges own perspective",
                "Это моя перспектива тоже. Возможно, я тоже не вижу всего.",
                "Они могут быть правы в своём.");
        noSignal("plain advice stays quiet",
                "Рекомендую проверить unit-экономику.",
                "Бюджет 10 руб.");

        List<String> idempotent = new ArrayList<String>();
        NaiveRealismPolicy.detect(
                "Это объективные факты. Они не понимают.",
                "Бюджет 100.",
                idempotent);
        int once = idempotent.size();
        NaiveRealismPolicy.detect(
                "Это объективные факты. Они не понимают.",
                "Бюджет 100.",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Это просто факты. Любой видит.",
                "Они игнорируют реальность.",
                viaMap);
        check(contains(viaMap, "naive realism"), "detectAll wires naive realism");

        System.out.println("NaiveRealismPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        NaiveRealismPolicy.detect(a, b, out);
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
