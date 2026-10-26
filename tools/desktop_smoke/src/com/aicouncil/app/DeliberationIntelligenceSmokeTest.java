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
        check("coordinator map names the conflict", map.contains("РАСХОЖДЕНИЯ") && map.contains("руб"));

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

        DeliberationIntelligence.Brief polar = DeliberationIntelligence.analyze(
                "Стоит ли внедрять?",
                "Да, рекомендую внедрять. Нужно и лучше начать сейчас. Согласен с планом.",
                "Нет, не рекомендую. Нельзя и не стоит запускать. Не согласен с планом.");
        boolean hasPolarity = false;
        for (String c : polar.conflicts) if (c.startsWith("полярность")) hasPolarity = true;
        check("detects polarity clash", hasPolarity);
        check("hedge score is defined", polar.hedgeRatio >= 0 && polar.hedgeRatio <= 100);

        DeliberationIntelligence.Brief causal = DeliberationIntelligence.analyze(
                "Почему выросла выручка?",
                "Новая цена вызывает рост выручки, потому что спрос неэластичен в этом сегменте.",
                "Это корреляция: цена связан с выручкой, но не вызывает её сама по себе.");
        check("detects causation vs correlation", containsPrefix(causal.conflicts, "причина"));

        DeliberationIntelligence.Brief horizon = DeliberationIntelligence.analyze(
                "Оцени эффект изменения цены",
                "В ближайшие месяцы эффект краткосрочный и заметный для кассы.",
                "На горизонте лет эффект долгосрочный и другой по знаку.");
        check("splits short-term and long-term", containsPrefix(horizon.conflicts, "горизонт"));

        DeliberationIntelligence.Brief years = DeliberationIntelligence.analyze(
                "Когда вышел стандарт",
                "Стандарт вышел в 2018 году и действует для новых внедрений.",
                "Стандарт вышел в 2022 году и действует для новых внедрений.");
        check("flags nearby year clash", containsPrefix(years.conflicts, "даты"));
        DeliberationIntelligence.Brief sameYear = DeliberationIntelligence.analyze(
                "Когда вышел стандарт",
                "Стандарт вышел в 2018 году.",
                "Стандарт подтверждён в 2018 году.");
        check("same year is not a date clash", !containsPrefix(sameYear.conflicts, "даты"));

        DeliberationIntelligence.Brief door = DeliberationIntelligence.analyze(
                "Стоит ли внедрять систему?",
                "Да, рекомендую внедрять сейчас, эффект понятен.",
                "Да, тоже рекомендую внедрять сейчас, эффект понятен.");
        check("decision without rollback is flagged", containsPrefix(door.conflicts, "обратимость"));
        DeliberationIntelligence.Brief reversible = DeliberationIntelligence.analyze(
                "Стоит ли внедрять систему?",
                "Да, рекомендую: решение обратимо, можно отменить за неделю.",
                "Да, тоже рекомендую: откат описан и решение обратимо.");
        check("reversibility mention suppresses the gap", !containsPrefix(reversible.conflicts, "обратимость"));

        DeliberationIntelligence.Brief confidence = DeliberationIntelligence.analyze(
                "Оцени шанс успеха",
                "Уверенность 90% при текущих данных и узком сценарии.",
                "Уверенность 40% при текущих данных и узком сценарии.");
        check("flags confidence mismatch", containsPrefix(confidence.conflicts, "уверенность"));

        DeliberationIntelligence.Brief direction = DeliberationIntelligence.analyze(
                "Что будет с выручкой?",
                "Ожидаю рост выручки и рекомендую запуск.",
                "Ожидаю падение выручки и советую избегнуть запуска.");
        check("flags directional antonyms", containsPrefix(direction.conflicts, "направление"));

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

    private static boolean containsPrefix(List<String> conflicts, String prefix) {
        for (String conflict : conflicts) if (conflict.startsWith(prefix)) return true;
        return false;
    }
}
