package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class HiddenProfilePolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("agreement treated as a second sample",
                "Все эксперты сходятся, поэтому рекомендую запуск: мнения совпали, этого достаточно.",
                "Замер удержания показывает отток 12% в месяц, и этого числа нет в общем пересказе.");
        signal("english hidden profile",
                "All experts agree, so I recommend shipping. The decision is to launch.",
                "Only one expert measured churn at 18% and the others never cited that figure.");
        noSignal("quoting the unique number answers it",
                "Все эксперты сходятся и рекомендую запуск, но отток 12% уже стоит в решении.",
                "Замер удержания показывает отток 12% в месяц.");
        noSignal("two recommendations without consensus-as-proof stay quiet",
                "Рекомендую Postgres, потому что транзакции переживают отказ узла.",
                "Рекомендую SQLite, потому что один файл проще сопровождать.");
        noSignal("naming the bias is not itself an error",
                "Все эксперты сходятся, рекомендую запуск. Это скрытый профиль: согласие не доказательство, уникальное наблюдение учтено.",
                "Замер удержания показывает отток 12% в месяц.");
        noSignal("empty side stays quiet",
                "Все эксперты сходятся, поэтому рекомендую запуск, мнения совпали.",
                "");

        List<String> idempotent = new ArrayList<String>();
        HiddenProfilePolicy.detect(
                "Все эксперты сходятся, поэтому рекомендую запуск: мнения совпали, этого достаточно.",
                "Замер удержания показывает отток 12% в месяц, и этого числа нет в общем пересказе.",
                idempotent);
        int once = idempotent.size();
        HiddenProfilePolicy.detect(
                "Все эксперты сходятся, поэтому рекомендую запуск: мнения совпали, этого достаточно.",
                "Замер удержания показывает отток 12% в месяц, и этого числа нет в общем пересказе.",
                idempotent);
        check(once == 1, "one hidden-profile signal, not a stack");
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(idempotent.get(0).contains("possible signal"), "signal stays a hypothesis");

        List<String> via = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Запускать ли тариф?",
                "Все эксперты сходятся, поэтому рекомендую запуск: мнения совпали, этого достаточно.",
                "Замер удержания показывает отток 12% в месяц, и этого числа нет в общем пересказе.",
                via);
        check(contains(via, "hidden profile"), "detectAll wires the hidden profile");
        check(contains(via, "скрытый профиль"), "the kind that was found is named");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Запускать ли платный тариф при таком удержании?",
                "Все эксперты сходятся, поэтому рекомендую запуск: мнения совпали, этого достаточно.",
                "Замер удержания показывает отток 12% в месяц, и этого числа нет в общем пересказе.");
        check(contains(ranked.conflicts, "hidden profile"), "hidden profile survives the deliberation map");
        String map = DeliberationIntelligence.formatForCoordinator(ranked);
        check(map.contains("не вторая выборка"), "coordinator is told agreement is not a second sample");

        System.out.println("HiddenProfilePolicySmokeTest: " + passed + "/" + passed + " passed");
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
        HiddenProfilePolicy.detect(a, b, out);
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
