package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class AffectHeuristicPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("liking replaces risk",
                "Мне нравится проект, риски низкие: нравится, значит риск небольшой.",
                "");
        signal("both experts close on taste",
                "Противный, значит бесполезный. Отвращение доказывает провал.",
                "I like it so the risk is low and love it so the downside is small.");
        signal("affect versus separated risk",
                "Бесит, значит провалится.",
                "Симпатия не доказательство: отделим вкус от риска и посчитаем пользу отдельно.");
        noSignal("taste is separated from risk",
                "Нравится, значит риск кажется низким, но симпатия не доказательство.",
                "");
        noSignal("ordinary preference is quiet",
                "Мне ближе первый вариант интерфейса, его и покажем на пилоте.",
                "Согласен с первым вариантом.");
        noSignal("named heuristic plus separation stays quiet",
                "Это affect heuristic, но liking is not a risk estimate.",
                "");

        List<String> idempotent = new ArrayList<String>();
        AffectHeuristicPolicy.detect(
                "Нравится, значит риск низкий, нравится — значит выгодно.",
                "",
                idempotent);
        int once = idempotent.size();
        AffectHeuristicPolicy.detect(
                "Нравится, значит риск низкий, нравится — значит выгодно.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("AffectHeuristicPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        AffectHeuristicPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
