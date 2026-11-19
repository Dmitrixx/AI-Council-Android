package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class AbsoluteRiskNeglectPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("relative reduction closes the choice",
                "Препарат снижает риск на 50%, поэтому выбираем его и закрываем вопрос.");
        noSignal("absolute baseline is named",
                "Снижает риск на 50%. Абсолютный риск с 2 из 100 до 1 из 100. NNT велик, поэтому не закрываем выбор одним процентом.");
        noSignal("gap is named",
                "Absolute risk neglect: относительный процент без базовой частоты не закрывает выбор.");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "This reduces risk by half, therefore we should recommend it and close.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "absolute risk neglect"), "detectAll wires absolute risk neglect");

        List<String> map = new ArrayList<String>();
        map.add("absolute risk neglect (possible signal, requires context): относительный процент");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("NNT") || cue.toString().contains("абсолютн"),
                "coordinator cue refuses to close on a relative figure");

        System.out.println("AbsoluteRiskNeglectPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String name, String text) {
        List<String> out = new ArrayList<String>();
        AbsoluteRiskNeglectPolicy.detect(text, "", out);
        check(!out.isEmpty(), name);
    }

    private static void noSignal(String name, String text) {
        List<String> out = new ArrayList<String>();
        AbsoluteRiskNeglectPolicy.detect(text, "", out);
        check(out.isEmpty(), name);
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
