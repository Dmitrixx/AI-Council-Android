package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class FallacyFallacyPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("russian fallacy implies false",
                "Раз в аргументе ошибка значит вывод ложен. Плохой довод значит тезис ложен.");
        signal("english fallacy implies false",
                "Contains a fallacy therefore the conclusion is false. Bad argument so the opposite is true.");
        signal("sophism closes the claim",
                "Это софизм поэтому неправда.");
        noSignal("naming the error is quiet",
                "Fallacy fallacy. Argument from fallacy. Софизм не опровергает тезис.");
        noSignal("rejecting the move is quiet",
                "Ошибка в аргументе не делает вывод ложным. A fallacy does not prove the conclusion false.");
        noSignal("ordinary critique is quiet",
                "В расчёте ошибка в знаке. Тезис всё равно надо проверить отдельно по данным.");

        List<String> both = new ArrayList<String>();
        FallacyFallacyPolicy.detect(
                "Раз аргумент ошибочен значит неверно.",
                "The reasoning is invalid so the claim is untrue.",
                both);
        check(contains(both, "оба эксперта"), "both sides treating a bad argument as a refutation are flagged together");

        List<String> one = new ArrayList<String>();
        FallacyFallacyPolicy.detect(
                "Ошибка в рассуждении доказывает обратное.",
                "Сверим базовую частоту и оставим тезис открытым.",
                one);
        check(contains(one, "один эксперт"), "a single miss is the asymmetric form");
        check(!contains(one, "оба эксперта"), "a single miss is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Since the argument fails the claim is false.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "fallacy fallacy"), "detectAll wires the fallacy-fallacy check");

        String task = "1. Отдели ошибку довода от тезиса\n2. Проверь, не объявлено ли обратное из-за софизма";
        String addendum = DeliberationIntelligence.expertAddendum(1, task);
        check(addendum.contains("ОШИБКА НЕ ВЕРДИКТ"), "experts are told an error is not a verdict");

        List<String> map = new ArrayList<String>();
        map.add("fallacy fallacy (possible signal, requires context): ошибка в доводе");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("не доказывает обратное"), "coordinator is told not to close on a bad argument");

        List<String> capped = new ArrayList<String>();
        String many = "Раз в аргументе ошибка значит вывод ложен. Fallacious therefore false. Contains a fallacy therefore the conclusion is false.";
        FallacyFallacyPolicy.detect(many, many, capped);
        int first = capped.size();
        FallacyFallacyPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 2, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("FallacyFallacyPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        FallacyFallacyPolicy.detect(text, "", conflicts);
        check(contains(conflicts, "fallacy fallacy"), label);
    }

    private static void noSignal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        FallacyFallacyPolicy.detect(text, "", conflicts);
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
