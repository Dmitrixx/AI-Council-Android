package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class SpecialPleadingPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("russian exemption",
                "Для нас правила не действуют. Себе можно, другим нельзя.");
        signal("english exemption",
                "The rules don't apply to us. We are exempt from the rule.");
        signal("asymmetric standard",
                "Стандарт строже к ним, чем к нам: двойной стандарт в нашу пользу.");
        noSignal("naming the fallacy is not the error",
                "Не делай исключение без различия: special pleading не аргумент, нужно релевантное различие.");
        noSignal("a fair comparison stays quiet",
                "Сравним стоимость, риск и то, кого правило реально затрагивает.");
        noSignal("demanding a difference is not pleading",
                "Without a relevant difference an exemption is not a reason.");

        List<String> both = new ArrayList<String>();
        SpecialPleadingPolicy.detect(
                "Для нас правила не действуют.",
                "The rules don't apply to us.",
                both);
        check(contains(both, "оба эксперта"), "both sides pleading are flagged together");

        List<String> one = new ArrayList<String>();
        SpecialPleadingPolicy.detect(
                "Себе можно, другим нельзя.",
                "Сравним издержки и выгоду без исключений.",
                one);
        check(contains(one, "исключение из правила"), "one pleading side is enough");
        check(contains(one, "один эксперт"), "a single miss is also the asymmetric form");
        check(!contains(one, "оба эксперта"), "a single miss is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Мы исключение из этого правила.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "special pleading"), "detectAll wires the special-pleading check");

        DeliberationIntelligence.Brief brief = DeliberationIntelligence.analyze(
                "Применять ли это правило к себе?",
                "Для нас правила не действуют. Себе можно, другим нельзя.",
                "Сравним стоимость и риск.");
        String map = DeliberationIntelligence.formatForCoordinator(brief);
        check(map.contains("релевантного различия"), "coordinator must not close a double standard");

        List<String> capped = new ArrayList<String>();
        String many = "Для нас правила не действуют. Себе можно, другим нельзя. The rules don't apply to us.";
        SpecialPleadingPolicy.detect(many, many, capped);
        int first = capped.size();
        SpecialPleadingPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 2, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("SpecialPleadingPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        SpecialPleadingPolicy.detect(text, "", conflicts);
        check(contains(conflicts, "special pleading"), label);
    }

    private static void noSignal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        SpecialPleadingPolicy.detect(text, "", conflicts);
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
