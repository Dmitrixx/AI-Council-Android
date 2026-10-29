package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ForecastCoherencePolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        List<String> out = new ArrayList<String>();
        ForecastCoherencePolicy.detect("стоит ли запускать",
                "Вероятность успеха 70% и вероятность провала 50%.",
                "Вероятность успеха 60% и вероятность провала 40%.",
                out);
        check(has(out, "не дополняют"), "complement flags 70+50");
        check(!has(out, "ИИ №2"), "60+40 is a coherent complement");

        out.clear();
        ForecastCoherencePolicy.detect("прогноз",
                "По отдельности 40%. И то и другое 55%.",
                "Ничего похожего.",
                out);
        check(has(out, "совместный исход"), "conjunction cannot outrank a part");

        out.clear();
        ForecastCoherencePolicy.detect("прогноз",
                "Варианты взаимоисключающие: только один вариант, 60% и 55%.",
                "Обычный текст без долей.",
                out);
        check(has(out, "больше 100"), "exclusive shares cannot sum past 100");

        out.clear();
        ForecastCoherencePolicy.detect("прогноз запуска",
                "Вероятность успеха 70%. Выигрыш 100. Потеря 20.",
                "Вероятность успеха 20%. Выигрыш 100. Потеря 80.",
                out);
        check(has(out, "знаки расходятся"), "expected values of opposite sign are a crux");
        Double positive = ForecastCoherencePolicy.expectedValue(
                "вероятность успеха 70%. выигрыш 100. потеря 20.");
        Double negative = ForecastCoherencePolicy.expectedValue(
                "вероятность успеха 20%. выигрыш 100. потеря 80.");
        check(positive != null && positive > 0, "70/100/20 is a positive expected value");
        check(negative != null && negative < 0, "20/100/80 is a negative expected value");

        out.clear();
        ForecastCoherencePolicy.detect("стоит ли инвестировать",
                "Вероятность успеха 75%.",
                "Вероятность успеха 40%.",
                out);
        check(has(out, "не усредняй"), "same event 75 vs 40 is not an average");

        out.clear();
        ForecastCoherencePolicy.detect("прогноз срока",
                "Ровно 80% и никак иначе.",
                "Тоже без класса сравнения.",
                out);
        check(has(out, "reference class"), "point forecast without an outside view is flagged");

        out.clear();
        ForecastCoherencePolicy.detect("прогноз срока",
                "Ровно 80%, но reference class похожих запусков около половины.",
                "Тоже опираемся на base rate.",
                out);
        check(!has(out, "reference class"), "an explicit reference class suppresses the gap");

        out.clear();
        ForecastCoherencePolicy.detect("Оцени бюджет проекта",
                "Бюджет проекта составляет 100 руб.",
                "Бюджет проекта составляет 100 рублей.",
                out);
        check(out.isEmpty(), "plain matching budgets stay quiet");

        out.clear();
        ForecastCoherencePolicy.detect("Чем хранить данные и какой бюджет?",
                "Рекомендую PostgreSQL. Бюджет 100 руб. Уверенность 80%.",
                "Рекомендую SQLite. Бюджет 250 руб. Уверенность 40%.",
                out);
        check(out.isEmpty(), "confidence percents are not treated as event probabilities");

        check(ForecastCoherencePolicy.wantsNumericContract("стоит ли запускать продукт"),
                "launch decisions ask for a numeric contract");
        check(!ForecastCoherencePolicy.wantsNumericContract("Оцени бюджет проекта"),
                "a budget note is not a forecast contract");

        String addendum = DeliberationIntelligence.expertAddendum(0, "Стоит ли запускать этот продукт в этом квартале?");
        check(addendum.contains("ЧИСЛОВОЙ КОНТРАКТ"), "experts are asked to state chance, gain and loss");
        check(DeliberationIntelligence.expertAddendum(0, "Test task").isEmpty(),
                "short unrelated task still has no addendum");

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Чем хранить данные и какой бюджет?",
                "Рекомендую PostgreSQL. Бюджет 100 руб. Уверенность 80%.",
                "Рекомендую SQLite. Бюджет 250 руб. Уверенность 40%.");
        check(ranked.conflicts.get(0).startsWith("выбор"),
                "forecast layer does not outrank an untouched choice crux");

        System.out.println("ForecastCoherencePolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static boolean has(List<String> out, String part) {
        for (String line : out) if (line.contains(part)) return true;
        return false;
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            System.err.println("FAIL " + label);
            System.exit(1);
        }
        passed++;
    }
}
