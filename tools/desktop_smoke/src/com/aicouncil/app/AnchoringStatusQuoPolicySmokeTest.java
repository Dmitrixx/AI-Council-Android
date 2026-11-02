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

        List<String> calibrated = new ArrayList<String>();
        AnchoringPolicy.detect(
                "Оценка около 100 с учётом базовой частоты и чувствительности к якорю.", "", calibrated);
        require(!contains(calibrated, "якорение"), "base-rate adjusted estimate should not trigger anchoring");

        List<String> justified = new ArrayList<String>();
        StatusQuoPolicy.detect(
                "Оставить как есть после сравнения затрат на переключение и выгоды от изменения.",
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
