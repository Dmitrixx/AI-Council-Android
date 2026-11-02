package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

/** Regression coverage for numeric anchoring and status-quo signals. */
public final class AnchoringStatusQuoPolicySmokeTest {
    private AnchoringStatusQuoPolicySmokeTest() {}

    public static void main(String[] args) {
        List<String> integrated = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Оценка около 100 без базовой частоты и независимой проверки.",
                "Лучше оставить как есть, потому что так привычнее.",
                integrated);
        require(contains(integrated, "якорение"), "DeliberationSignals must expose numeric anchoring");
        require(contains(integrated, "status quo"), "DeliberationSignals must expose status quo bias");

        List<String> matchingResults = new ArrayList<String>();
        AnchoringPolicy.detect("Бюджет проекта составляет 100 руб.",
                "Бюджет проекта составляет 100 рублей.", matchingResults);
        require(!contains(matchingResults, "якорение"), "matching ordinary values are not shared anchors");

        List<String> explicitAnchor = new ArrayList<String>();
        AnchoringPolicy.detect("Первое названное число — около 100.",
                "Начальное значение около 110 принято за основу.", explicitAnchor);
        require(contains(explicitAnchor, "якорение"), "explicit shared starting anchor should be flagged");

        List<String> calibrated = new ArrayList<String>();
        AnchoringPolicy.detect(
                "Оценка около 100 с учётом базовой частоты и чувствительности к якорю.", "", calibrated);
        require(!contains(calibrated, "якорение"), "base-rate adjusted estimate should not trigger anchoring");

        List<String> ordinaryEstimate = new ArrayList<String>();
        AnchoringPolicy.detect("Оценка составляет 100 единиц по итогам расчёта и измерений.", "", ordinaryEstimate);
        require(!contains(ordinaryEstimate, "якорение"), "ordinary measured estimate should not be treated as an anchor");

        List<String> justified = new ArrayList<String>();
        StatusQuoPolicy.detect(
                "Оставить как есть после сравнения: затраты на переключение и выгода от изменения оценены.",
                "Проверить альтернативу и стоимость смены.", justified);
        require(!contains(justified, "status quo"), "explicit switching-cost comparison should suppress status quo signal");
        System.out.println("Anchoring/status-quo policy smoke tests passed.");
    }

    private static boolean contains(List<String> signals, String token) {
        for (String signal : signals) if (signal.toLowerCase().contains(token.toLowerCase())) return true;
        return false;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
