package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ConfusionOfTheInversePolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("accuracy copied into the posterior",
                "Тест точен на 99%, значит вероятность болезни 99%.");
        signal("english accuracy equals chance",
                "The test is 99% accurate, so a 99% chance you have the disease.");
        signal("guilt equals the match probability",
                "The chance of guilt equals the match probability, so he did it.");
        noSignal("base rate blocks the phrase",
                "Тест точен на 99%, но базовая частота 1% и большинство срабатываний ложноположительные, это не вероятность болезни 99%.");
        noSignal("naming the fallacy is not committing it",
                "This would be the prosecutor's fallacy if accuracy were copied into the posterior.");
        noSignal("sensitivity labeled as sensitivity is not a posterior",
                "Чувствительность 99%. Специфичность отдельно. Вывод пока не закрыт.");

        String task = "Распространённость 1%, чувствительность 99%, специфичность 99%, положительный тест. Какова вероятность болезни?";
        List<String> wrong = new ArrayList<String>();
        ConfusionOfTheInversePolicy.detectAgainstTask(task,
                "Вероятность болезни 99%.",
                "Нужны ещё данные, я не называю процент.",
                wrong);
        check(contains(wrong, "confusion of the inverse"), "sensitivity is not the posterior when Bayes is known");

        List<String> right = new ArrayList<String>();
        ConfusionOfTheInversePolicy.detectAgainstTask(task,
                "Апостериорная вероятность около 50%, а не чувствительность.",
                "Положительный тест не равен 99%.",
                right);
        check(!contains(right, "confusion of the inverse"), "the real posterior is not flagged");

        List<String> quiet = new ArrayList<String>();
        ConfusionOfTheInversePolicy.detectAgainstTask("Какая завтра погода в городе?",
                "Вероятность болезни 99%.", "", quiet);
        check(quiet.isEmpty(), "without a Bayes triple the numeric check stays quiet");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Точность 99%, значит вероятность болезни 99%.",
                "Я не вижу ошибки.",
                wired);
        check(contains(wired, "confusion of the inverse"), "detectAll wires the inverse check");

        DeliberationIntelligence.Brief brief = DeliberationIntelligence.analyze(
                "Стоит ли верить положительному тесту на редкую болезнь при такой точности?",
                "Тест точен на 99%, значит вероятность болезни 99%.",
                "Нужна базовая частота, точность не равна вероятности гипотезы.");
        String map = DeliberationIntelligence.formatForCoordinator(brief);
        check(map.contains("Не копируй точность"), "coordinator is told not to copy accuracy into the posterior");

        System.out.println("ConfusionOfTheInversePolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        ConfusionOfTheInversePolicy.detect(text, "", conflicts);
        check(contains(conflicts, "confusion of the inverse"), label);
    }

    private static void noSignal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        ConfusionOfTheInversePolicy.detect(text, "", conflicts);
        check(!contains(conflicts, "confusion of the inverse"), label);
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
