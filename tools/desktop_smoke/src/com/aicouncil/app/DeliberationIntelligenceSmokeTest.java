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

        List<DeliberationIntelligence.ArithmeticFault> english = DeliberationIntelligence.findArithmetic("20% of 100 = 30 and 3×4=11.");
        check("flags english percent-of", containsExpr(english, "20% of 100 = 30") && expectedOf(english, "20% of 100 = 30").equals("20"));
        check("flags times sign", containsExpr(english, "3×4=11") && expectedOf(english, "3×4=11").equals("12"));
        check("accepts english percent and times", DeliberationIntelligence.findArithmetic("20% of 100 = 20, 3×4=12.").isEmpty());

        DeliberationIntelligence.Brief choice = DeliberationIntelligence.analyze(
                "Чем хранить очередь задач?",
                "Рекомендую PostgreSQL: транзакции и запас по росту. Уверенность 60–75%. Следующий шаг: проверитьть нагрузку. Если провал — миграция дорогая.",
                "Рекомендую SQLite: проще на одном узле. Уверенность 50–70%. Следующий шаг: замерить запись. Если провал — упрёмся в один файл.");
        check("flags different recommendation targets", containsPrefix(choice.conflicts, "выбор"));
        check("resolution names the crux", DeliberationIntelligence.resolutionLine(choice).contains("РЕШЕНИЕ КАРТЫ")
                && DeliberationIntelligence.formatForCoordinator(choice).contains("выбор"));

        DeliberationIntelligence.Brief sameChoice = DeliberationIntelligence.analyze(
                "Чем хранить очередь?",
                "Рекомендую PostgreSQL и только его.",
                "Тоже рекомендую PostgreSQL для этой очереди.");
        check("same recommendation target is not a choice clash", !containsPrefix(sameChoice.conflicts, "выбор"));

        DeliberationIntelligence.Brief people = DeliberationIntelligence.analyze(
                "Как новая цена затронет клиентов?",
                "Маржа вырастет на 4 пункта при текущей эластичности. Уверенность 50%. Следующий шаг: A/B.",
                "Маржа вырастет на 4 пункта, если спрос удержит объём. Уверенность 50%. Следующий шаг: A/B.");
        check("names a stakeholder nobody discussed", containsText(people.missing, "стейкхолдер не рассмотрен: клиент"));

        DeliberationIntelligence.Brief covered = DeliberationIntelligence.analyze(
                "Как новая цена затронет клиентов?",
                "Клиенты с низким чеком уйдут первыми. Уверенность 40–60%. Следующий шаг: опрос.",
                "Клиенты сохранят объём, если дать порог скидки. Уверенность 40–60%. Следующий шаг: опрос.");
        check("mentioned stakeholder is not a gap", !containsText(covered.missing, "стейкхолдер"));

        DeliberationIntelligence.Brief scope = DeliberationIntelligence.analyze(
                "Какой шанс успеха запуска?",
                "В этом случае шанс высокий, потому что команда уже внедряла похожее.",
                "Base rate похожих запусков — около 20%, reference class важнее истории команды.");
        check("splits case and base rate", containsPrefix(scope.conflicts, "масштаб выборки"));

        DeliberationIntelligence.Brief strayPercent = DeliberationIntelligence.analyze(
                "Оцени шанс",
                "Скидка 15% уже в цене, уверенность здесь не числом, а качественно.",
                "Скидка 70% разрушит маржу, и это тоже не калибровка уверенности.");
        check("bare percents are not a confidence clash", !containsPrefix(strayPercent.conflicts, "уверенность"));

        DeliberationIntelligence.Brief negated = DeliberationIntelligence.analyze(
                "Чи варто запускати?",
                "Так, варто і потрібно запускати. Раджу почати зараз.",
                "Ні, не рекомендую і не варто. Не згоден із запуском.");
        check("ukrainian polarity is a real clash", containsPrefix(negated.conflicts, "полярность"));
        DeliberationIntelligence.Brief cabinet = DeliberationIntelligence.analyze(
                "Стоит ли?",
                "Кабинет уже готов, интернет стабилен, ответ будет позже сегодня.",
                "Кабинет уже готов, интернет стабилен, ответ будет позже сегодня.");
        check("word кабинет is not a no", !containsPrefix(cabinet.conflicts, "полярность"));
        DeliberationIntelligence.Brief realNo = DeliberationIntelligence.analyze(
                "Стоит ли запускать?",
                "Да, рекомендую запуск и лучше начать сейчас.",
                "Не рекомендую запуск: нельзя и не стоит.");
        check("negated recommend clashes with a yes", containsPrefix(realNo.conflicts, "полярность"));

        DeliberationIntelligence.Brief falseYes = DeliberationIntelligence.analyze(
                "Что делать с запуском?",
                "Не рекомендую запуск в этом квартале, нельзя спешить.",
                "Не рекомендую запуск в этом квартале, нельзя спешить.");
        check("negation is not read as agreement to launch", !containsPrefix(falseYes.conflicts, "полярность"));

        DeliberationIntelligence.Brief units = DeliberationIntelligence.analyze(
                "Какая дистанция до склада?",
                "Дистанция около 10 км при текущем маршруте.",
                "Дистанция около 10 миль при текущем маршруте.");
        check("same number in km and miles is a unit clash", containsPrefix(units.conflicts, "единицы"));
        DeliberationIntelligence.Brief converted = DeliberationIntelligence.analyze(
                "Какая дистанция до склада?",
                "Дистанция около 10 км при текущем маршруте.",
                "Дистанция около 6.2 миль при текущем маршруте.");
        check("converted miles are not a fake unit clash", !containsPrefix(converted.conflicts, "единицы"));

        DeliberationIntelligence.Brief money = DeliberationIntelligence.analyze(
                "Сколько стоит лицензия?",
                "Лицензия стоит 100 руб в год при базовом тарифе.",
                "Лицензия стоит 100 usd в год при базовом тарифе.");
        check("same number in different currencies is flagged", containsPrefix(money.conflicts, "валюта"));

        DeliberationIntelligence.Brief premise = DeliberationIntelligence.analyze(
                "Как цена влияет на выручку?",
                "Допущение: спрос неэластичен, поэтому цену можно поднять.",
                "Допущение: спрос эластичен, поэтому цену поднимать нельзя.");
        check("opposite premises are a crux", containsPrefix(premise.conflicts, "предпосылки"));

        DeliberationIntelligence.Brief ukDirection = DeliberationIntelligence.analyze(
                "Що буде з виручкою?",
                "Очікую зростання виручки і раджу запуск.",
                "Очікую падіння виручки і краще уникнути запуску.");
        check("ukrainian direction markers clash", containsPrefix(ukDirection.conflicts, "направление"));

        check("ukrainian percent-of is checked", DeliberationIntelligence.localCheckFooter("20% від 200 = 50").contains("40"));

        DeliberationIntelligence.Brief ranked = DeliberationIntelligence.analyze(
                "Чем хранить данные и какой бюджет?",
                "Рекомендую PostgreSQL. Бюджет 100 руб. Уверенность 80%.",
                "Рекомендую SQLite. Бюджет 250 руб. Уверенность 40%.");
        check("choice outranks a bare number", ranked.conflicts.get(0).startsWith("выбор"));

        check("russian word arithmetic", DeliberationIntelligence.localCheckFooter("2 плюс 3 равно 6").contains("5"));
        check("russian word arithmetic accepts a correct sum", DeliberationIntelligence.findArithmetic("2 плюс 3 равно 5, 10 минус 4 = 6, 3 умножить на 4 равно 12, 8 разделить на 2 = 4.").isEmpty());
        check("flags russian multiply", DeliberationIntelligence.localCheckFooter("3 умножить на 4 равно 11").contains("12"));

        DeliberationIntelligence.Brief forbid = DeliberationIntelligence.analyze(
                "Можно ли хранить ключ API в репозитории?",
                "Ключ API в репозитории запрещаю: секрет утечёт при первой публикации.",
                "Ключ API в репозитории допускаю, если доступ ограничен командой.");
        check("forbid versus allow is exclusivity", containsPrefix(forbid.conflicts, "исключительность"));

        DeliberationIntelligence.Brief calibrated = DeliberationIntelligence.analyze(
                "Оцени шанс успеха",
                "Уверенность 95%. Думаю, что получится само.",
                "Уверенность 55%. Думаю, что получится после проверки.");
        check("high confidence on thin evidence is calibration", containsPrefix(calibrated.conflicts, "калибровка"));

        DeliberationIntelligence.Brief alts = DeliberationIntelligence.analyze(
                "Сравни варианты хранения очереди задач",
                "Рекомендую одно хранилище и сразу переходить. Уверенность 50%. Следующий шаг: замерить запись.",
                "Рекомендую одно хранилище и сразу переходить. Уверенность 50%. Следующий шаг: замерить запись.");
        check("comparison without alternatives is a gap", containsText(alts.missing, "альтернативы"));

        DeliberationIntelligence.Brief supplier = DeliberationIntelligence.analyze(
                "Как смена тарифа затронет поставщика?",
                "Маржа команды вырастет на 4 пункта при текущей схеме.",
                "Маржа команды вырастет на 4 пункта, если объём удержать.");
        check("names a missing supplier", containsText(supplier.missing, "поставщик"));

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

    private static boolean containsText(List<String> items, String part) {
        for (String item : items) if (item.contains(part)) return true;
        return false;
    }
}
