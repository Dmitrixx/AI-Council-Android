package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ChestertonFencePolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("abolish a rule nobody understands",
                "Правило никому не понятно, поэтому отменим регламент.",
                "");
        signal("forgotten purpose is treated as a reason to delete",
                "Раз никто не помнит зачем, давайте убрать эту проверку.",
                "");
        signal("english scrap of an old policy",
                "The process looks outdated, abolish the policy.",
                "");
        signal("both experts tear down without a function",
                "Отменим регламент, он только мешает.",
                "Remove the policy, it is just bureaucracy.");
        signal("nobody remembers, so scrap it",
                "Nobody remembers why, so remove the checklist.",
                "");
        noSignal("function is named before removal",
                "Прежде чем отменить регламент, нужно понять исходную функцию правила.",
                "");
        noSignal("fence is named and investigated",
                "Это забор Честертона: сначала выяснить, зачем проверка появилась, и только потом решать.",
                "");
        noSignal("ordinary caution stays quiet",
                "Регламент стоит сверить с текущей целью, а не отменять вслепую.",
                "Оставим проверку, пока не ясна цена ошибки.");
        noSignal("status quo preference is not a fence demolition",
                "Лучше оставить как есть и ничего не делать.",
                "");

        List<String> idempotent = new ArrayList<String>();
        ChestertonFencePolicy.detect(
                "Правило никому не понятно, поэтому отменим регламент.",
                "",
                idempotent);
        int once = idempotent.size();
        ChestertonFencePolicy.detect(
                "Правило никому не понятно, поэтому отменим регламент.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Правило никому не понятно, поэтому отменим регламент.",
                "Бюджет 10 руб.",
                viaMap);
        check(contains(viaMap, "chesterton fence"), "detectAll wires chesterton fence");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Стоит ли отменить старый регламент запуска без разбора?",
                "Правило никому не понятно, поэтому отменим регламент. Бюджет 100 руб.",
                "Бюджет 400 руб и другой срок окупаемости.");
        check(!ranked.conflicts.isEmpty() && ranked.conflicts.get(0).contains("chesterton fence"),
                "chesterton hypothesis outranks a bare number clash");
        String map = DeliberationIntelligence.formatForCoordinator(ranked);
        check(map.contains("возможный сигнал"), "coordinator map keeps the hypothesis visible");
        check(map.contains("исходную функцию") || map.contains("исходной функции") || map.contains("Снос без исходной"),
                "coordinator map tells not to close a demolition");

        String decision = SynthesisProtocol.expertBlock(
                "Стоит ли отменить внутренний регламент согласования перед релизом продукта?");
        check(decision.contains("исходную функцию") || decision.contains("зачем они появились"),
                "decision protocol asks why a rule exists before removal");
        String coord = SynthesisProtocol.coordinatorBlock(
                "Стоит ли отменить внутренний регламент согласования перед релизом продукта?",
                map);
        check(coord.contains("Честертон") || coord.contains("исходн"),
                "coordinator protocol refuses to close a demolition");

        System.out.println("ChestertonFencePolicySmokeTest: " + passed + "/" + passed + " passed");
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
        ChestertonFencePolicy.detect(a, b, out);
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
