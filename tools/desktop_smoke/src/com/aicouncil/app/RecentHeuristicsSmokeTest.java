package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

/** Regression coverage for availability, representativeness, and planning-fallacy signals. */
public final class RecentHeuristicsSmokeTest {
    private RecentHeuristicsSmokeTest() {}

    public static void main(String[] args) {
        List<String> available = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Помню яркий пример, недавно слышал такую историю; думаю, это часто происходит.",
                "Нужна отдельная проверка исходных данных.", available);
        require(has(available, "эвристика доступности"), "availability heuristic must be integrated");

        List<String> availabilityControlled = new ArrayList<String>();
        AvailabilityPolicy.detect(
                "Помню яркий пример, но базовая частота и статистика по reference class показывают редкость.",
                "Нет дополнительных случаев.", availabilityControlled);
        require(!has(availabilityControlled, "эвристика доступности"),
                "base-rate and systematic data must suppress availability signal");

        List<String> representative = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Это типичный случай, похож на X, значит это X.",
                "Категорию нужно установить по измеримым признакам.", representative);
        require(has(representative, "эвристика репрезентативности"),
                "representativeness heuristic must be integrated");

        List<String> representativeControlled = new ArrayList<String>();
        RepresentativenessPolicy.detect(
                "Это типичный случай, похож на X, но базовая частота и reference class не подтверждают вывод.",
                "Проверим систематические данные.", representativeControlled);
        require(!has(representativeControlled, "эвристика репрезентативности"),
                "base-rate context must suppress representativeness signal");

        List<String> planning = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "На проект потребуется всего 3 дня, это быстро и легко.",
                "Срок зависит от результата интеграционных проверок.", planning);
        require(has(planning, "planning fallacy"), "planning fallacy must be integrated");

        List<String> planningControlled = new ArrayList<String>();
        PlanningFallacyPolicy.detect(
                "На проект потребуется всего 3 дня по истории похожих проектов и их фактическим срокам.",
                "Оценка зависит от контрольной выборки.", planningControlled);
        require(!has(planningControlled, "planning fallacy"),
                "historical reference class must suppress planning-fallacy signal");

        System.out.println("RecentHeuristicsSmokeTest: 6/6 passed");
    }

    private static boolean has(List<String> signals, String prefix) {
        for (String signal : signals) if (signal.startsWith(prefix)) return true;
        return false;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
