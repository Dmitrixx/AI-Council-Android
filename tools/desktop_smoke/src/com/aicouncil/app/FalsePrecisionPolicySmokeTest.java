package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class FalsePrecisionPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("hundredths without a range",
                "Вероятность успеха 73.46%.",
                "Бюджет 100 руб.");
        signal("english chance",
                "The chance of success is 63.27%.",
                "Budget 100.");
        noSignal("range acknowledges the limit",
                "Вероятность около 73.46%, диапазон 60–80%.",
                "Это не точнее диапазона.");
        noSignal("money is not a probability",
                "Бюджет 12.50 руб. Срок 3 дня.",
                "Цена 19.99.");
        noSignal("coarse percent stays quiet",
                "Вероятность успеха 70%.",
                "Шанс около половины.");

        List<String> idempotent = new ArrayList<String>();
        FalsePrecisionPolicy.detect("Вероятность 73.46%.", "Бюджет.", idempotent);
        int once = idempotent.size();
        FalsePrecisionPolicy.detect("Вероятность 73.46%.", "Бюджет.", idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Вероятность провала 12.34%.",
                "Probability of delay is 8.75%.",
                viaMap);
        check(contains(viaMap, "false precision"), "detectAll wires false precision");

        System.out.println("FalsePrecisionPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        FalsePrecisionPolicy.detect(a, b, out);
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
