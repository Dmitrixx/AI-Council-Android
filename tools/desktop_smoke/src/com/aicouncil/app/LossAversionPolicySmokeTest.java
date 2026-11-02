package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class LossAversionPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("loss feels heavier so the bet is refused",
                "Неприятие потерь: потерять 100 страшнее, чем выиграть 150, поэтому отказываемся от ставки.",
                "");
        signal("both take the sure thing and skip expected value",
                "Лучше синица в руках: гарантированный выигрыш, предпочту его.",
                "Тоже лучше синица в руках и гарантированный маленький выигрыш.");
        signal("losses weigh more with no coefficient",
                "Проигрыш ощущается сильнее, потери весят больше выгод, поэтому осторожничаем.",
                "Нейтрально.");
        noSignal("coefficient and expected value are explicit",
                "Неприятие потерь учтено: коэффициент 2, матожидание положительное, доля банка по Келли.");
        noSignal("ordinary pilot is not a prospect-theory bet",
                "Бюджет пилота 12000, запускаем этап без ставки на капитал.");
        noSignal("survival constraint is not loss aversion",
                "Не стоит вкладывать весь капитал: критерий выживания важнее среднего.");
        noSignal("negated refusal does not count as a refusal",
                "Потеря страшнее выигрыша, но мы не отказываемся: матожидание посчитано.");

        List<String> idempotent = new ArrayList<String>();
        LossAversionPolicy.detect(
                "Неприятие потерь: потерять 100 страшнее, поэтому отказываемся от ставки.",
                "",
                idempotent);
        int once = idempotent.size();
        LossAversionPolicy.detect(
                "Неприятие потерь: потерять 100 страшнее, поэтому отказываемся от ставки.",
                "",
                idempotent);
        check(once > 0 && once <= 3, "signal count stays bounded");
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");

        System.out.println("LossAversionPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
        if (!out.isEmpty()) {
            check(out.get(0).contains("возможный сигнал"), label + " is marked as a hypothesis");
        }
    }

    private static void noSignal(String label, String a) {
        check(run(a, "").isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        LossAversionPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
