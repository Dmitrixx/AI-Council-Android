package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class SelfServingBiasPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("success to self skill",
                "Мы выиграли благодаря навыку и компетентности команды.",
                "");
        signal("failure to external luck",
                "Проиграли из-за обстоятельств и не повезло с ситуацией.",
                "");
        signal("other success is luck",
                "Им просто повезло, их успех — случайность.",
                "");
        signal("both experts self-serve",
                "Наш успех — результат компетентности. Им просто повезло.",
                "We won due to talent. They just got lucky.");
        signal("english skill attribution",
                "Succeeded because of skill and competence.",
                "");
        noSignal("balanced attribution stays quiet",
                "Успех и навык, и обстоятельства. Не только skill.",
                "");
        noSignal("named bias stays quiet",
                "Это self-serving bias: нужно учитывать и внутренние, и внешние факторы.",
                "");
        noSignal("ordinary analysis stays quiet",
                "Результат зависит от подготовки и от рынка. Нужны данные по обоим.",
                "Согласен смотреть base rates.");

        List<String> idempotent = new ArrayList<String>();
        SelfServingBiasPolicy.detect(
                "Мы выиграли благодаря навыку.",
                "",
                idempotent);
        int once = idempotent.size();
        SelfServingBiasPolicy.detect(
                "Мы выиграли благодаря навыку.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Мы выиграли благодаря навыку и компетентности.",
                "Бюджет 10 руб.",
                viaMap);
        check(contains(viaMap, "self-serving bias"), "detectAll wires self-serving bias");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Почему один проект выстрелил, а другой нет?",
                "Мы выиграли благодаря навыку. Им просто повезло.",
                "Бюджет 400 руб и другой срок.");
        check(!ranked.conflicts.isEmpty(), "self-serving hypothesis appears in map");
        String map = DeliberationIntelligence.formatForCoordinator(ranked);
        check(map.contains("возможный сигнал") || map.contains("self-serving"),
                "coordinator map keeps the hypothesis visible");

        System.out.println("SelfServingBiasPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        SelfServingBiasPolicy.detect(a, b, out);
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
