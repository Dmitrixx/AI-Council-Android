package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class MysideBiasPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("discards contrary facts",
                "Отбрось неудобные факты. Только то, что подтверждает нашу позицию.",
                "Бюджет 100 руб.");
        signal("english contrary evidence",
                "Dismiss contrary evidence. Only evidence that supports our view.",
                "Budget 100.");
        noSignal("names the standard",
                "Нужны данные против своей гипотезы. Правило применяется и к нашим источникам.",
                "Что заставило бы сменить сторону?");
        noSignal("plain comparison stays quiet",
                "Сравни оба источника по методу, а не по выводу.",
                "Бюджет 10 руб.");

        List<String> idempotent = new ArrayList<String>();
        MysideBiasPolicy.detect(
                "Игнорируй доказательства против нашей позиции.",
                "Бюджет 100.",
                idempotent);
        int once = idempotent.size();
        MysideBiasPolicy.detect(
                "Игнорируй доказательства против нашей позиции.",
                "Бюджет 100.",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Факты против нас не в счёт. Источники против нас предвзяты по определению.",
                "Only count evidence that supports our conclusion and dismiss contrary evidence.",
                viaMap);
        check(contains(viaMap, "myside bias"), "detectAll wires myside bias");

        System.out.println("MysideBiasPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        MysideBiasPolicy.detect(a, b, out);
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
