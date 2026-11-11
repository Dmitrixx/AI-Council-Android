package com.aicouncil.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CoordinatorCueSmokeTest {
    private static int passed;

    public static void main(String[] args) {
        StringBuilder empty = new StringBuilder();
        CoordinatorCue.append(empty, null);
        CoordinatorCue.append(null, new ArrayList<String>());
        check(empty.length() == 0, "missing input stays silent");
        check(CoordinatorCue.noteCount(new ArrayList<String>()) == 0, "empty map has no notes");

        List<String> novelty = Arrays.asList(
                "appeal to novelty (possible signal, requires context): вывод из новизны");
        StringBuilder one = new StringBuilder();
        CoordinatorCue.append(one, novelty);
        check(one.toString().contains("не доказательство качества"), "novelty cue tells the coordinator not to close on newness");
        check(CoordinatorCue.noteCount(novelty) == 1, "one matching signal is one note");

        List<String> many = new ArrayList<String>();
        many.add("seen and unseen (possible signal, requires context): видимый эффект");
        many.add("streetlight (possible signal, requires context): удобная метрика");
        many.add("appeal to novelty (possible signal, requires context): новое");
        many.add("appeal to pity (possible signal, requires context): жалость");
        many.add("red herring (possible signal, requires context): увод");
        many.add("tu quoque (possible signal, requires context): ты сам");
        StringBuilder capped = new StringBuilder();
        CoordinatorCue.append(capped, many);
        int lines = 0;
        for (String line : capped.toString().split("\n")) {
            if (line.trim().length() > 0) lines++;
        }
        check(lines == 4, "notes stay capped at four");
        check(capped.toString().contains("невидимую цену"), "decision-shaped cue is kept under the cap");
        check(capped.toString().contains("неизмеренным") || capped.toString().contains("удобно посчитать"),
                "streetlight cue survives the cap");
        check(!capped.toString().contains("Увод в сторону"), "lower-priority cue yields to the cap");
        check(CoordinatorCue.noteCount(many) == 4, "noteCount matches the cap");

        List<String> unknown = Arrays.asList("число: разные оценки (10 руб vs 40 руб)");
        StringBuilder quiet = new StringBuilder();
        CoordinatorCue.append(quiet, unknown);
        check(quiet.length() == 0, "a bare number clash is not a reasoning cue");

        System.out.println("CoordinatorCueSmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
