package com.aicouncil.app;

public final class SynthesisProtocolSmokeTest {
    private static int passed;

    public static void main(String[] args) {
        check(SynthesisProtocol.expertBlock(null).isEmpty(), "null task is silent");
        check(SynthesisProtocol.expertBlock("Test task").isEmpty(), "short task is silent");
        check(SynthesisProtocol.coordinatorBlock("Test task", "").isEmpty(), "short coordinator block is silent");

        String decisionTask = "Стоит ли запускать этот продукт в этом квартале и какие риски необратимы?";
        String decision = SynthesisProtocol.expertBlock(decisionTask);
        check(decision.contains("Режим разбора: решение"), "launch question is a decision");
        check(decision.contains("оставить как есть"), "decision lists the status quo");
        check(decision.contains("не усредняй"), "experts are told not to average");
        check(SynthesisProtocol.classify(decisionTask) == SynthesisProtocol.Kind.DECISION, "classify decision");
        String decisionCoord = SynthesisProtocol.coordinatorBlock(decisionTask, "есть конфликт");
        check(decisionCoord.contains("ДЕЛАТЬ") && decisionCoord.contains("СНАЧАЛА ПРОВЕРИТЬ"), "decision ends with an action label");
        check(decisionCoord.contains("Не сглаживай"), "a mapped conflict is not averaged");
        String emptyMap = SynthesisProtocol.coordinatorBlock(decisionTask, "");
        check(emptyMap.contains("Локальная карта пуста"), "agreement alone is not confidence");

        String forecastTask = "Дай прогноз: какова вероятность успеха этого запуска через год?";
        check(SynthesisProtocol.classify(forecastTask) == SynthesisProtocol.Kind.FORECAST, "classify forecast");
        String forecast = SynthesisProtocol.expertBlock(forecastTask);
        check(forecast.contains("outside view") && forecast.contains("диапазоном"), "forecast asks for a range and a reference class");
        check(SynthesisProtocol.coordinatorBlock(forecastTask, "").contains("не заменяй средним"), "forecasts are not averaged");

        String mixed = "Стоит ли запускать, если прогноз спроса через год слабый?";
        check(SynthesisProtocol.classify(mixed) == SynthesisProtocol.Kind.DECISION, "a go decision outranks a single forecast cue");

        String fact = "Проверь факт: правда ли, что эта цифра из отчёта, и не выдумывай источник.";
        check(SynthesisProtocol.classify(fact) == SynthesisProtocol.Kind.FACT, "classify fact check");
        check(SynthesisProtocol.expertBlock(fact).contains("Не выдумывай цитаты"), "fact mode forbids invented citations");

        String design = "Спроектируй архитектуру локального сервиса и напиши, как сделать первый прототип.";
        check(SynthesisProtocol.classify(design) == SynthesisProtocol.Kind.DESIGN, "classify design");
        check(SynthesisProtocol.expertBlock(design).contains("режим отказа"), "design names a failure mode");

        String why = "Объясни, почему метрика выросла, и как работает этот механизм на практике сегодня.";
        check(SynthesisProtocol.classify(why) == SynthesisProtocol.Kind.EXPLANATION, "classify explanation");
        check(SynthesisProtocol.expertBlock(why).contains("два механизма"), "explanation asks for two mechanisms");

        String general = "Собери короткий разбор этой длинной заметки без специальной команды режима.";
        check(general.length() >= 40, "fixture is long enough");
        check(SynthesisProtocol.classify(general) == SynthesisProtocol.Kind.GENERAL, "unmarked task stays general");
        check(SynthesisProtocol.expertBlock(general).contains("дешёвый следующий шаг"), "general mode still asks for a test");
        check(!decision.contains("Неэргодичный"), "an ordinary launch is not treated as a ruin bet");

        String ruinTask = "Стоит ли вложить весь капитал с кредитным плечом, если ожидаемая доходность в среднем плюс?";
        check(ruinTask.length() >= 40, "ruin fixture is long enough");
        String ruin = SynthesisProtocol.expertBlock(ruinTask);
        check(ruin.contains("Неэргодичный риск"), "levered all-in task warns that the average is not the path");
        check(ruin.contains("долю банка"), "levered task asks for a bankroll fraction");
        check(SynthesisProtocol.coordinatorBlock(ruinTask, "").contains("all-in"), "coordinator refuses to average a ruin bet into all-in");

        System.out.println("SynthesisProtocolSmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
