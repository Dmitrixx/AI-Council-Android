package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

/** Regression coverage for hindsight bias and ambiguity aversion. */
public final class HindsightAmbiguitySmokeTest {
    private HindsightAmbiguitySmokeTest() {}

    public static void main(String[] args) {
        List<String> hindsight = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "После результата я знал, что именно так и произойдёт.",
                "Причины результата пока нужно проверить.",
                hindsight);
        require(has(hindsight, "hindsight bias"),
                "DeliberationSignals must expose hindsight-bias signals");

        List<String> hindsightControlled = new ArrayList<String>();
        HindsightBiasPolicy.detect(
                "После результата это кажется очевидным, но до события были альтернативные сценарии и неопределённость.",
                "Нужны исходные прогнозы.",
                hindsightControlled);
        require(!has(hindsightControlled, "hindsight bias"),
                "explicit prior uncertainty and alternatives must suppress hindsight-bias signals");

        List<String> ambiguity = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Лучше взять известный риск: вероятность здесь известна, а по другому варианту неизвестно.",
                "Сравним доступные данные отдельно.",
                ambiguity);
        require(has(ambiguity, "ambiguity aversion"),
                "DeliberationSignals must expose ambiguity-aversion signals");

        List<String> ambiguityControlled = new ArrayList<String>();
        AmbiguityAversionPolicy.detect(
                "Лучше взять известный риск: вероятность здесь известна, а по другому варианту неизвестно; сравнили ожидаемую ценность, base rate и ценность информации.",
                "Оценим результат пилота отдельно.",
                ambiguityControlled);
        require(!has(ambiguityControlled, "ambiguity aversion"),
                "explicit expected-value and base-rate comparison must suppress ambiguity-aversion signals");

        List<String> informationValue = new ArrayList<String>();
        AmbiguityAversionPolicy.detect(
                "Не будем собирать данные, потому что пока неясно.",
                "",
                informationValue);
        require(has(informationValue, "игнорирование ценности информации"),
                "refusing data collection under ambiguity should flag missing information value");

        List<String> informationValueControlled = new ArrayList<String>();
        AmbiguityAversionPolicy.detect(
                "Не будем собирать данные, потому что пока неясно; сначала оценим ценность информации и проведём пилот.",
                "",
                informationValueControlled);
        require(!has(informationValueControlled, "игнорирование ценности информации"),
                "explicit information-value assessment should suppress the signal");

        System.out.println("HindsightAmbiguitySmokeTest: 6/6 passed");
    }

    private static boolean has(List<String> signals, String token) {
        for (String signal : signals) {
            if (signal.toLowerCase().contains(token.toLowerCase())) return true;
        }
        return false;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
