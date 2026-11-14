package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ConfoundingPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("correlation closed as cause",
                "Продажи и реклама коррелируют, значит причина — реклама, это и есть причина роста.",
                "");
        signal("both experts close on co-movement",
                "They move together, so it causes the outcome. This is the cause.",
                "Показатели совместно меняются, значит причина найдена, одно вызывает другое.");
        noSignal("control is named",
                "Есть корреляция, но мешающий фактор не снят: после контроля связь слабеет.",
                "");
        noSignal("gap is named",
                "This is confounding: correlation proves causation is the wrong reading.",
                "");
        noSignal("association without a causal close",
                "Выручка и сезон коррелируют. Пока только совместное движение, причину не называю.",
                "They are associated. I am not claiming a cause.");
        noSignal("ordinary cause with a mechanism",
                "Насос остановился, потому что сгорел предохранитель. Заменим его и проверим давление.",
                "");

        List<String> idempotent = new ArrayList<String>();
        ConfoundingPolicy.detect(
                "Correlation, therefore causes the drop. This is the cause.",
                "",
                idempotent);
        int once = idempotent.size();
        ConfoundingPolicy.detect(
                "Correlation, therefore causes the drop. This is the cause.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("ConfoundingPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        ConfoundingPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
