package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class DecisionQualityPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("opportunity cost",
                "Рекомендую выбрать вариант A как оптимальный вариант.");
        signal("status quo",
                "Лучше оставить как есть, потому что так сейчас и потому что привычно.");
        signal("metric fixation",
                "Единственный критерий — KPI: успех = рост этой метрики.");
        signal("false precision",
                "Выручка точно составит 12.4 млн в следующем квартале.");
        signal("action bias",
                "Бездействие недопустимо, нужно что-то сделать, поэтому действуем.");

        List<String> chorus = new ArrayList<>();
        DecisionQualityPolicy.detect(
                "Однозначно это верно, нет сомнений.",
                "Безусловно верно, it is clear that so.",
                chorus);
        check(!chorus.isEmpty(), "consensus without evidence detected");
        check(chorus.stream().anyMatch(x -> x.contains("консенсус без опоры")),
                "consensus label present");

        noSignal("choice with trade-off",
                "Рекомендую выбрать A, упущенная выгода — отказ от B, trade-off явный.");
        noSignal("status quo with costs compared",
                "Оставить как есть можно, но сравнили издержки перемен и цену бездействия.");
        noSignal("metric with balance",
                "Смотрим KPI, но баланс метрик и dashboard важнее одной цифры.");
        noSignal("precision with range",
                "Оценка около 12 млн, диапазон 10–14, погрешность учтена.");
        noSignal("action with stop rule",
                "Можно действовать, но есть критерий остановки и value of information.");
        noSignal("firm claims with sources",
                "Однозначно по данным исследования https://example.com/s1 n=400.",
                "Безусловно верно: meta-analysis 2023 и RCT подтверждают.");

        List<String> many = new ArrayList<>();
        String packed = "Рекомендую выбрать вариант A как оптимальный вариант. "
                + "Лучше оставить как есть, потому что так сейчас и потому что привычно. "
                + "Единственный критерий — KPI: успех = рост этой метрики. "
                + "Выручка точно составит 12.4 млн. "
                + "Бездействие недопустимо, нужно что-то сделать, поэтому действуем.";
        DecisionQualityPolicy.detect(packed, "Однозначно верно без данных.", many);
        check(many.size() >= 4 && many.size() <= 6, "several distinct quality signals under cap");
        check(many.stream().allMatch(x -> x.contains("возможный сигнал") && x.contains("контекста")),
                "signals stay hypotheses");
        int size = many.size();
        DecisionQualityPolicy.detect(packed, "Однозначно верно без данных.", many);
        check(many.size() == size, "repeat scan does not duplicate");

        System.out.println("DecisionQualityPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String answer) {
        List<String> out = new ArrayList<>();
        DecisionQualityPolicy.detect(answer, "", out);
        check(!out.isEmpty(), label + " detected");
        check(out.stream().allMatch(x -> x.startsWith("качество решения")),
                label + " is labelled as a quality signal");
    }

    private static void noSignal(String label, String answer) {
        noSignal(label, answer, "");
    }

    private static void noSignal(String label, String a, String b) {
        List<String> out = new ArrayList<>();
        DecisionQualityPolicy.detect(a, b, out);
        check(out.isEmpty(), label);
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            System.err.println("FAIL " + label);
            System.exit(1);
        }
        passed++;
    }
}
