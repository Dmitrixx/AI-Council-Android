package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class RelativePrivationPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("russian not as bad",
                "Не так плохо, потому что есть хуже. Есть проблемы похуже.");
        signal("english relative privation",
                "At least it is not as bad as the alternative. There are worse problems.");
        signal("not the worst",
                "Не самое плохое. Есть и хуже, поэтому это приемлемо.");
        noSignal("naming the error is quiet",
                "Relative privation. Fallacy of relative privation. Есть хуже — не аргумент.");
        noSignal("rejecting the move is quiet",
                "Хуже не отменяет. A worse problem does not cancel this one. Not as bad does not mean acceptable.");
        noSignal("ordinary comparison is quiet",
                "Сравниваем два варианта по риску и цене. Это описание, не отмахивание.");

        List<String> both = new ArrayList<String>();
        RelativePrivationPolicy.detect(
                "Есть проблемы похуже.",
                "There are bigger problems.",
                both);
        check(contains(both, "оба эксперта"), "both sides dismissing via worse problem are flagged together");

        List<String> one = new ArrayList<String>();
        RelativePrivationPolicy.detect(
                "Не так плохо, потому что есть хуже.",
                "Сравним базовую частоту похожих мер.",
                one);
        check(contains(one, "один эксперт"), "a single miss is the asymmetric form");
        check(!contains(one, "оба эксперта"), "a single miss is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "По крайней мере не так плохо как катастрофа.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "relative privation"), "detectAll wires the relative privation check");

        List<String> capped = new ArrayList<String>();
        String many = "Не так плохо, потому что есть хуже. At least it is not as bad as. There are worse problems.";
        RelativePrivationPolicy.detect(many, many, capped);
        int first = capped.size();
        RelativePrivationPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 2, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("RelativePrivationPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        RelativePrivationPolicy.detect(text, "", conflicts);
        check(contains(conflicts, "relative privation"), label);
    }

    private static void noSignal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        RelativePrivationPolicy.detect(text, "", conflicts);
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
