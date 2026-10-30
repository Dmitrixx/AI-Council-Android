package com.aicouncil.app;

public final class DecisionArithmeticPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        String classic = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Ценность информации. Вероятность успеха 30%. Выигрыш 1000. Потеря 400.");
        check(classic.contains("EVPI: 280"), "classic EVPI");
        check(classic.contains("действия: 20"), "classic expected value of acting");
        check(classic.contains("не рекомендация"), "note is not advice");

        String cheap = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Value of information. Probability of success 30%. Gain 1000. Loss 400. Test cost 50.");
        check(cheap.contains("ниже EVPI"), "cheap perfect test is under the ceiling");

        String dear = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Ценность идеальной информации. Вероятность успеха 30%. Выигрыш 1000. Потеря 400. Цена проверки 400.");
        check(dear.contains("не окупает"), "expensive test cannot beat EVPI");

        String incomplete = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Ценность информации. Вероятность успеха 30%. Выигрыш 1000.");
        check(incomplete.isEmpty(), "two payoff labels are not enough");

        String prose = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Ценность информации огромна, но цифр я не даю.");
        check(prose.isEmpty(), "the phrase without numbers stays quiet");

        String units = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Точка безубыточности. Постоянные затраты 10000, цена 50, переменные затраты 30.");
        check(units.contains("500 единиц"), "break-even quantity");
        check(units.contains("не рекомендация"), "break-even is still not advice");

        String decimalUnits = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Точка безубыточности. Постоянные затраты 80, цена 10.5, переменные затраты 2.5.");
        check(decimalUnits.contains("10 единиц"), "decimal price and cost keep the fractional part");

        String margin = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Безубыточность. Постоянные затраты 1000, цена 10, переменные затраты 12.");
        check(margin.contains("не положительный"), "non-positive contribution has no fake quantity");
        check(!margin.contains("единиц при"), "negative margin does not invent a unit count");

        String bareBreak = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Посчитай точку безубыточности для кафе.");
        check(bareBreak.isEmpty(), "break-even words without numbers stay quiet");

        String kelly = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Критерий Келли. Вероятность выигрыша 60%. Десятичный коэффициент 2.");
        check(kelly.contains("20% банка"), "even-money-plus decimal odds Kelly fraction");
        check(kelly.contains("10% банка"), "half Kelly is reported as arithmetic only");

        String negativeKelly = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Kelly criterion. Win probability 40%. Net odds 1.");
        check(negativeKelly.contains("не положительна"), "negative edge does not suggest a stake");

        String noOdds = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Формула Келли. Вероятность выигрыша 60%.");
        check(noOdds.isEmpty(), "Kelly without odds stays quiet");

        String impossible = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Критерий Келли. Вероятность выигрыша 140%. Десятичный коэффициент 2.");
        check(impossible.isEmpty(), "impossible probability is rejected");

        String odds = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Доля Келли. Вероятность выигрыша 60%. Десятичный коэффициент 1.");
        check(odds.contains("не даёт"), "decimal odds at or below 1 are not a stake");

        String other = DecisionArithmeticPolicy.evaluateExplicitRequest("Посчитай 2+2 и не трогай Келли.");
        check(other.isEmpty(), "ordinary arithmetic does not trigger decision arithmetic");

        String ukrainian = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Цінність інформації. Ймовірність успіху 30%. Виграш 1000. Втрата 400.");
        check(ukrainian.contains("EVPI: 280"), "Ukrainian labels use the same formula");

        String expected = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Ожидаемая ценность. Вероятность успеха 30%. Выигрыш 1000. Потеря 400.");
        check(expected.contains("действия: 20"), "explicit expected value without an information price");
        check(!expected.contains("EVPI"), "plain expected value does not invent EVPI");

        String expectedBare = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Посчитай expected value, чисел нет.");
        check(expectedBare.isEmpty(), "expected-value phrase without numbers stays quiet");

        String bayes = DecisionArithmeticPolicy.evaluateExplicitRequest(
                "Тест положительный. Распространённость 1%, чувствительность 99%, специфичность 95%.");
        check(bayes.isEmpty(), "a Bayes task is not a decision-arithmetic task");

        if (passed != 25) {
            throw new AssertionError("DecisionArithmeticPolicySmokeTest failed: " + passed + "/25");
        }
        System.out.println("DecisionArithmeticPolicySmokeTest: " + passed + "/25 passed");
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        passed++;
    }
}
