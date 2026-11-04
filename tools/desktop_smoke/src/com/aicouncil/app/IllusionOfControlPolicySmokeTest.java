package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class IllusionOfControlPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("claims control over random outcome",
                "Я контролирую исход этой сделки, удача на моей стороне.",
                "");
        signal("both experts claim personal mastery of external factors",
                "Мы контролируем рынок своими решениями.",
                "I control the outcome through my will.");
        signal("skill in one domain claimed to transfer to unrelated control",
                "Раз я хорош в продажах, значит я контролирую успех запуска и гарантирую результат.",
                "");
        noSignal("explicit mechanism and controllable factors named",
                "Контролируемые факторы названы: цена и объём. Случайный спрос остаётся внешним.",
                "");
        noSignal("ordinary planning without control claim",
                "Оценим риски и подготовим запасной план.",
                "Согласен, нужен план Б.");
        noSignal("named illusion plus domain limit stays quiet",
                "Это похоже на иллюзию контроля, но навык не переносится на внешний рынок.",
                "");

        List<String> idempotent = new ArrayList<String>();
        IllusionOfControlPolicy.detect(
                "Я контролирую исход, моя воля решает.",
                "",
                idempotent);
        int once = idempotent.size();
        IllusionOfControlPolicy.detect(
                "Я контролирую исход, моя воля решает.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("IllusionOfControlPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        IllusionOfControlPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
