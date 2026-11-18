package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ColliderBiasPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("selected survivors treated as the population",
                "Among survivors the trait is rare, therefore in the population the association holds generally.",
                "");
        signal("both experts generalize from the admitted",
                "Среди поступивших признак редок, значит в популяции его почти нет.",
                "Among those admitted this is rare, therefore in the population it is rare and the association holds generally.");
        noSignal("selection effect is named",
                "Среди выживших связь видна, но это может быть Берксон: отбор создал корреляцию.",
                "");
        noSignal("selected group without a population leap",
                "Among respondents the rate looks high. We should not generalize yet.",
                "");
        noSignal("ordinary population claim",
                "В популяции базовая частота неизвестна, поэтому не закрываю.",
                "Agree. No selected subgroup is being treated as everyone.");

        List<String> idempotent = new ArrayList<String>();
        ColliderBiasPolicy.detect(
                "Among survivors the trait predicts recovery, therefore in the population the association holds generally.",
                "",
                idempotent);
        int once = idempotent.size();
        ColliderBiasPolicy.detect(
                "Among survivors the trait predicts recovery, therefore in the population the association holds generally.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("ColliderBiasPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
        if (!out.isEmpty()) {
            check(out.get(0).contains("possible signal"), label + " is marked as a hypothesis");
            check(out.get(0).contains("возможный сигнал"), label + " keeps the russian hypothesis marker");
        }
    }

    private static void noSignal(String label, String a, String b) {
        check(run(a, b).isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        ColliderBiasPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
