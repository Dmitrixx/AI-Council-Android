package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class FalseDilemmaPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("exhaustive pair",
                "Только два варианта: запустить всё сейчас или закрыть проект. Третьего не дано.",
                "");
        signal("no alternative",
                "Других вариантов нет, только два варианта, и нужно выбрать запуск.",
                "Комментарий без рамки.");
        signal("forced pair",
                "Придётся выбрать из двух, другого выхода нет, одно из двух.",
                "");
        noSignal("third path is named",
                "Только два варианта выглядят громко, но есть третий вариант: пилот на одном потоке.",
                "");
        noSignal("status quo stays open",
                "Либо так, либо никак звучит жёстко. Оставить как есть тоже вариант, статус-кво не закрыт.",
                "");
        noSignal("negated binary",
                "Это не только два варианта. Можно начать с обратимого шага.",
                "");
        noSignal("ordinary comparison",
                "Сравнил срок и стоимость. Пилот дешевле полного запуска.",
                "Согласен сравнить ещё раз после пилота.");

        List<String> idempotent = new ArrayList<String>();
        FalseDilemmaPolicy.detect(
                "Только два варианта, третьего не дано.",
                "Только два варианта, третьего не дано.",
                idempotent);
        int once = idempotent.size();
        FalseDilemmaPolicy.detect(
                "Только два варианта, третьего не дано.",
                "Только два варианта, третьего не дано.",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 3, "signal count stays bounded");

        System.out.println("FalseDilemmaPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        FalseDilemmaPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
