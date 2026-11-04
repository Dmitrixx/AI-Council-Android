package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class OutcomeBiasPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("outcome used as proof of decision quality",
                "Раз сработало — значит решение было правильным. Успех доказывает качество выбора.",
                "");
        signal("both experts judge only by result",
                "Раз провалилось, решение было плохим.",
                "It failed so the decision was wrong.");
        signal("outcome verdict versus process view",
                "Раз выиграли — решение отличное.",
                "На момент решения информация была слабой; хороший процесс может дать плохой итог из-за удачи.");
        noSignal("explicit process and information at decision time",
                "На момент решения данные были полными, процесс был строгим; итог — отдельно.",
                "");
        noSignal("ordinary success discussion is not outcome bias",
                "Пилот дал +12% конверсии, дальше масштабируем.",
                "Согласен с пилотом.");
        noSignal("named bias plus process stays quiet",
                "Это похоже на outcome bias, но процесс на момент выбора был обоснован.",
                "");

        List<String> idempotent = new ArrayList<String>();
        OutcomeBiasPolicy.detect(
                "Раз сработало, успех доказывает правильность решения.",
                "",
                idempotent);
        int once = idempotent.size();
        OutcomeBiasPolicy.detect(
                "Раз сработало, успех доказывает правильность решения.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("OutcomeBiasPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        OutcomeBiasPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
