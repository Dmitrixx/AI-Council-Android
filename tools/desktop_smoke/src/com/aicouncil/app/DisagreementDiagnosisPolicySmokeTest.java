package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class DisagreementDiagnosisPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("same target, incompatible mechanisms",
                "Рекомендую Postgres, потому что транзакции переживут отказ узла.",
                "Рекомендую Postgres, потому что команда не хочет учить другой синтаксис.");
        signal("english fragile agreement",
                "I recommend Postgres because failover survives a node crash.",
                "I recommend Postgres because hiring is cheaper than a new stack.");
        noSignal("shared mechanism is real agreement",
                "Рекомендую Postgres, потому что транзакции переживают отказ узла.",
                "Рекомендую Postgres, потому что те же транзакции проще восстанавливать.");
        noSignal("a named flip keeps consensus intact",
                "Рекомендую Postgres, потому что транзакции переживают отказ. Если окажется, что запись однопоточная, вывод изменится.",
                "Рекомендую Postgres, потому что команда не хочет учить другой синтаксис.");
        noSignal("different targets are a choice clash, not false agreement",
                "Рекомендую Postgres, потому что транзакции переживают отказ узла.",
                "Рекомендую SQLite, потому что один файл проще сопровождать.");
        noSignal("naming the diagnosis is not itself an error",
                "Рекомендую Postgres, потому что транзакции. Это хрупкое согласие, тип разногласия уже назван.",
                "Рекомендую Postgres, потому что команда не хочет учить другой синтаксис.");
        signal("fact against norm is not one number",
                "Данные показывают отток 12% и базовая частота против запуска, поэтому не запускаем.",
                "Это справедливо, поэтому должен запустить, даже если отток выше. Стоит делать.");
        noSignal("fact and norm on the same side stay quiet",
                "Данные показывают отток 12%, поэтому не запускаем. Это и есть ожидаемая ценность.",
                "Базовая частота тоже против. Не стоит запускать.");
        noSignal("matched budgets are not a disagreement",
                "Бюджет проекта составляет 100 руб.",
                "Бюджет проекта составляет 100 рублей.");
        noSignal("empty side stays quiet",
                "Рекомендую Postgres, потому что транзакции переживают отказ узла.",
                "");

        List<String> idempotent = new ArrayList<String>();
        DisagreementDiagnosisPolicy.detect(
                "Рекомендую Postgres, потому что транзакции переживут отказ узла.",
                "Рекомендую Postgres, потому что команда не хочет учить другой синтаксис.",
                idempotent);
        int once = idempotent.size();
        DisagreementDiagnosisPolicy.detect(
                "Рекомендую Postgres, потому что транзакции переживут отказ узла.",
                "Рекомендую Postgres, потому что команда не хочет учить другой синтаксис.",
                idempotent);
        check(once == 1, "one diagnosis, not a stack of kinds");
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(idempotent.get(0).contains("possible signal"), "diagnosis stays a hypothesis");

        List<String> via = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Какую базу выбрать?",
                "Рекомендую Postgres, потому что транзакции переживут отказ узла.",
                "Рекомендую Postgres, потому что команда не хочет учить другой синтаксис.",
                via);
        check(contains(via, "disagreement diagnosis"), "detectAll wires the diagnosis");
        check(contains(via, "хрупкое согласие"), "fragile agreement is the kind that was found");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Какую базу выбрать для очереди?",
                "Рекомендую Postgres, потому что транзакции переживут отказ узла. Бюджет 100 руб.",
                "Рекомендую Postgres, потому что команда не хочет учить другой синтаксис. Бюджет 400 руб.");
        check(contains(ranked.conflicts, "disagreement diagnosis"), "diagnosis survives the deliberation map");
        String map = DeliberationIntelligence.formatForCoordinator(ranked);
        check(map.contains("не усредняй причины") || map.contains("несущий механизм"),
                "coordinator is told not to average incompatible reasons");

        System.out.println("DisagreementDiagnosisPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        DisagreementDiagnosisPolicy.detect(a, b, out);
        return out;
    }

    private static boolean contains(List<String> items, String needle) {
        if (items == null) return false;
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
