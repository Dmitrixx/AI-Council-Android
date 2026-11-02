package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ErgodicityPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("positive average with a full stake",
                "Ожидаемая доходность положительная, средний выигрыш выше нуля, поэтому стоит вложить весь капитал.",
                "");
        signal("levered go-ahead",
                "Матожидание положительное, берём кредитное плечо и все деньги.",
                "Нейтральный комментарий без ставки.");
        signal("repeated bets scored by the ensemble",
                "Если повторять каждый раунд, в среднем выгодно, серия ставок сходится к среднему выигрышу.",
                "");
        noSignal("kelly sizes the same bet",
                "Ожидаемая доходность положительная, но это не весь капитал: доля банка по Келли, размер позиции ограничен.",
                "");
        noSignal("explicit survival constraint",
                "В среднем плюс и кредитное плечо выглядят заманчиво, однако критерий выживания запрещает all-in.",
                "Согласен: медиана капитала важнее среднего.");
        noSignal("ordinary budget is not a ruin bet",
                "Бюджет этапа 12000, средняя маржа нормальная, запускаем пилот.",
                "Согласен с пилотом без плеча.");
        noSignal("warning already names the bankroll fraction",
                "Не стоит вкладывать весь капитал: положительное матожидание не спасает. Доля банка не задана.",
                "Размер позиции нужно ограничить.");

        List<String> idempotent = new ArrayList<String>();
        ErgodicityPolicy.detect(
                "Ожидаемая доходность положительная, поэтому стоит вложить весь капитал.",
                "Ожидаемая доходность положительная, поэтому стоит вложить весь капитал.",
                idempotent);
        int once = idempotent.size();
        ErgodicityPolicy.detect(
                "Ожидаемая доходность положительная, поэтому стоит вложить весь капитал.",
                "Ожидаемая доходность положительная, поэтому стоит вложить весь капитал.",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 4, "signal count stays bounded");

        System.out.println("ErgodicityPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        ErgodicityPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
