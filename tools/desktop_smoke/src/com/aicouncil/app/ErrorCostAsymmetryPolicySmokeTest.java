package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ErrorCostAsymmetryPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        List<String> hit = new ArrayList<String>();
        ErrorCostAsymmetryPolicy.detect(
                "Поэтому выбираем запуск. Однозначно стоит. Закрываем выбор.",
                "",
                hit);
        check(!hit.isEmpty(), "a close without the costlier error is a signal");
        check(hit.get(0).contains("possible signal"), "the signal stays a hypothesis");
        check(hit.get(0).contains("error-cost asymmetry"), "the signal names the asymmetry");

        List<String> named = new ArrayList<String>();
        ErrorCostAsymmetryPolicy.detect(
                "Поэтому выбираем запуск. Ложная тревога дороже упущенного шанса. Закрываем выбор.",
                "",
                named);
        check(named.isEmpty(), "naming which error is costlier is not itself an error");

        List<String> unknown = new ArrayList<String>();
        ErrorCostAsymmetryPolicy.detect(
                "Закрываем выбор, но неизвестно, какая ошибка дороже.",
                "",
                unknown);
        check(unknown.isEmpty(), "an explicit unknown is not a fresh error");

        List<String> both = new ArrayList<String>();
        String close = "Поэтому выбираем запуск. The choice is clear. Закрываем выбор.";
        ErrorCostAsymmetryPolicy.detect(close, close, both);
        check(both.size() == 2, "both experts closing adds the pair signal and stays capped");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll("Стоит ли запускать долгий проект на год с бюджетом?", close, "", wired);
        check(contains(wired, "error-cost asymmetry"), "detectAll wires the asymmetry check");

        List<String> map = new ArrayList<String>();
        map.add("error-cost asymmetry (possible signal, requires context): какая ошибка дороже");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("лишнее действие") || cue.toString().contains("упущенный шанс"),
                "coordinator refuses to close until the costlier mistake is named");

        System.out.println("ErrorCostAsymmetryPolicySmokeTest: " + passed + "/" + passed + " passed");
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
