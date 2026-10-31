package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Conservative offline reasoning-risk detectors (v2.36+; false compromise 2.44;
 * texas sharpshooter / relative privation / nirvana in 2.45; continuum/sorites + broader UK markers in 2.47;
 * composition / division / appeal to fear in 2.59).
 * Signals are hypotheses that require context, not definitive fallacy verdicts.
 * Coordinator must rank these highest and must not average them away.
 */
public final class ReasoningFallacyPolicy {
    private static final int MAX_SIGNALS = 8;

    private ReasoningFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, appealToIgnorance(a) || appealToIgnorance(b),
                "апелляция к незнанию: отсутствие найденных доказательств принимается за доказательство ложности");
        add(conflicts, initial, bandwagon(a) || bandwagon(b),
                "бандвагон / апелляция к большинству: популярность выдаётся за корректность");
        add(conflicts, initial, postHoc(a) || postHoc(b),
                "post hoc: временной порядок принимается за причинную связь");
        add(conflicts, initial, specialPleading(a) || specialPleading(b),
                "special pleading: правило объявляется общим, но для «нас» делается исключение без критерия");
        add(conflicts, initial, slipperySlope(a) || slipperySlope(b),
                "скользкий склон: одно разрешение объявляется неизбежным путём к хаосу");
        add(conflicts, initial, hastyGeneralization(a) || hastyGeneralization(b),
                "поспешное обобщение: единичный случай превращается в «все / всегда»");
        add(conflicts, initial, strawman(a) || strawman(b),
                "соломенное чучело: позиция оппонента искажена вместо ответа на исходный тезис");
        add(conflicts, initial, adHominem(a) || adHominem(b),
                "ad hominem: личность или статус подменяет проверку аргумента");
        add(conflicts, initial, circular(a) || circular(b),
                "круговая аргументация: вывод повторяет посылку без новой опоры");
        add(conflicts, initial, appealToAuthority(a) || appealToAuthority(b),
                "апелляция к авторитету: «эксперт сказал» без проверки и без критерия экспертизы");
        add(conflicts, initial, appealToTradition(a) || appealToTradition(b),
                "апелляция к традиции: «так всегда делали» принимается за правильность");
        add(conflicts, initial, tuQuoque(a) || tuQuoque(b),
                "tu quoque: «а ты сам» подменяет проверку содержания");
        add(conflicts, initial, falseDichotomy(a) || falseDichotomy(b),
                "ложная дихотомия: объявлены только два варианта, остальные исключены без обоснования");
        add(conflicts, initial, redHerring(a) || redHerring(b),
                "red herring: вместо ответа на вопрос вводится другая проблема");
        add(conflicts, initial, appealToEmotion(a) || appealToEmotion(b),
                "апелляция к эмоциям: чувство («подумайте о детях») подменяет аргумент");
        add(conflicts, initial, falseEquivalence(a) || falseEquivalence(b),
                "ложная эквивалентность: разные по сути вещи объявлены «одним и тем же»");
        add(conflicts, initial, noTrueScotsman(a) || noTrueScotsman(b),
                "no true scotsman: определение группы сужается post hoc, чтобы исключить контрпример");
        add(conflicts, initial, appealToNature(a) || appealToNature(b),
                "апелляция к природе: «естественно» принимается за «хорошо/правильно»");
        add(conflicts, initial, geneticFallacy(a) || geneticFallacy(b),
                "генетическая ошибка: происхождение источника принимается за опровержение содержания");
        add(conflicts, initial, burdenShift(a) || burdenShift(b),
                "сдвиг бремени доказательства: утверждающий требует опровержения вместо своей опоры");
        add(conflicts, initial, movingGoalposts(a) || movingGoalposts(b),
                "moving goalposts: после выполнения прежнего критерия требования повышаются без нового обоснования");
        add(conflicts, initial, falseCompromise(a) || falseCompromise(b),
                "ложный компромисс: «истина посередине» усредняет развилку без критерия");
        add(conflicts, initial, anchoring(a) || anchoring(b),
                "якорение: первое названное число удерживает оценку рядом без независимой проверки");
        add(conflicts, initial, gambler(a) || gambler(b),
                "ошибка игрока: серия независимых исходов объявлена изменившей вероятность");
        add(conflicts, initial, texasSharpshooter(a) || texasSharpshooter(b),
                "texas sharpshooter: кластер выделен после данных и объявлен значимым");
        add(conflicts, initial, relativePrivation(a) || relativePrivation(b),
                "relative privation: наличие худших проблем используется, чтобы обесценить текущую");
        add(conflicts, initial, nirvana(a) || nirvana(b),
                "nirvana fallacy: неидеальный вариант отвергается вместо сравнения с доступными");
        add(conflicts, initial, continuum(a) || continuum(b),
                "континуум / sorites: отсутствие резкой границы объявлено отсутствием различия");
        add(conflicts, initial, composition(a) || composition(b),
                "composition: свойства частей автоматически переносятся на целое без обоснования");
        add(conflicts, initial, division(a) || division(b),
                "division: свойства целого автоматически переносятся на части без обоснования");
        add(conflicts, initial, appealToFear(a) || appealToFear(b),
                "апелляция к страху: негативный эмоциональный сценарий подменяет проверку вероятности и альтернатив");

        // Cross-answer: opposing recommendations must not be averaged
        if (opposingRecommendations(a, b)) {
            add(conflicts, initial, true,
                    "рекомендации расходятся: одна сторона запрещает, другая усредняет — не усреднять развилку");
        }
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "возможный сигнал логического риска (требует контекста, не вердикт): " + core;
        for (String existing : conflicts) {
            if (existing.contains(core)) return; // idempotent
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static boolean multi(String text, String[] groupA, String[] groupB) {
        return has(text, groupA) && has(text, groupB);
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    // --- detectors ---

    private static boolean appealToIgnorance(String t) {
        return multi(t,
                new String[]{"no evidence", "нет доказательств", "нет данных", "не найдено доказательств",
                        "отсутствие доказательств", "немає доказів"},
                new String[]{"therefore", "значит", "therefore false", "не существует", "неверно",
                        "ложно", "does not exist", "is false", "не існує"});
    }

    private static boolean bandwagon(String t) {
        return multi(t,
                new String[]{"everyone", "все делают", "многие делают", "популярн", "majority",
                        "everyone does", "всі роблять"},
                new String[]{"must be correct", "значит правильно", "должно быть верно",
                        "correct", "правильно", "вірно"});
    }

    private static boolean postHoc(String t) {
        return multi(t,
                new String[]{"после этого", "after this", "після цього", "then", "затем"},
                new String[]{"из-за этого", "caused", "вызвал", "причина", "therefore caused",
                        "значить через це"});
    }

    private static boolean specialPleading(String t) {
        return multi(t,
                new String[]{"правило для всех", "rule for all", "общее правило", "для всех"},
                new String[]{"кроме нас", "except us", "мы особенные", "special", "виняток для нас"});
    }

    private static boolean slipperySlope(String t) {
        return multi(t,
                new String[]{"если разрешить", "if we allow", "если допустить", "якщо дозволити"},
                new String[]{"неизбежно", "inevitably", "приведёт к хаосу", "lead to chaos",
                        "хаосу", "катастроф"});
    }

    private static boolean hastyGeneralization(String t) {
        return multi(t,
                new String[]{"один случай", "one case", "по одному", "один приклад"},
                new String[]{"все люди", "always", "всегда", "все всегда", "всі завжди"});
    }

    private static boolean strawman(String t) {
        return has(t, "misrepresents", "искажает позицию", "искажает мой", "straw man",
                "соломенн", "не то, что я сказал", "перекручує позицію");
    }

    private static boolean adHominem(String t) {
        return multi(t,
                new String[]{"idiot", "идиот", "глуп", "дурак", "stupid", "некомпетентн"},
                new String[]{"argument is wrong", "аргумент неверен", "поэтому неверно",
                        "so your argument", "тому аргумент"});
    }

    private static boolean circular(String t) {
        return has(t, "верно потому что это верно", "true because it is true",
                "это верно, потому что это верно", "тому що це правда");
    }

    private static boolean appealToAuthority(String t) {
        return multi(t,
                new String[]{"эксперт сказал", "expert said", "authority says", "експерт сказав"},
                new String[]{"поэтому это верно", "therefore true", "не нужно проверять",
                        "without checking", "тому це вірно"});
    }

    private static boolean appealToTradition(String t) {
        return multi(t,
                new String[]{"так всегда делали", "always done", "традиция", "так завжди робили"},
                new String[]{"значит правильно", "therefore correct", "правильно", "вірно"});
    }

    private static boolean tuQuoque(String t) {
        return multi(t,
                new String[]{"а ты сам", "you yourself", "you do the same", "а ти сам"},
                new String[]{"поэтому твой аргумент", "so your argument", "аргумент неверен",
                        "тому твій аргумент"});
    }

    private static boolean falseDichotomy(String t) {
        return multi(t,
                new String[]{"либо", "either", "або"},
                new String[]{"других вариантов нет", "no other options", "only two",
                        "інших варіантів немає"});
    }

    private static boolean redHerring(String t) {
        return multi(t,
                new String[]{"но что насчёт", "what about", "а що щодо"},
                new String[]{"вместо ответа", "instead of answering", "основной вопрос",
                        "замість відповіді"});
    }

    private static boolean appealToEmotion(String t) {
        return multi(t,
                new String[]{"подумайте о детях", "think of the children", "подумайте про дітей",
                        "ужасно"},
                new String[]{"вместо аргументов", "instead of arguments", "просто почувствуйте",
                        "feel instead"});
    }

    private static boolean falseEquivalence(String t) {
        return multi(t,
                new String[]{"одно и то же", "the same thing", "одне й те саме"},
                new String[]{"хотя разные", "although different", "хоча різні"});
    }

    private static boolean noTrueScotsman(String t) {
        return multi(t,
                new String[]{"настоящий", "no true", "справжній"},
                new String[]{"никогда бы так не сделал", "would never", "значит не настоящий",
                        "therefore not true"});
    }

    private static boolean appealToNature(String t) {
        return multi(t,
                new String[]{"естественно", "natural", "природно"},
                new String[]{"значит хорошо", "therefore good", "правильно", "добре"});
    }

    private static boolean geneticFallacy(String t) {
        return multi(t,
                new String[]{"от плохого источника", "from a bad source", "з поганого джерела",
                        "произошло от"},
                new String[]{"поэтому неверно", "therefore false", "неверно", "невірно"});
    }

    private static boolean burdenShift(String t) {
        return multi(t,
                new String[]{"я утверждаю", "i claim", "я стверджую"},
                new String[]{"докажи что это не так", "prove it is not", "докажіть що це не так"});
    }

    private static boolean movingGoalposts(String t) {
        return multi(t,
                new String[]{"ранее требовалось", "previously required", "раніше вимагалося"},
                new String[]{"этого недостаточно", "not enough", "нужно ещё", "потрібно ще"});
    }

    private static boolean falseCompromise(String t) {
        return multi(t,
                new String[]{"истина посередине", "truth in the middle", "середина", "усредним",
                        "істина посередині"},
                new String[]{"оба понемногу правы", "both a little right", "оба правы",
                        "обидва трохи праві"});
    }

    private static boolean anchoring(String t) {
        return multi(t,
                new String[]{"первое названное число", "first number", "якорь", "перше назване число"},
                new String[]{"останемся рядом", "stay near", "около него", "поруч з ним"});
    }

    private static boolean gambler(String t) {
        return multi(t,
                new String[]{"три раза подряд", "three times in a row", "серия неудач",
                        "тричі поспіль"},
                new String[]{"вероятность выросла", "probability increased", "должно выпасть",
                        "ймовірність зросла"});
    }

    private static boolean texasSharpshooter(String t) {
        return multi(t,
                new String[]{"после данных", "after the data", "після даних", "выбрали кластер"},
                new String[]{"нарисовали круг", "drew the circle", "паттерн значим",
                        "pattern is significant"});
    }

    private static boolean relativePrivation(String t) {
        return multi(t,
                new String[]{"проблемы хуже", "worse problems", "проблеми гірші"},
                new String[]{"не так плохо", "not so bad", "можно не учитывать",
                        "не важливо"});
    }

    private static boolean nirvana(String t) {
        return multi(t,
                new String[]{"не идеально", "not perfect", "неідеально", "есть недостатки"},
                new String[]{"отвергнуть", "reject", "лучше ничего", "therefore reject"});
    }

    private static boolean continuum(String t) {
        return multi(t,
                new String[]{"нет чёткой границы", "no clear boundary", "немає чіткої межі"},
                new String[]{"нет различия", "no difference", "можно не различать",
                        "немає різниці"});
    }

    private static boolean composition(String t) {
        return multi(t,
                new String[]{"каждая часть", "each part", "кожна частина", "каждый элемент"},
                new String[]{"целое тоже", "the whole too", "ціле теж", "поэтому целое", "therefore the whole"});
    }

    private static boolean division(String t) {
        return multi(t,
                new String[]{"целое имеет", "the whole has", "ціле має", "система имеет"},
                new String[]{"каждая часть тоже", "each part too", "кожна частина теж", "поэтому части", "therefore parts"});
    }

    private static boolean appealToFear(String t) {
        return multi(t,
                new String[]{"будет ужасно", "it will be terrible", "буде жахливо", "страшно подумать", "fear that"},
                new String[]{"поэтому нельзя", "therefore we must not", "тому не можна", "отвергнуть", "reject"});
    }

    private static boolean opposingRecommendations(String a, String b) {
        boolean forbidA = has(a, "запретить", "нельзя", "forbid", "must not", "заборонити");
        boolean forbidB = has(b, "запретить", "нельзя", "forbid", "must not", "заборонити");
        boolean avgA = has(a, "истина посередине", "усредним", "truth in the middle", "середнє");
        boolean avgB = has(b, "истина посередине", "усредним", "truth in the middle", "середнє");
        return (forbidA && avgB) || (forbidB && avgA);
    }
}
