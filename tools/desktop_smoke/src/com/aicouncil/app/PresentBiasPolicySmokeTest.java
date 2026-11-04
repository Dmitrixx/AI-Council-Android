package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class PresentBiasPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("near payoff without a horizon",
                "Берём деньги сейчас, потом разберёмся. Ближайшая выгода важнее плана.",
                "");
        signal("both experts take the immediate gain",
                "Синица в руках, квартальный бонус важнее.",
                "Деньги сейчас, не будем ждать отдачи.");
        signal("immediate gain versus a horizon comparison",
                "Выгода сегодня важнее, потом разберёмся.",
                "Сравниваем NPV на горизонте 5 лет при ставке дисконтирования 8%.");
        noSignal("explicit discount rate and horizon",
                "Сравниваем чистую приведённую стоимость на горизонте 5 лет, ставка дисконтирования 8%.",
                "");
        noSignal("ordinary next step is not present bias",
                "Сейчас запущу пилот и посмотрю метрику на дистанции квартала.",
                "Согласен с пилотом.");
        noSignal("named bias plus a comparison stays quiet",
                "Это похоже на present bias, но поздний итог на горизонте больше.",
                "");

        List<String> idempotent = new ArrayList<String>();
        PresentBiasPolicy.detect(
                "Ближайшая выгода важнее, деньги сейчас, потом разберёмся.",
                "",
                idempotent);
        int once = idempotent.size();
        PresentBiasPolicy.detect(
                "Ближайшая выгода важнее, деньги сейчас, потом разберёмся.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("PresentBiasPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        PresentBiasPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
