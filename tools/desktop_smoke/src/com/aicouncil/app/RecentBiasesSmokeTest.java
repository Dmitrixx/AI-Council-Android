package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

/** Positive and controlled-negative coverage for the newest local bias policies. */
public final class RecentBiasesSmokeTest {
    private RecentBiasesSmokeTest() {}

    public static void main(String[] args) {
        List<String> survivorship = new ArrayList<String>();
        SurvivorshipBiasPolicy.detect(
                "Истории успеха доказывают, что стратегия работает, следовательно, рекомендую повторить.",
                "", survivorship);
        check(has(survivorship, "survivorship bias"), "visible successes without the failures must be flagged");

        List<String> survivorshipControlled = new ArrayList<String>();
        SurvivorshipBiasPolicy.detect(
                "Истории успеха доказывают, что стратегия работает, следовательно, рекомендую повторить; при этом учтены невидимые провалы и доля отсева.",
                "", survivorshipControlled);
        check(!has(survivorshipControlled, "survivorship bias"),
                "failure and dropout data must suppress survivorship-bias signal");

        List<String> regression = new ArrayList<String>();
        RegressionToMeanPolicy.detect(
                "Рекорд — устойчивая черта команды, и результат всегда будет таким же.",
                "", regression);
        check(has(regression, "regression to the mean"), "permanent interpretation of a record must be flagged");

        List<String> regressionControlled = new ArrayList<String>();
        RegressionToMeanPolicy.detect(
                "Рекорд — устойчивая черта команды и всегда будет сохраняться, но учтены шум измерения и регрессия к среднему.",
                "", regressionControlled);
        check(!has(regressionControlled, "regression to the mean"),
                "measurement noise and regression-to-mean acknowledgement must suppress the signal");

        List<String> authority = new ArrayList<String>();
        AuthorityBiasPolicy.detect(
                "Известный специалист рекомендует проект, следовательно, проект надёжен.",
                "", authority);
        check(has(authority, "authority bias"), "status without substance must be flagged");

        List<String> authorityControlled = new ArrayList<String>();
        AuthorityBiasPolicy.detect(
                "Известный специалист рекомендует проект, следовательно, решение опирается на доказательства, механизм и базовую частоту, проверенные отдельно.",
                "", authorityControlled);
        check(!has(authorityControlled, "authority bias"),
                "independent evidence and mechanism must suppress authority-bias signal");

        List<String> framing = new ArrayList<String>();
        FramingEffectPolicy.detect(
                "Вы получите выгоду, поэтому рекомендую вариант.",
                "Этот вариант означает потерю, поэтому не рекомендую его.",
                framing);
        check(has(framing, "framing effect"), "gain-versus-loss framing conflict must be flagged");

        List<String> framingControlled = new ArrayList<String>();
        FramingEffectPolicy.detect(
                "При такой формулировке вы получите выгоду; альтернативный фрейм также проверен, поэтому рекомендую решение.",
                "При такой формулировке вы получите выгоду; альтернативный фрейм также проверен.",
                framingControlled);
        check(!has(framingControlled, "framing effect"),
                "same frame with an explicit framing check must not trigger");

        List<String> gambler = new ArrayList<String>();
        GamblersFallacyPolicy.detect(
                "После серии одинаковых исходов подряд теперь должен выпасть другой исход, закон средних.",
                "", gambler);
        check(has(gambler, "gambler's fallacy"), "streak correction assumption must be flagged");

        List<String> gamblerControlled = new ArrayList<String>();
        GamblersFallacyPolicy.detect(
                "После серии одинаковых исходов подряд теперь должен выпасть другой исход; события независимы и каждый исход рассматривается отдельно.",
                "", gamblerControlled);
        check(!has(gamblerControlled, "gambler's fallacy"),
                "explicit independence must suppress gambler's-fallacy signal");

        List<String> halo = new ArrayList<String>();
        HaloEffectPolicy.detect(
                "Он привлекательный и известный, значит компетентный и надёжный.",
                "", halo);
        check(has(halo, "halo effect"), "unrelated positive traits used as evidence must be flagged");

        List<String> haloControlled = new ArrayList<String>();
        HaloEffectPolicy.detect(
                "Он привлекательный и известный, но отдельные доказательства подтверждают компетентность и надёжность.",
                "", haloControlled);
        check(!has(haloControlled, "halo effect"),
                "separate evidence must suppress halo-effect signal");

        System.out.println("RecentBiasesSmokeTest: PASS");
    }

    private static boolean has(List<String> signals, String marker) {
        for (String signal : signals) {
            if (signal.toLowerCase(java.util.Locale.ROOT).contains(marker)) return true;
        }
        return false;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
