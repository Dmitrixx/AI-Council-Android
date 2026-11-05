package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class BeliefBiasPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("plausibility replaces validity",
                "Звучит правдоподобно, значит аргумент верный и вывод доказан.",
                "");
        signal("desire replaces evidence",
                "Хочу, чтобы это было правдой, значит так и есть.",
                "");
        signal("implausible conclusion kills the argument",
                "Неправдоподобно, значит рассуждение ложное и его можно не проверять.",
                "");
        signal("both experts close on taste for truth",
                "Приятный вывод, значит он верный. Так лучше для нас, значит это правда.",
                "Sounds believable so it is true. I want it to be true, so it is.");
        signal("english welcome conclusion",
                "The conclusion is welcome, so it is proven.",
                "");
        noSignal("plausibility is separated from proof",
                "Звучит правдоподобно, но правдоподобие не доказательство.",
                "");
        noSignal("ordinary caution stays quiet",
                "Вывод выглядит правдоподобно, его всё равно нужно сверить с данными.",
                "Согласен проверить источник до рекомендации.");
        noSignal("named bias plus separation stays quiet",
                "Это belief bias: wanting it true is not evidence.",
                "");

        List<String> idempotent = new ArrayList<String>();
        BeliefBiasPolicy.detect(
                "Звучит правдоподобно, значит аргумент верный.",
                "",
                idempotent);
        int once = idempotent.size();
        BeliefBiasPolicy.detect(
                "Звучит правдоподобно, значит аргумент верный.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        List<String> viaMap = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Звучит правдоподобно, значит аргумент верный.",
                "Бюджет 10 руб.",
                viaMap);
        check(contains(viaMap, "belief bias"), "detectAll wires belief bias");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Стоит ли верить этому длинному разбору запуска?",
                "Звучит правдоподобно, значит аргумент верный. Бюджет 100 руб.",
                "Бюджет 400 руб и другой срок окупаемости.");
        check(!ranked.conflicts.isEmpty() && ranked.conflicts.get(0).contains("belief bias"),
                "belief-bias hypothesis outranks a bare number clash");
        String map = DeliberationIntelligence.formatForCoordinator(ranked);
        check(map.contains("возможный сигнал"), "coordinator map keeps the hypothesis visible");

        DeliberationIntelligence.Brief stacked = DeliberationIntelligence.analyze(
                "Стоит ли принимать желанный вывод двух экспертов как факт?",
                "Хочу, чтобы это было правдой, значит так и есть.",
                "Приятный вывод, значит он верный.");
        String ceiling = DeliberationIntelligence.formatForCoordinator(stacked);
        check(ceiling.contains("ПОТОЛОК УВЕРЕННОСТИ"),
                "several distortion hypotheses cap coordinator confidence");

        System.out.println("BeliefBiasPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        BeliefBiasPolicy.detect(a, b, out);
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
