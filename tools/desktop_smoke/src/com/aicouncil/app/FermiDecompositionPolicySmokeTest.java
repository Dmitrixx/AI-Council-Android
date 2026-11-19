package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class FermiDecompositionPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("a precise percent closes the choice",
                "Конверсия будет ровно 12.4%, поэтому выбираем этот канал. Этого достаточно чтобы закрыть выбор.");
        signal("english exact figure is treated as measured",
                "The market is exactly 4.2 billion, therefore we should enter. The figure is decisive.");
        noSignal("decomposed into factors",
                "Оценка 12.4% — это допущение: разложение Ферми, трафик × доля × оплата. Не измерение.");
        noSignal("round qualitative answer",
                "Канал выглядит слабее альтернативы. Выбор пока открыт.");
        noSignal("names the gap",
                "Fermi decomposition missing. Точное число без множителей не измерение, поэтому не закрываем выбор.");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Оценка равна 18.5%, поэтому выбираем. Этого достаточно чтобы закрыть выбор.",
                "",
                wired);
        check(contains(wired, "fermi decomposition"), "detectAll wires the fermi check");

        List<String> map = new ArrayList<String>();
        map.add("fermi decomposition (possible signal, requires context): точное число");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("множ"), "coordinator cue asks for factors");

        List<String> capped = new ArrayList<String>();
        String many = "Ровно 12.4%, поэтому выбираем. Exactly 3.1%, therefore we should. The figure is decisive.";
        FermiDecompositionPolicy.detect(many, many, capped);
        int first = capped.size();
        FermiDecompositionPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first > 0 && first <= 2, "number of signals is bounded");
        check(capped.get(0).contains("possible signal"), "signals stay hypotheses");

        System.out.println("FermiDecompositionPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String name, String text) {
        List<String> out = new ArrayList<String>();
        FermiDecompositionPolicy.detect(text, "", out);
        check(!out.isEmpty(), name);
    }

    private static void noSignal(String name, String text) {
        List<String> out = new ArrayList<String>();
        FermiDecompositionPolicy.detect(text, "", out);
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
