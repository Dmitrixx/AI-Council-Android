package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class PropositionalFallacyPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("russian affirming the consequent",
                "Раз следствие есть, значит причина доказана. Симптом есть, значит диагноз доказан.");
        signal("english affirming the consequent",
                "The effect is present, so the cause must be. Because the effect happened, the cause is proven.");
        signal("ukrainian affirming the consequent",
                "Наслідок є, отже причина доведена.");
        signal("denying the antecedent",
                "Достаточное условие не выполнено, значит вывод ложен. Not P, therefore not Q.");
        noSignal("modus tollens stays valid",
                "Нет следствия, значит нет этой причины. Modus tollens: not Q, therefore not P.");
        noSignal("modus ponens stays valid",
                "Modus ponens: если P, то Q; P, значит Q.");
        noSignal("naming the fallacy is not the error",
                "Утверждение консеквента не доказательство: следствие не доказывает причину. Affirming the consequent is invalid.");
        noSignal("a failed condition is named as not a refutation",
                "Отсутствие одного достаточного условия не отменяет следствие. Denying the antecedent is invalid.");
        noSignal("an ordinary conditional stays quiet",
                "Если бюджет превысит план, остановим запуск и пересчитаем диапазон.");

        List<String> both = new ArrayList<String>();
        PropositionalFallacyPolicy.detect(
                "Раз следствие есть, значит причина доказана.",
                "Because the effect happened, the cause is proven.",
                both);
        check(contains(both, "оба эксперта выводят причину"), "both sides affirming are flagged together");

        List<String> one = new ArrayList<String>();
        PropositionalFallacyPolicy.detect(
                "Один из путей не сработал — значит результат невозможен.",
                "Сравним базовую частоту и альтернативные причины.",
                one);
        check(contains(one, "отрицание антецедента"), "one denied antecedent is enough");
        check(contains(one, "один эксперт"), "a single miss is the asymmetric form");
        check(!contains(one, "оба эксперта"), "a single miss is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Консеквент истинен, следовательно антецедент.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "propositional fallacy"), "detectAll wires the propositional check");

        DeliberationIntelligence.Brief brief = DeliberationIntelligence.analyze(
                "Доказан ли диагноз?",
                "Симптом есть, значит диагноз доказан.",
                "Сравним базовую частоту и другие причины того же симптома.");
        String map = DeliberationIntelligence.formatForCoordinator(brief);
        check(map.contains("Не выводи причину из одного следствия"), "coordinator must not treat an effect as a unique cause");

        DeliberationIntelligence.Brief equated = DeliberationIntelligence.analyze(
                "Это одно и то же?",
                "Это то же самое, что отказ от лечения.",
                "Есть релевантное различие по риску.");
        String equivalence = DeliberationIntelligence.formatForCoordinator(equated);
        check(equivalence.contains("ложной эквивалентности"), "coordinator must not close a false equivalence");

        String addendum = DeliberationIntelligence.expertAddendum(1,
                "1. Отдели причину от следствия\n2. Проверь, не путается ли достаточное условие с необходимым");
        check(addendum.contains("ФОРМАЛЬНАЯ ЛОГИКА"), "experts are told to keep modus tollens and reject both fallacies");
        check(DeliberationIntelligence.expertAddendum(0, "Test task").isEmpty(), "short task still has no addendum");

        List<String> capped = new ArrayList<String>();
        String many = "Раз следствие есть, значит причина доказана. Denying the antecedent. Not P, therefore not Q.";
        PropositionalFallacyPolicy.detect(many, many, capped);
        int first = capped.size();
        PropositionalFallacyPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 3, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("PropositionalFallacyPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        PropositionalFallacyPolicy.detect(text, "", conflicts);
        check(contains(conflicts, "propositional fallacy"), label);
    }

    private static void noSignal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        PropositionalFallacyPolicy.detect(text, "", conflicts);
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
