package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class GishGallopPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("russian list is declared proof",
                "Этот список доказывает. Раз пунктов много значит верно.");
        signal("english verbosity",
                "This list of claims proves the point. Proof by verbosity.");
        signal("cannot answer every point",
                "Вы не сможете ответить на все пункты, поэтому я прав.");
        noSignal("naming the error is quiet",
                "Gish gallop. Поток утверждений не доказательство. A flood of claims is not proof.");
        noSignal("ordinary short answer is quiet",
                "Один довод: цена выросла, потому что спрос неэластичен.");
        noSignal("a sourced list is quiet", pile(true) + "\nпоэтому верно");

        signal("a bare pile closed as truth", pile(false) + "\nпоэтому верно");

        List<String> both = new ArrayList<String>();
        GishGallopPolicy.detect(
                "Поток утверждений доказывает.",
                "Quantity of arguments proves it.",
                both);
        check(contains(both, "оба эксперта"), "both sides galloping are flagged together");

        List<String> one = new ArrayList<String>();
        GishGallopPolicy.detect(
                "И ещё одно доказательство, и ещё одно доказательство.",
                "Сверяем одно наблюдение и базовую частоту.",
                one);
        check(contains(one, "один эксперт"), "a single gallop is the asymmetric form");
        check(!contains(one, "оба эксперта"), "a single gallop is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Too many points to refute therefore true.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "gish gallop"), "detectAll wires the gish gallop check");

        String decision = SynthesisProtocol.coordinatorBlock(
                "Стоит ли запускать этот продукт в этом квартале и какие риски необратимы?",
                "gish gallop");
        check(decision.contains("тезисов много"), "coordinator protocol refuses to close on a pile of claims");

        List<String> map = new ArrayList<String>();
        map.add("gish gallop (possible signal, requires context): перечень");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("пунктов много"), "coordinator cue names the pile");

        List<String> capped = new ArrayList<String>();
        String many = "Этот список доказывает. Proof by verbosity. And another thing that proves it.";
        GishGallopPolicy.detect(many, many, capped);
        int first = capped.size();
        GishGallopPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 2, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("GishGallopPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static String pile(boolean sourced) {
        StringBuilder sb = new StringBuilder();
        String[] items = {"рынок растёт", "конкуренты слабые", "команда сильная", "бренд известный", "цена низкая", "сроки короткие"};
        for (int i = 0; i < items.length; i++) {
            sb.append(i + 1).append(". ").append(items[i]);
            if (sourced && i == 0) sb.append(", источник: отчёт, 12%");
            sb.append("\n");
        }
        return sb.toString();
    }

    private static void signal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        GishGallopPolicy.detect(text, "", conflicts);
        check(contains(conflicts, "gish gallop"), label);
    }

    private static void noSignal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        GishGallopPolicy.detect(text, "", conflicts);
        check(conflicts.isEmpty(), label);
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
