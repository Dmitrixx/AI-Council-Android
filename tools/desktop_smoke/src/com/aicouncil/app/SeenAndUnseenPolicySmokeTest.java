package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class SeenAndUnseenPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("only the visible gain counts",
                "Видно только выгоду, остальное не считается, поэтому запускаем.",
                "");
        signal("decision ignores what cannot be seen",
                "Считаем только то, что видно и закрываем выбор.",
                "");
        signal("broken window as a benefit",
                "Разбитое окно создаёт работу, значит это полезно для города.",
                "");
        signal("english seen effect",
                "Only the seen effect matters, so the subsidy is a gain.",
                "");
        signal("unseen costs dismissed",
                "Unseen costs do not count, ignore what is not seen.",
                "");
        signal("both experts stay on the visible ledger",
                "Считаем только то, что видно.",
                "Only the seen effect matters for this budget.");
        noSignal("unseen cost is kept on the ledger",
                "Видно выгоду, но невидимая цена тоже считается, поэтому рано закрывать.",
                "");
        noSignal("bastiat is named as a check",
                "Это Бастиа: то, чего не видно, тоже цена, и её надо назвать.",
                "");
        noSignal("broken window is not wealth",
                "Разбитое окно не создаёт богатства: работа стекольщика видна, а пропавшая покупка нет.",
                "");
        noSignal("ordinary visible number is not the fallacy",
                "Выручка за квартал видна в отчёте и равна 12.",
                "");

        List<String> idempotent = new ArrayList<String>();
        SeenAndUnseenPolicy.detect("Считаем только то, что видно.", "", idempotent);
        int once = idempotent.size();
        SeenAndUnseenPolicy.detect("Считаем только то, что видно.", "", idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll("Считаем только то, что видно. Бюджет 100 руб.", "Бюджет 10 руб.", viaMap);
        check(contains(viaMap, "seen and unseen"), "detectAll wires seen and unseen");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Стоит ли субсидировать ремонт, если видна только работа исполнителей?",
                "Считаем только то, что видно. Бюджет 100 руб.",
                "Бюджет 400 руб и другой срок окупаемости.");
        check(!ranked.conflicts.isEmpty() && ranked.conflicts.get(0).contains("seen and unseen"),
                "seen-and-unseen hypothesis outranks a bare number clash");
        String map = DeliberationIntelligence.formatForCoordinator(ranked);
        check(map.contains("невидимую цену") || map.contains("невидимая цена"),
                "coordinator map refuses to close on the seen effect alone");

        String decision = SynthesisProtocol.expertBlock(
                "Стоит ли запускать субсидию на ремонт в этом квартале, если видна только занятость?");
        check(decision.contains("не видно") || decision.contains("невидимую"),
                "decision protocol asks for the unseen cost");
        String coord = SynthesisProtocol.coordinatorBlock(
                "Стоит ли запускать субсидию на ремонт в этом квартале, если видна только занятость?",
                map);
        check(coord.contains("Бастиа") || coord.contains("невидимая цена") || coord.contains("увиденным"),
                "coordinator protocol refuses to close on the visible ledger");

        System.out.println("SeenAndUnseenPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
        if (!out.isEmpty()) {
            check(out.get(0).contains("possible signal"), label + " is marked as a hypothesis");
        }
    }

    private static void noSignal(String label, String a, String b) {
        check(run(a, b).isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        SeenAndUnseenPolicy.detect(a, b, out);
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
