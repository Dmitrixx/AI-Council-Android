package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class MiddleGroundFallacyPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("english midpoint is declared true",
                "The two estimates disagree, but the truth is in the middle.");
        signal("russian golden mean is the answer",
                "Они спорят о сроке, но золотая середина и есть ответ.");
        signal("average of the positions is the answer",
                "The answer is the average of the two positions, so we can close this.");
        noSignal("naming the fallacy is not committing it",
                "Calling the midpoint true would be the middle ground fallacy.");
        noSignal("refusing to average is not the error",
                "Не усредняй конфликтующие числа: середина не доказательство.");
        noSignal("a real compromise without a truth claim stays quiet",
                "Можно временно взять рабочий диапазон 3–5 дней и проверить его фактом.");

        List<String> both = new ArrayList<String>();
        MiddleGroundFallacyPolicy.detect(
                "Истина посередине, спор закрыт.",
                "The compromise is the truth.",
                both);
        check(count(both, "middle ground") == 2, "both experts declaring the midpoint are both flagged");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Правда где-то посередине, поэтому берём среднее.",
                "Я оставляю конфликт открытым.",
                wired);
        check(contains(wired, "middle ground"), "detectAll wires the middle-ground check");

        DeliberationIntelligence.Brief brief = DeliberationIntelligence.analyze(
                "Какой срок реалистичен, если оценки расходятся?",
                "Истина лежит посередине, берём среднее двух сроков.",
                "Сроки несовместимы, пока нет нового наблюдения.");
        String map = DeliberationIntelligence.formatForCoordinator(brief);
        check(map.contains("Середину двух позиций"), "coordinator is told not to crown the midpoint");

        System.out.println("MiddleGroundFallacyPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        MiddleGroundFallacyPolicy.detect(text, "", conflicts);
        check(contains(conflicts, "middle ground"), label);
    }

    private static void noSignal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        MiddleGroundFallacyPolicy.detect(text, "", conflicts);
        check(!contains(conflicts, "middle ground"), label);
    }

    private static boolean contains(List<String> items, String needle) {
        for (String item : items) {
            if (item.contains(needle)) return true;
        }
        return false;
    }

    private static int count(List<String> items, String needle) {
        int n = 0;
        for (String item : items) {
            if (item.contains(needle)) n++;
        }
        return n;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
