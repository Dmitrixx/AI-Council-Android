package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class OmissionBiasPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("inaction declared safer",
                "Бездействие безопаснее. Лучше не действовать, так безопаснее для всех.",
                "");
        signal("both experts treat omission as safety",
                "Ничего не делать безопаснее, бездействие не причиняет вреда.",
                "Doing nothing is safer. Inaction is safer than a new program.");
        signal("omission versus harm of not acting",
                "Безопаснее ничего не делать.",
                "Ущерб от бездействия выше: сравним действие и бездействие по смертям.");
        noSignal("harm of inaction is named",
                "Бездействие безопаснее звучит, но вред от бездействия уже посчитан.",
                "");
        noSignal("ordinary wait is not omission bias",
                "Подождём отчёт в пятницу и тогда решим.",
                "Согласен подождать отчёт.");
        noSignal("named bias plus comparison stays quiet",
                "Это похоже на omission bias, но ущерб от бездействия больше.",
                "");

        List<String> idempotent = new ArrayList<String>();
        OmissionBiasPolicy.detect(
                "Бездействие безопаснее, ничего не делать безопаснее.",
                "",
                idempotent);
        int once = idempotent.size();
        OmissionBiasPolicy.detect(
                "Бездействие безопаснее, ничего не делать безопаснее.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("OmissionBiasPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        OmissionBiasPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
