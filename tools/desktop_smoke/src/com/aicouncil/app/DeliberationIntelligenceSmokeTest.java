package com.aicouncil.app;

import java.util.List;

public final class DeliberationIntelligenceSmokeTest {
    private static int checks;

    private static void check(String name, boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
    }

    public static void main(String[] args) {
        List<DeliberationIntelligence.ArithmeticFault> wrong = DeliberationIntelligence.findArithmetic("Итого 2+3=6 и 20% от 200 = 50.");
        check("flags 2+3=6", containsExpr(wrong, "2+3=6") && expectedOf(wrong, "2+3=6").equals("5"));
        check("flags 20 percent of 200", containsExpr(wrong, "20% от 200 = 50"));
        List<DeliberationIntelligence.ArithmeticFault> right = DeliberationIntelligence.findArithmetic("Верно: 2+3=5, 20% от 200 = 40, 10 / 2 = 5.");
        check("accepts correct arithmetic", right.isEmpty());
        check("null arithmetic is empty", DeliberationIntelligence.findArithmetic(null).isEmpty());

        DeliberationIntelligence.Brief clash = DeliberationIntelligence.analyze(
                "Оцени бюджет проекта",
                "Бюджет проекта составляет 100 руб. при текущих допущениях.",
                "Бюджет проекта составляет 250 рублей и это другая оценка.");
        check("detects numeric budget conflict", !clash.conflicts.isEmpty());
        String map = DeliberationIntelligence.formatForCoordinator(clash);
        check("coordinator map names the conflict", map.contains("ЧИСЛОВЫЕ РАСХОЖДЕНИЯ") && map.contains("руб"));

        DeliberationIntelligence.Brief same = DeliberationIntelligence.analyze(
                "Оцени бюджет проекта",
                "Бюджет проекта составляет 100 руб.",
                "Бюджет проекта составляет 100 рублей.");
        check("matching numbers are not a conflict", same.conflicts.isEmpty());
        check("no map when nothing is wrong", DeliberationIntelligence.formatForCoordinator(same).isEmpty());

        String task = "1. Сравни стоимость внедрения локальной модели\n2. Оцени риск утечки ключей API\n3. Предложи порядок запуска";
        List<String> questions = DeliberationIntelligence.subquestions(task);
        check("numbered lines become shared subquestions", questions.size() == 3 && questions.get(1).contains("утечки"));
        String addendum = DeliberationIntelligence.expertAddendum(1, task);
        check("second expert gets the shared map", addendum.contains("ОБЩИЕ ПОДВОПРОСЫ") && addendum.contains("другой метод"));
        check("short task does not invent subquestions", DeliberationIntelligence.expertAddendum(0, "Test task").isEmpty());

        DeliberationIntelligence.Brief thin = DeliberationIntelligence.analyze(task, "Не знаю.", "Ошибка API: timeout");
        check("api failure has zero substance", thin.scoreB == 0);
        check("uncovered subquestions are listed", !thin.missing.isEmpty());
        check("footer quotes the recomputed value", DeliberationIntelligence.localCheckFooter("2*10=15").contains("20"));
        check("correct equation has no footer", DeliberationIntelligence.localCheckFooter("2*10=20").isEmpty());
        check("null answers do not throw", DeliberationIntelligence.analyze(null, null, null).scoreA == 0);

        System.out.println("DeliberationIntelligenceSmokeTest: " + checks + "/" + checks + " passed");
    }

    private static boolean containsExpr(List<DeliberationIntelligence.ArithmeticFault> faults, String part) {
        for (DeliberationIntelligence.ArithmeticFault fault : faults) {
            if (fault.expression.replace(" ", "").contains(part.replace(" ", ""))) return true;
        }
        return false;
    }

    private static String expectedOf(List<DeliberationIntelligence.ArithmeticFault> faults, String part) {
        for (DeliberationIntelligence.ArithmeticFault fault : faults) {
            if (fault.expression.replace(" ", "").contains(part.replace(" ", ""))) return fault.expected;
        }
        return "";
    }
}
