package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class RhymeAsReasonPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        List<String> hit = new ArrayList<String>();
        RhymeAsReasonPolicy.detect(
                "Слоган говорит сам за себя, звучит убедительно. Поэтому вердикт: закрываем на этом.",
                "",
                hit);
        check(!hit.isEmpty(), "fluency used as a close is a signal");
        check(hit.get(0).contains("possible signal"), "fluency signal stays a hypothesis");

        List<String> named = new ArrayList<String>();
        RhymeAsReasonPolicy.detect(
                "Звучит убедительно, но rhyme as reason: рифма не делает истинным. Данные показывают обратное.",
                "",
                named);
        check(named.isEmpty(), "naming the fluency gap is not itself an error");

        List<String> evidence = new ArrayList<String>();
        RhymeAsReasonPolicy.detect(
                "Слоган говорит сам за себя. Поэтому вердикт. Независимое основание: данные показывают эффект.",
                "",
                evidence);
        check(evidence.isEmpty(), "independent evidence keeps fluency from closing the choice");

        List<String> map = new ArrayList<String>();
        map.add("rhyme as reason (possible signal, requires context): рифма");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("рифма") || cue.toString().contains("не доказательство"),
                "coordinator cannot close on rhyme");

        System.out.println("RhymeAsReasonPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
