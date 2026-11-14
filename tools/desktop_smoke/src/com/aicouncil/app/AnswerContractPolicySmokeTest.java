package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class AnswerContractPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        String task = "Стоит ли переносить сервер в другой регион из-за задержки?";
        signal(task,
                "Поэтому рекомендую купить новый телефон: он красивее, значит вывод очевиден.",
                "Задержка сервера пока не измерена.");
        noSignal(task,
                "Не стоит переносить сервер: задержка не измерена, поэтому вывод предварительный.",
                "Сервер в другом регионе не поможет, пока задержка неизвестна. Не рекомендую перенос.");
        noSignal(task,
                "Это не на заданный вопрос: я говорю про телефон, поэтому не закрываю выбор сервера.",
                "Регион сервера не обсуждаю.");
        noSignal("короткий запрос",
                "Поэтому рекомендую купить телефон, вывод очевиден и ясен.",
                "Значит следует купить часы, поэтому вывод готов.");

        List<String> idempotent = new ArrayList<String>();
        String off = "Поэтому рекомендую купить новый телефон: он красивее, значит вывод очевиден.";
        AnswerContractPolicy.detect(task, off, "заметка", idempotent);
        int once = idempotent.size();
        AnswerContractPolicy.detect(task, off, "заметка", idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(task, off, "ещё одна заметка без закрытия", viaMap);
        check(contains(viaMap, "answer contract"), "detectAll wires the answer contract when the task is known");

        List<String> withoutTask = new ArrayList<String>();
        DeliberationSignals.detectAll(off, "ещё одна заметка без закрытия", withoutTask);
        check(!contains(withoutTask, "answer contract"), "answers alone do not invent a question");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(task, off,
                "Не стоит переносить сервер, пока задержка не измерена.");
        check(contains(ranked.conflicts, "answer contract"), "off-question close appears on the map");
        String map = DeliberationIntelligence.formatForCoordinator(ranked);
        check(map.contains("прямой ответ") || map.contains("заданный вопрос") || map.contains("answer contract"),
                "coordinator is told to answer the asked question");

        String addendum = DeliberationIntelligence.expertAddendum(1, task + "\n1. Как измерить задержку до переноса?");
        check(addendum.contains("заданный вопрос"), "experts are told to answer the asked question first");

        System.out.println("AnswerContractPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String task, String a, String b) {
        List<String> out = run(task, a, b);
        check(!out.isEmpty(), "off-question close should signal");
        if (!out.isEmpty()) {
            check(out.get(0).contains("possible signal"), "signal is marked as a hypothesis");
        }
    }

    private static void noSignal(String label, String a, String b) {
        String task = label.equals("короткий запрос")
                ? "Коротко"
                : "Стоит ли переносить сервер в другой регион из-за задержки?";
        check(run(task, a, b).isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String task, String a, String b) {
        List<String> out = new ArrayList<String>();
        AnswerContractPolicy.detect(task, a, b, out);
        return out;
    }

    private static boolean contains(List<String> items, String needle) {
        for (String item : items) {
            if (item != null && item.contains(needle)) return true;
        }
        return false;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
