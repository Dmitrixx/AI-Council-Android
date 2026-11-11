package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class IsOughtPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("russian fact to duty",
                "Раз так есть, значит так и должно. Это факт, поэтому это правильно.");
        signal("english is to ought",
                "It is, therefore it should be. What is must be what ought to be.");
        signal("moralistic reverse",
                "Раз так должно быть, значит так и есть. It ought to be, therefore it is.");
        noSignal("naming the gap is quiet",
                "Is-ought: гильотина Юма. Hume's guillotine. Из есть не следует должно.");
        noSignal("rejecting the jump is quiet",
                "Факт не делает норму. Ought does not follow from is. A wish is not a fact.");
        noSignal("ordinary description is quiet",
                "Сейчас доля отказов 4%. Это описание, не рекомендация.");

        List<String> both = new ArrayList<String>();
        IsOughtPolicy.detect(
                "Раз так есть, значит так и должно.",
                "It is, therefore it should be.",
                both);
        check(contains(both, "оба эксперта"), "both sides jumping the gap are flagged together");

        List<String> one = new ArrayList<String>();
        IsOughtPolicy.detect(
                "Так устроено, значит так и надо.",
                "Сравним базовую частоту похожих мер.",
                one);
        check(contains(one, "один эксперт"), "a single miss is the asymmetric form");
        check(!contains(one, "оба эксперта"), "a single miss is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Из того что есть следует что должно.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "is-ought"), "detectAll wires the is-ought check");

        DeliberationIntelligence.Brief brief = DeliberationIntelligence.analyze(
                "Следует ли из факта норма?",
                "Раз так есть, значит так и должно.",
                "Факт не делает норму, смотрим отдельное основание.");
        String map = DeliberationIntelligence.formatForCoordinator(brief);
        check(map.contains("не следует «так должно»"), "coordinator must not close on the is-ought jump");

        String addendum = DeliberationIntelligence.expertAddendum(1,
                "1. Отдели факт от нормы\n2. Проверь, не выведено ли долженствование из описания");
        check(addendum.contains("ФАКТ И НОРМА"), "experts are told is does not yield ought");
        check(addendum.contains("ФОРМАЛЬНАЯ ЛОГИКА"), "formal-logic addendum stays in place");

        List<String> capped = new ArrayList<String>();
        String many = "Раз так есть, значит так и должно. It is, therefore it should be. Ought implies is.";
        IsOughtPolicy.detect(many, many, capped);
        int first = capped.size();
        IsOughtPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 2, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("IsOughtPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        IsOughtPolicy.detect(text, "", conflicts);
        check(contains(conflicts, "is-ought"), label);
    }

    private static void noSignal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        IsOughtPolicy.detect(text, "", conflicts);
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
