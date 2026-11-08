package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class StrawManPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("caricature instead of the claim",
                "Они якобы считают, что надо всё закрыть. Никто в здравом уме так не предлагает, значит идея мертва.",
                "");
        signal("both refute a cartoon",
                "Так вы просто хотите сдаться. Их позиция сводится к абсурду.",
                "На самом деле они хотят разрушить всё. So you just want to quit.");
        noSignal("steelman comes first",
                "Сильнейшая версия их аргумента: пилот дешевле полного отката. Мы её не искажая разбираем, а не соломенное чучело.");
        noSignal("ordinary disagreement",
                "Я не согласен с бюджетом 12 тысяч: эффект не показан на выборке.");
        noSignal("named straw man is not a fresh hit",
                "Это straw man: сильнейшая версия позиции другая, и мы отвечаем на неё.");

        List<String> idempotent = new ArrayList<String>();
        StrawManPolicy.detect("Они якобы хотят сдаться. Так вы просто хотите закрыть всё.", "", idempotent);
        int once = idempotent.size();
        StrawManPolicy.detect("Они якобы хотят сдаться. Так вы просто хотите закрыть всё.", "", idempotent);
        check(once > 0 && once <= 2, "signal count stays bounded");
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(idempotent.get(0).contains("possible signal"), "marked as a hypothesis");

        System.out.println("StrawManPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
    }

    private static void noSignal(String label, String a) {
        check(run(a, "").isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        StrawManPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
