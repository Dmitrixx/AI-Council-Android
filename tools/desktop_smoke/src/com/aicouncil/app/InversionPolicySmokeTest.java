package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class InversionPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("a plan that only says what to do",
                "План: надо сделать запуск. Следующие шаги ясны, поэтому делаем и решение принято.");
        signal("english proceed without a failure path",
                "Action plan: we should do the rollout. Therefore proceed and go ahead.");
        noSignal("names how it fails",
                "План: надо сделать пилот. Сценарий провала — отток за неделю. Чего избежать: полного запуска.");
        noSignal("open recommendation",
                "Есть варианты. Пока не закрываем выбор.");
        noSignal("names inversion",
                "Inversion: план без того, как это провалится, не закрывает выбор. Поэтому делаем только после проверки.");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "План: надо сделать это. Поэтому делаем, решение принято.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "inversion"), "detectAll wires the inversion check");

        String decision = SynthesisProtocol.expertBlock(
                "Стоит ли запускать этот план на всю компанию уже в этом квартале?");
        check(decision.contains("Инверсия"), "decision protocol asks for inversion");

        List<String> map = new ArrayList<String>();
        map.add("inversion (possible signal, requires context): план без провала");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().toLowerCase().contains("провал"), "coordinator cue asks how the plan fails");

        List<String> capped = new ArrayList<String>();
        String many = "План: надо сделать. Поэтому делаем, решение принято. Action plan: we should do it. Go ahead.";
        InversionPolicy.detect(many, many, capped);
        int first = capped.size();
        InversionPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first > 0 && first <= 2, "number of signals is bounded");

        System.out.println("InversionPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String name, String text) {
        List<String> out = new ArrayList<String>();
        InversionPolicy.detect(text, "", out);
        check(!out.isEmpty(), name);
    }

    private static void noSignal(String name, String text) {
        List<String> out = new ArrayList<String>();
        InversionPolicy.detect(text, "", out);
        check(out.isEmpty(), name);
    }

    private static boolean contains(List<String> items, String needle) {
        for (String item : items) {
            if (item.contains(needle)) return true;
        }
        return false;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
