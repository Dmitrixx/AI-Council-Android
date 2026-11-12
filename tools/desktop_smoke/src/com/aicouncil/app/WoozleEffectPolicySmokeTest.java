package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class WoozleEffectPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("a shared citation is treated as truth",
                "Раз все цитируют одно и то же, значит верно.");
        signal("wide citation is treated as replication",
                "Widely cited means independently confirmed.");
        signal("a pile of papers closes the question",
                "12 статей подтверждают эффект, вопрос закрыт.");
        noSignal("naming the error is quiet",
                "Woozle. Цепочка цитирования не независимая выборка, повторные ссылки на один источник не репликация.");
        noSignal("a traced primary source is quiet",
                "Четыре статьи сходятся к одному первоисточнику, это не репликация.");
        noSignal("an ordinary source count is quiet",
                "В задаче два источника, я их не усредняю и не закрываю вопрос.");

        List<String> both = new ArrayList<String>();
        WoozleEffectPolicy.detect(
                "Литература сходится, поэтому вопрос закрыт.",
                "The literature agrees so the claim is settled.",
                both);
        check(contains(both, "оба эксперта"), "both sides are flagged together");

        List<String> one = new ArrayList<String>();
        WoozleEffectPolicy.detect(
                "Цепочка ссылок достаточна чтобы закрыть вопрос.",
                "Первоисточник не прослежен, поэтому ссылки не закрывают вывод.",
                one);
        check(contains(one, "один эксперт"), "a single side is the asymmetric form");
        check(!contains(one, "оба эксперта"), "a single side is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Cited everywhere so it is established.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "woozle"), "detectAll wires the woozle check");

        String decision = SynthesisProtocol.coordinatorBlock(
                "Стоит ли закрывать вопрос, потому что на работу ссылаются десятки статей?",
                "woozle");
        check(decision.contains("не независимая выборка") || decision.contains("Эффект вузла"),
                "coordinator protocol refuses to close on a citation count");

        String addendum = DeliberationIntelligence.expertAddendum(1,
                "1. Стоит ли считать цепочку цитат независимым подтверждением эффекта?\n2. Какой первоисточник реально измерял явление?");
        check(addendum.contains("ЦЕПОЧКА ЦИТАТ"), "experts are told a citation chain is not independent proof");
        check(addendum.contains("МОТИВ НЕ ОПРОВЕРЖЕНИЕ"), "experts are told a bad motive is not a refutation");
        check(DeliberationIntelligence.expertAddendum(0, "Test task").isEmpty(), "short task still has no addendum");

        List<String> map = new ArrayList<String>();
        map.add("woozle (possible signal, requires context): цепочка цитат");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("независим"), "coordinator cue asks for independent observations");

        List<String> capped = new ArrayList<String>();
        String many = "Раз все цитируют одно и то же, значит верно. Widely cited means independently confirmed.";
        WoozleEffectPolicy.detect(many, many, capped);
        int first = capped.size();
        WoozleEffectPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 2, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("WoozleEffectPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String name, String text) {
        List<String> out = new ArrayList<String>();
        WoozleEffectPolicy.detect(text, "", out);
        check(!out.isEmpty(), name);
    }

    private static void noSignal(String name, String text) {
        List<String> out = new ArrayList<String>();
        WoozleEffectPolicy.detect(text, "", out);
        check(out.isEmpty(), name);
    }

    private static boolean contains(List<String> items, String needle) {
        for (String item : items) {
            if (item.contains(needle)) return true;
        }
        return false;
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        passed++;
    }
}
