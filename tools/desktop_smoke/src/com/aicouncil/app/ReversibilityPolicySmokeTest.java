package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ReversibilityPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("permanent delete with no exit",
                "Удалим данные безвозвратно и закроем проект навсегда. Точки невозврата уже нет.",
                "");
        signal("both burn the boats",
                "Сжигаем мосты: one-way door, no way back, действуем необратимо.",
                "Тоже нельзя откатить. Удалить навсегда — и вперёд.");
        noSignal("pilot and rollback are named",
                "Шаг необратимый, поэтому сначала пилот на небольшой группе и явный откат, если метрика просядет. Премортем уже записан.");
        noSignal("ordinary reversible choice",
                "Берём тариф помесячно, отменить можно в любой момент.");
        noSignal("acknowledged two-way door",
                "Это two-way door: решение обратимо, можно отменить без потери капитала.");

        List<String> idempotent = new ArrayList<String>();
        ReversibilityPolicy.detect("Удалим безвозвратно, нельзя откатить.", "", idempotent);
        int once = idempotent.size();
        ReversibilityPolicy.detect("Удалим безвозвратно, нельзя откатить.", "", idempotent);
        check(once > 0 && once <= 2, "signal count stays bounded");
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(idempotent.get(0).contains("possible signal"), "marked as a hypothesis");

        System.out.println("ReversibilityPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
    }

    private static void noSignal(String label, String a) {
        check(run(a, "").isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        ReversibilityPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
