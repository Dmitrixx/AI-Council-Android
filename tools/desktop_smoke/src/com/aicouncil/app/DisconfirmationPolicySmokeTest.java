package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class DisconfirmationPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("sealed russian verdict",
                "Однозначно рекомендую запуск: ничто не изменит вывод.");
        signal("sealed english verdict",
                "I recommend we launch. Nothing would change my mind.");
        signal("confident recommendation without an observation",
                "Однозначно рекомендую выбрать этот план, без сомнений.");
        signal("english certain recommendation",
                "I recommend we should ship it. Definitely the right call.");
        noSignal("recommendation that names the breaking observation",
                "Однозначно рекомендую запуск. Наблюдение, которое изменило бы вывод: удержание ниже 20%.");
        noSignal("naming the criterion is not the error",
                "Не закрывай без опровержения: критерий опровержения нужно назвать явно, what would change my mind.");
        noSignal("hedged recommendation stays quiet",
                "Возможно, рекомендую подождать, но это не точно.");
        noSignal("a budget figure is not a sealed verdict",
                "Бюджет проекта составляет 100 руб. при текущих допущениях.");
        noSignal("uncertainty word alone is not a decision",
                "Однозначно цифры ещё сырые, вывод рано закрывать.");

        List<String> both = new ArrayList<String>();
        DisconfirmationPolicy.detect(
                "Однозначно рекомендую запуск, без вариантов.",
                "I recommend we should proceed. Definitely.",
                both);
        check(contains(both, "оба эксперта закрывают решение"), "both experts without a falsifier are flagged together");
        check(!contains(both, "уверенное решение без наблюдения"), "paired miss does not also file the single-sided form");

        List<String> one = new ArrayList<String>();
        DisconfirmationPolicy.detect(
                "Однозначно рекомендую запуск, без сомнений.",
                "Рекомендую подождать. Наблюдение, которое изменило бы вывод: рост отказов.",
                one);
        check(contains(one, "уверенное решение без наблюдения"), "one sealed side is enough");
        check(!contains(one, "оба эксперта"), "a single miss is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Однозначно рекомендую внедрить. Ничто не изменит вывод.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "disconfirmation"), "detectAll wires the disconfirmation check");

        DeliberationIntelligence.Brief brief = DeliberationIntelligence.analyze(
                "Стоит ли запускать продукт?",
                "Однозначно рекомендую запуск, без сомнений.",
                "Точно считаю бюджет 100 руб., но не рекомендую закрывать вопрос.");
        String map = DeliberationIntelligence.formatForCoordinator(brief);
        check(map.contains("Не закрывай вердикт без наблюдения"), "coordinator must not close an unfalsified verdict");

        String addendum = DeliberationIntelligence.expertAddendum(0, "Стоит ли запускать этот продукт в этом квартале?");
        check(addendum.contains("КРИТЕРИЙ ОПРОВЕРЖЕНИЯ"), "experts are asked for one breaking observation");
        check(DeliberationIntelligence.expertAddendum(0, "Test task").isEmpty(), "short unrelated task still has no addendum");

        List<String> capped = new ArrayList<String>();
        String many = "Однозначно рекомендую запуск. Ничто не изменит вывод. I recommend we should ship. Definitely.";
        DisconfirmationPolicy.detect(many, many, capped);
        int first = capped.size();
        DisconfirmationPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 2, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("DisconfirmationPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        DisconfirmationPolicy.detect(text, "", conflicts);
        check(contains(conflicts, "disconfirmation"), label);
    }

    private static void noSignal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        DisconfirmationPolicy.detect(text, "", conflicts);
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
