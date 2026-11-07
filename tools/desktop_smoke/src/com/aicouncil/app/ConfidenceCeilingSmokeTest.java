package com.aicouncil.app;

import java.util.List;

/** English "possible signal" markers must raise the coordinator confidence ceiling. */
public final class ConfidenceCeilingSmokeTest {
    private static int passed;

    public static void main(String[] args) {
        DeliberationIntelligence.Brief brief = DeliberationIntelligence.analyze(
                "Стоит ли менять текущий инструмент на новый?",
                "Я знаю лучше. Только некомпетентные сомневаются. Any competent person sees this.",
                "Это уже моё. Because I own it. Уже есть, поэтому ценнее. Keep what I have.");
        String map = DeliberationIntelligence.formatForCoordinator(brief);
        check(map.contains("ПОТОЛОК УВЕРЕННОСТИ"), "english hypothesis markers raise the ceiling");
        check(map.contains("possible signal"), "ceiling text names the english marker");
        int hypotheses = 0;
        for (String conflict : brief.conflicts) {
            if (conflict.contains("possible signal") || conflict.contains("возможный сигнал")) hypotheses++;
        }
        check(hypotheses >= 2, "at least two hypothesis signals are visible to the coordinator");
        check(map.contains("Согласие формулировок само по себе не доказательство"),
                "wording agreement is not treated as evidence");
        System.out.println("ConfidenceCeilingSmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
