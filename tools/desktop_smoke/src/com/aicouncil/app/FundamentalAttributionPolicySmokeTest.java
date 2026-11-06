package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class FundamentalAttributionPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("character stands in for the act",
                "Он такой человек, значит так и поступит.",
                "");
        signal("nature replaces the situation",
                "Это в его природе, поэтому виноват именно он.",
                "");
        signal("english type of person",
                "She's the type of person who always fails, so she failed.",
                "");
        signal("both experts close on character",
                "По натуре он ленивый, поэтому проект провален.",
                "He's the type of person who misses deadlines, so the slip is his fault.");
        signal("good person therefore outcome",
                "Хороший человек, поэтому решение верное.",
                "");
        noSignal("situation is named",
                "Он такой человек, но ситуация и стимулы объясняют поступок лучше характера.",
                "");
        noSignal("named bias stays quiet",
                "Это fundamental attribution: характер без ситуации не доказательство.",
                "");
        noSignal("ordinary caution stays quiet",
                "Нужно смотреть обстоятельства и стимулы, прежде чем судить поступок.",
                "Сначала роль и ограничения, потом характер.");

        List<String> idempotent = new ArrayList<String>();
        FundamentalAttributionPolicy.detect(
                "Он такой человек, значит так и поступит.",
                "",
                idempotent);
        int once = idempotent.size();
        FundamentalAttributionPolicy.detect(
                "Он такой человек, значит так и поступит.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Он такой человек, значит так и поступит.",
                "Бюджет 10 руб.",
                viaMap);
        check(contains(viaMap, "fundamental attribution"), "detectAll wires fundamental attribution");

        DeliberationIntelligence.Brief stacked = DeliberationIntelligence.analyze(
                "Стоит ли объяснять срыв только характером?",
                "Он такой человек, значит так и поступит. Хочу, чтобы это было правдой, значит так и есть.",
                "По натуре она ленивая, поэтому виновата. Приятный вывод, значит он верный.");
        String ceiling = DeliberationIntelligence.formatForCoordinator(stacked);
        check(ceiling.contains("ПОТОЛОК УВЕРЕННОСТИ"),
                "several distortion hypotheses cap coordinator confidence");
        check(ceiling.contains("fundamental attribution") || contains(stacked.conflicts, "fundamental attribution"),
                "attribution hypothesis stays on the map");

        System.out.println("FundamentalAttributionPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
        if (!out.isEmpty()) {
            check(out.get(0).contains("возможный сигнал"), label + " is marked as a hypothesis");
        }
    }

    private static void noSignal(String label, String a, String b) {
        check(run(a, b).isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        FundamentalAttributionPolicy.detect(a, b, out);
        return out;
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
