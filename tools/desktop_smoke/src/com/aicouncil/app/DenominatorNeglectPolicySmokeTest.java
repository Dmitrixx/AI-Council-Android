package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class DenominatorNeglectPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("a relative cut closes the choice",
                "Препарат снижает риск на 50%, поэтому выбираем его.");
        signal("halving the risk is treated as enough",
                "It halves the risk, therefore we should take it.");
        noSignal("naming the absolute baseline is quiet",
                "Снижает риск на 50%, абсолютный риск с 2 из 1000 до 1 из 1000. Базовая частота мала, выбор не закрываем одним процентом.");
        noSignal("naming the error is quiet",
                "Denominator neglect. Относительное снижение без абсолютного риска не закрывает выбор.");
        noSignal("an open comparison is quiet",
                "Относительная цифра без знаменателя не основание. Базовая частота неизвестна, выбор открыт.");

        List<String> both = new ArrayList<String>();
        DenominatorNeglectPolicy.detect(
                "Вдвое снижает риск, поэтому выбираем.",
                "It halves the risk, therefore we should take it.",
                both);
        check(contains(both, "оба эксперта"), "both sides are flagged together");

        List<String> one = new ArrayList<String>();
        DenominatorNeglectPolicy.detect(
                "На 90% эффективен, значит надо назначать.",
                "Абсолютный риск и базовая частота не названы, поэтому не закрываем выбор процентом.",
                one);
        check(!one.isEmpty(), "a single side still flags");
        check(!contains(one, "оба эксперта"), "a single side is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Relative risk reduction of this size, so we should recommend it. It reduces risk by half.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "denominator neglect"), "detectAll wires the denominator check");

        String decision = SynthesisProtocol.coordinatorBlock(
                "Стоит ли назначать препарат всем, если он снижает риск на пятьдесят процентов?",
                "denominator neglect");
        check(decision.contains("знаменател") || decision.contains("абсолютн"),
                "coordinator protocol refuses to close on a relative percentage");

        List<String> map = new ArrayList<String>();
        map.add("denominator neglect (possible signal, requires context): относительный процент");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("абсолютн"), "coordinator cue names the absolute risk");

        List<String> capped = new ArrayList<String>();
        String many = "Снижает риск на 50%, поэтому выбираем. It halves the risk, therefore we should take it.";
        DenominatorNeglectPolicy.detect(many, many, capped);
        int first = capped.size();
        DenominatorNeglectPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 2, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("DenominatorNeglectPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String name, String text) {
        List<String> out = new ArrayList<String>();
        DenominatorNeglectPolicy.detect(text, "", out);
        check(!out.isEmpty(), name);
    }

    private static void noSignal(String name, String text) {
        List<String> out = new ArrayList<String>();
        IdentifiableVictimPolicySmokeTestQuiet(name, text);
    }

    private static void IdentifiableVictimPolicySmokeTestQuiet(String name, String text) {
        List<String> out = new ArrayList<String>();
        DenominatorNeglectPolicy.detect(text, "", out);
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
