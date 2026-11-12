package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class IdentifiableVictimPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("a story outweighs the count",
                "Одна история важнее цифр, поэтому программу расширяем.");
        signal("a faceless number is dismissed",
                "Statistics have no face so they do not count. It's just a statistic so it doesn't matter.");
        noSignal("naming the error is quiet",
                "Identifiable victim. Яркая история не отменяет базовую частоту.");
        noSignal("an ordinary comparison is quiet",
                "Сравниваем один описанный случай с частотой в классе и не закрываем выбор историей.");

        List<String> both = new ArrayList<String>();
        IdentifiableVictimPolicy.detect(
                "Живой человек значит важнее статистики.",
                "A single story outweighs the statistics.",
                both);
        check(contains(both, "оба эксперта"), "both sides are flagged together");

        List<String> one = new ArrayList<String>();
        IdentifiableVictimPolicy.detect(
                "Раз нет лица значит проблемы нет.",
                "Держим базовую частоту и не закрываем выбор историей.",
                one);
        check(contains(one, "один эксперт"), "a single side is the asymmetric form");
        check(!contains(one, "оба эксперта"), "a single side is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "A named person outweighs the base rate.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "identifiable victim"), "detectAll wires the identifiable-victim check");

        String decision = SynthesisProtocol.coordinatorBlock(
                "Стоит ли расширять программу из-за одной описанной семьи?",
                "identifiable victim");
        check(decision.contains("не перевешивает частоту") || decision.contains("нет лица"),
                "coordinator protocol refuses to close on a face or on a faceless number");

        List<String> map = new ArrayList<String>();
        map.add("identifiable victim (possible signal, requires context): яркая история");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("частот"), "coordinator cue names the frequency");

        List<String> capped = new ArrayList<String>();
        String many = "Одна история важнее цифр. A single story outweighs the statistics.";
        IdentifiableVictimPolicy.detect(many, many, capped);
        int first = capped.size();
        IdentifiableVictimPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 2, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("IdentifiableVictimPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String name, String text) {
        List<String> out = new ArrayList<String>();
        IdentifiableVictimPolicy.detect(text, "", out);
        check(!out.isEmpty(), name);
    }

    private static void noSignal(String name, String text) {
        List<String> out = new ArrayList<String>();
        IdentifiableVictimPolicy.detect(text, "", out);
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
