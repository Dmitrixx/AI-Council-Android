package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class SteelmanPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("rejects a cartoon of the other side",
                "Их позиция — абсурдная версия, которую никто в здравом уме не держит, поэтому отвергаем её как неверна.");
        signal("english strawman closes the case",
                "That is a straw man, a weak version of their view, so we reject it. The position is dismissed.");
        noSignal("names the strongest fair version",
                "Сильнейшая честная версия их позиции серьёзна. Карикатура не опровергает её.");
        noSignal("ordinary disagreement",
                "Мы расходимся в механизме. Нужно наблюдение, которое переключит вывод.");
        noSignal("labels the steelman gap",
                "Steelman missing: критика карикатуры без сильнейшей честной формулировки не закрывает выбор.");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Their claim is an absurd version only a fool would hold, therefore we reject it as неверна.",
                "Оставляю вопрос открытым.",
                wired);
        check(contains(wired, "steelman"), "detectAll wires the steelman check");

        String decision = SynthesisProtocol.coordinatorBlock(
                "Стоит ли отвергать их план целиком, если он кажется вам абсурдным?",
                "steelman");
        check(decision.contains("карикатур") || decision.contains("честн"),
                "coordinator protocol refuses to close on a caricature");

        List<String> map = new ArrayList<String>();
        map.add("steelman (possible signal, requires context): слабая карикатура");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("честн"), "coordinator cue asks for the strongest fair version");

        List<String> capped = new ArrayList<String>();
        String many = "Абсурдная версия, поэтому отвергаем, позиция неверна. Straw man, so we reject it.";
        SteelmanPolicy.detect(many, many, capped);
        int first = capped.size();
        SteelmanPolicy.detect(many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first > 0 && first <= 2, "number of signals is bounded");
        check(capped.get(0).contains("possible signal"), "signals stay hypotheses");

        System.out.println("SteelmanPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String name, String text) {
        List<String> out = new ArrayList<String>();
        SteelmanPolicy.detect(text, "", out);
        check(!out.isEmpty(), name);
    }

    private static void noSignal(String name, String text) {
        List<String> out = new ArrayList<String>();
        SteelmanPolicy.detect(text, "", out);
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
