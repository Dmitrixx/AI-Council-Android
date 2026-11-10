package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class EquivocationPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("russian name proves",
                "Само название доказывает, что практика вредна. Уже в самом названии ответ.");
        signal("english etymology",
                "The original meaning proves the claim. Etymology shows it is true.");
        signal("label as proof",
                "The label proves the claim. As the word literally means, it must be false.");
        signal("ukrainian name",
                "Сама назва доводить висновок.");
        noSignal("naming the error is quiet",
                "Equivocation: название не доказательство. Etymological fallacy. The name is not an argument.");
        noSignal("rejecting etymology is quiet",
                "Этимология не доказательство. Не выводи истину из названия. Original meaning is not evidence.");
        noSignal("ordinary naming is quiet",
                "Назовём критерий явно и проверим его на трёх прошлых кейсах.");

        List<String> both = new ArrayList<String>();
        EquivocationPolicy.detect(
                "Само название доказывает вред.",
                "The name itself proves the harm.",
                both);
        check(contains(both, "оба эксперта"), "both sides drawing from a name are flagged together");

        List<String> one = new ArrayList<String>();
        EquivocationPolicy.detect(
                "Исконное значение доказывает, что мера работает.",
                "Сравним базовую частоту похожих мер.",
                one);
        check(contains(one, "один эксперт"), "a single miss is the asymmetric form");
        check(!contains(one, "оба эксперта"), "a single miss is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Раз так называется, значит так и есть.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "equivocation"), "detectAll wires the equivocation check");

        DeliberationIntelligence.Brief brief = DeliberationIntelligence.analyze(
                "Верно ли это по названию?",
                "Само название доказывает верность меры.",
                "Название не доказательство, смотрим исход.");
        String map = DeliberationIntelligence.formatForCoordinator(brief);
        check(map.contains("Не закрывай вердикт на названии"), "coordinator must not close on a name");

        String addendum = DeliberationIntelligence.expertAddendum(1,
                "1. Отдели имя от свойства\n2. Проверь, не подменено ли значение слова");
        check(addendum.contains("ИМЯ НЕ ДОВОД"), "experts are told a name is not an argument");
        check(addendum.contains("ФОРМАЛЬНАЯ ЛОГИКА"), "formal-logic addendum stays in place");

        List<String> capped = new ArrayList<String>();
        String many = "Само название доказывает. The original meaning proves it. The label proves the claim.";
        EquivocationPolicy.detect(many, many, capped);
        int first = capped.size();
        EquivocationPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 2, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("EquivocationPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        EquivocationPolicy.detect(text, "", conflicts);
        check(contains(conflicts, "equivocation"), label);
    }

    private static void noSignal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        EquivocationPolicy.detect(text, "", conflicts);
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
