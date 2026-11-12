package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class MoralLicensingPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("past good act licenses the step",
                "Мы уже сделали доброе дело поэтому можно принять этот риск.");
        signal("identity as free pass",
                "Because we are good people the choice is acceptable. We already did good so this is fine.");
        noSignal("naming the error is quiet",
                "Moral licensing. Прошлое доброе дело не оправдывает текущий вред.");
        noSignal("ordinary restraint is quiet",
                "Мы сдержали себя раньше и снова сравниваем будущий вред и пользу без ссылки на заслуги.");

        List<String> both = new ArrayList<String>();
        MoralLicensingPolicy.detect(
                "Я пожертвовал значит теперь можно.",
                "Past restraint licenses the present choice.",
                both);
        check(contains(both, "оба эксперта"), "both sides are flagged together");

        List<String> one = new ArrayList<String>();
        MoralLicensingPolicy.detect(
                "Прошлые заслуги оправдывают этот выбор.",
                "Оцениваем шаг по будущим издержкам, без лицензии от прошлого.",
                one);
        check(contains(one, "один эксперт"), "a single side is the asymmetric form");
        check(!contains(one, "оба эксперта"), "a single side is not reported as both");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Our prior good acts justify this step.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "moral licensing"), "detectAll wires the moral-licensing check");

        List<String> map = new ArrayList<String>();
        map.add("moral licensing (possible signal, requires context): прошлое доброе дело");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("лиценз") || cue.toString().contains("заслуг"),
                "coordinator cue names the licensing trap");

        List<String> capped = new ArrayList<String>();
        String many = "Мы уже сделали доброе дело поэтому можно. We already did good so this is fine.";
        MoralLicensingPolicy.detect(many, many, capped);
        int first = capped.size();
        MoralLicensingPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first <= 2, "number of signals is bounded");
        check(capped.stream().allMatch(x -> x.contains("possible signal") && x.contains("requires context")),
                "signals stay hypotheses");

        System.out.println("MoralLicensingPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String name, String text) {
        List<String> out = new ArrayList<String>();
        MoralLicensingPolicy.detect(text, "", out);
        check(!out.isEmpty(), name);
    }

    private static void noSignal(String name, String text) {
        List<String> out = new ArrayList<String>();
        MoralLicensingPolicy.detect(text, "", out);
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
