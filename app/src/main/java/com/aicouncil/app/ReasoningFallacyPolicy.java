package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Conservative offline reasoning-risk detectors (v2.36+; false compromise 2.44;
 * texas sharpshooter / relative privation / nirvana in 2.45; continuum/sorites + broader UK markers in 2.47; anecdotal evidence in 2.48).
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
                "апелляция к большинству: распространённость практики принимается за её правильность");
        add(conflicts, initial, specialPleading(a) || specialPleading(b),
                "особое исключение: правило объявляется всеобщим, но автор выводит из него свою сторону без критерия");
        add(conflicts, initial, composition(a) || composition(b),
                "ошибка композиции: свойства частей автоматически переносятся на целое");
        add(conflicts, initial, division(a) || division(b),
                "ошибка деления: свойства целого автоматически переносятся на части");
        add(conflicts, initial, personalIncredulity(a) || personalIncredulity(b),
                "личная недостоверность: «я не понимаю / не верю» используется как опровержение");
        add(conflicts, initial, equivocation(a) || equivocation(b),
                "двусмысленность / эквивокация: одно слово меняет смысл между посылками");
        add(conflicts, initial, appealToNature(a) || appealToNature(b),
                "апелляция к природе: «естественно» отождествляется с «хорошо / правильно»");
        add(conflicts, initial, geneticFallacy(a) || geneticFallacy(b),
                "генетическая ошибка: происхождение тезиса подменяет его содержание");
        add(conflicts, initial, burdenShift(a) || burdenShift(b),
                "сдвиг бремени доказательства: требующий утверждение перекладывает доказательство на оппонента");
        add(conflicts, initial, movingGoalposts(a) || movingGoalposts(b),
                "перемещение ворот: критерии успеха меняются после того, как они были удовлетворены");
        add(conflicts, initial, appealToEmotion(a) || appealToEmotion(b),
                "апелляция к эмоциям: сильное чувство подменяет проверку утверждения");
        add(conflicts, initial, falseEquivalence(a) || falseEquivalence(b),
                "ложная эквивалентность: разные по масштабу/типу вещи уравниваются без обоснования");
        add(conflicts, initial, noTrueScotsman(a) || noTrueScotsman(b),
                "никакой настоящий … : определение группы сужается ad hoc, чтобы исключить контрпример");
        add(conflicts, initial, appealToAuthority(a) || appealToAuthority(b),
                "апелляция к авторитету: мнение авторитета принимается без связи с предметом и доказательствами");
        add(conflicts, initial, appealToTradition(a) || appealToTradition(b),
                "апелляция к традиции: «всегда так делали» подменяет проверку эффективности");
        add(conflicts, initial, tuQuoque(a) || tuQuoque(b),
                "tu quoque: указание на непоследовательность оппонента подменяет разбор тезиса");
        add(conflicts, initial, falseDichotomy(a) || falseDichotomy(b),
                "ложная дихотомия: представлены только два варианта при наличии промежуточных");
        add(conflicts, initial, redHerring(a) || redHerring(b),
                "отвлекающий манёвр: внимание уводится на побочный, эмоционально сильный вопрос");
        add(conflicts, initial, slipperySlope(a) || slipperySlope(b),
                "скользкий склон: цепочка последствий утверждается без вероятностной оценки шагов");
        add(conflicts, initial, strawMan(a) || strawMan(b),
                "соломенное чучело: позиция оппонента упрощена или искажена, затем опровергнута");
        add(conflicts, initial, adHominem(a) || adHominem(b),
                "ad hominem: критика личности подменяет критику аргумента");
        add(conflicts, initial, postHoc(a) || postHoc(b),
                "post hoc: временная последовательность принимается за причинную связь");
        add(conflicts, initial, circular(a) || circular(b),
                "круговая аргументация: вывод уже содержится в посылках без новой информации");
        add(conflicts, initial, hastyGeneralization(a) || hastyGeneralization(b),
                "поспешное обобщение: вывод о классе делается по слишком малой или смещённой выборке");
        add(conflicts, initial, correlationCausation(a) || correlationCausation(b),
                "корреляция ≠ причинность: совместное появление принимается за причинную связь");
        boolean mid = middleGround(a) || middleGround(b);
        boolean oppose = opposingAction(a, b);
        add(conflicts, initial, mid && oppose,
                "ложный компромисс: рекомендации расходятся, а вместо разбора развилки предлагается середина");
        add(conflicts, initial, mid && !oppose,
                "ложный компромисс: истина объявляется серединой без критерия, какая сторона верна");
        add(conflicts, initial, anchoring(a) || anchoring(b),
                "якорение: первое названное число удерживает оценку без независимой оценки");
        add(conflicts, initial, gambler(a) || gambler(b),
                "ошибка игрока: независимые испытания считаются должными компенсировать прошлую серию");
        add(conflicts, initial, texasSharpshooter(a) || texasSharpshooter(b),
                "техасский стрелок: кластер выделен после факта и выдаётся за заранее заданный паттерн");
        add(conflicts, initial, relativePrivation(a) || relativePrivation(b),
                "относительная малозначимость: «есть хуже» подменяет разбор текущего вреда или выгоды");
        add(conflicts, initial, nirvana(a) || nirvana(b),
                "нирвана: хороший вариант отвергается только потому, что он не идеален");
        add(conflicts, initial, continuum(a) || continuum(b),
                "континуум / сорит: отсутствие чёткой границы используется для отрицания различия");
        add(conflicts, initial, anecdotal(a) || anecdotal(b),
                "анекдотическое свидетельство: единичный личный случай подменяет статистику или базовую частоту");
    }

    private static void add(List<String> out, int initial, boolean condition, String message) {
        String formatted = "логический риск — возможный сигнал, требуется проверка контекста: " + message;
        if (!condition || out.contains(formatted)) return;
        int own = 0;
        for (String existing : out) if (existing.startsWith("логический риск")) own++;
        if (own >= MAX_SIGNALS) return;
        out.add(formatted);
    }

    private static boolean appealToIgnorance(String t) {
        return has(t, "no evidence", "нет доказательств", "нет свидетельств", "there is no evidence",
                "немає доказів", "немає свідчень")
                && has(t, "does not exist", "не существует", "is false", "значит ложно", "therefore false",
                "не існує", "значить хибно")
                && !has(t, "unresolved", "remains", "пока не известно", "не доказано и не опровергнуто",
                "поки не відомо", "не доведено і не спростовано");
    }

    private static boolean bandwagon(String t) {
        return has(t, "everyone does it", "все так делают", "все делают это", "большинство считает", "everybody does")
                && has(t, "must be correct", "must be true", "значит верно", "so it is right", "значит правильно");
    }

    private static boolean specialPleading(String t) {
        return has(t, "кроме нас", "except us", "кроме меня", "for everyone except")
                && has(t, "особенн", "we are special", "потому что мы");
    }

    private static boolean composition(String t) {
        return (has(t, "каждая часть", "каждый элемент", "все части") && has(t, "значит целое", "поэтому всё", "hence the whole"))
                || (has(t, "parts are", "each component") && has(t, "therefore the whole", "so the system"));
    }

    private static boolean division(String t) {
        return (has(t, "целое", "система", "the whole") && has(t, "значит каждая часть", "поэтому каждая", "so each part"))
                || (has(t, "group is") && has(t, "therefore every member", "so each member"));
    }

    private static boolean personalIncredulity(String t) {
        return has(t, "я не понимаю", "я не верю", "невозможно представить", "i don't understand", "i can't believe",
                "can't imagine how", "не могу представить")
                && has(t, "значит ложно", "значит неверно", "so it is false", "therefore false");
    }

    private static boolean equivocation(String t) {
        return has(t, "в одном смысле", "в другом смысле", "ambiguous term", "word means", "разные значения слова")
                && has(t, "поэтому", "therefore", "значит");
    }

    private static boolean appealToNature(String t) {
        return has(t, "это естественно", "потому что естественно", "natural therefore", "because it is natural",
                "природа так устроила")
                && has(t, "хорошо", "правильно", "нужно", "good", "right", "should")
                && !has(t, "depends on the context", "зависит от контекста", "desirability");
    }

    private static boolean geneticFallacy(String t) {
        return has(t, "произошло от", "происходит из", "плохого источника", "came from", "invented by", "источник идеи")
                && has(t, "поэтому неверно", "поэтому ложно", "поэтому верно", "so it is false", "so it is true")
                && !has(t, "current validity", "содержание аргумента", "the content of", "needs separate");
    }

    private static boolean burdenShift(String t) {
        return has(t, "докажи что это не", "докажи что нет", "докажите обратное", "prove it is not",
                "prove the opposite", "если не можете опровергнуть")
                && has(t, "я утверждаю", "значит верно", "значит правда", "therefore true");
    }

    private static boolean movingGoalposts(String t) {
        return has(t, "теперь нужно ещё", "этого недостаточно, нужно", "now you also need", "that is not enough, also",
                "изменим критерий", "новый критерий после", "goalposts moved");
    }

    private static boolean appealToEmotion(String t) {
        return has(t, "подумайте о детях", "представьте ребёнка", "подумайте о семье", "how would you feel",
                "imagine the suffering")
                && has(t, "вместо аргумент", "просто почувствуйте", "поэтому нужно", "поэтому нельзя",
                "therefore we must", "so we cannot")
                && !has(t, "данные", "evidence", "исследован", "study", "examine the data");
    }

    private static boolean falseEquivalence(String t) {
        return has(t, "одно и то же", "это то же самое", "exactly the same as", "no different from", "ничем не отличается")
                && !has(t, "по масштабу", "by scale", "differ in important", "share some features");
    }

    private static boolean noTrueScotsman(String t) {
        return has(t, "настоящий", "истинный", "true scotsman", "no true")
                && has(t, "никогда бы", "никогда не", "would never", "не сделал бы", "не настоящий");
    }

    private static boolean appealToAuthority(String t) {
        return has(t, "эксперт сказал", "учёный утверждает", "authority says", "expert said")
                && has(t, "это верно", "значит верно", "therefore true", "so it is settled")
                && !has(t, "данные", "метод", "исследование", "evidence", "study design");
    }

    private static boolean appealToTradition(String t) {
        return has(t, "так всегда делали", "всегда так делали", "we've always", "tradition says", "испокон веков")
                && has(t, "значит правильно", "поэтому правильно", "therefore right", "so we should keep");
    }

    private static boolean tuQuoque(String t) {
        return has(t, "а ты сам", "а сам ты", "ты тоже", "you also", "you yourself", "а вы сами")
                && has(t, "твой аргумент", "your argument", "значит ты не прав", "so your argument");
    }

    private static boolean falseDichotomy(String t) {
        int forks = count(t, "либо");
        boolean closedPair = forks >= 2 || has(t, "только два варианта", "only two options", "третьего не дано",
                "either/or", "no other option");
        boolean sealed = has(t, "других вариантов нет", "третьего не дано", "only two", "therefore", "поэтому");
        return closedPair && sealed && !has(t, "промежуточн", "continuum", "spectrum", "градаци");
    }

    private static boolean redHerring(String t) {
        return has(t, "что насчёт", "но давайте поговорим", "but what about", "instead let's discuss")
                && has(t, "вместо ответа", "другой проблем", "это важнее", "главное же", "the real issue is");
    }

    private static boolean slipperySlope(String t) {
        return has(t, "если разрешить", "if we allow", "скользкий склон", "this will lead to",
                "якщо дозволити", "якщо дозволити це")
                && has(t, "неизбежно", "приведёт к", "затем", "потом", "eventually", "в итоге", "will lead to",
                "неминуче", "приведе до", "потім")
                && !has(t, "вероятност", "probability", "оценка шага", "ймовірність", "оцінка кроку");
    }

    private static boolean strawMan(String t) {
        return has(t, "misrepresents my position", "misrepresent", "straw man", "соломенн",
                "искажает позицию", "искажённая позиция", "оппонент якобы считает", "упрощённая версия аргумента")
                && has(t, "instead of addressing", "но это абсурд", "легко опровергнуть", "obviously false", "instead of");
    }

    private static boolean adHominem(String t) {
        return has(t, "idiot", "идиот", "ad hominem", "character attack", "личный интерес автора", "его мотивы")
                && has(t, "so your argument", "значит аргумент", "therefore your point", "поэтому неверно");
    }

    private static boolean postHoc(String t) {
        return has(t, "после этого значит из-за этого", "после этого — из-за этого", "post hoc", "after this therefore")
                && has(t, "вызвало", "точно вызвало", "caused", "therefore caused");
    }

    private static boolean circular(String t) {
        return has(t, "потому что это верно", "because it is true", "это верно потому что это верно",
                "circular", "begging the question")
                && has(t, "верно", "true", "правильно");
    }

    private static boolean hastyGeneralization(String t) {
        return has(t, "по одному случаю", "по двум примерам", "one case", "a couple of examples")
                && has(t, "все люди", "всегда так", "all people", "always")
                && !has(t, "не обобщать", "not generalize", "needs more data");
    }

    private static boolean correlationCausation(String t) {
        return has(t, "корреляция", "correlation", "вместе встречаются")
                && has(t, "значит причина", "therefore cause", "поэтому вызывает")
                && !has(t, "не значит причина", "not necessarily cause", "confound");
    }

    private static boolean middleGround(String t) {
        return has(t, "истина посередине", "усредним позиции", "split the difference", "truth is in the middle",
                "оба понемногу правы");
    }

    private static boolean opposingAction(String a, String b) {
        return (has(a, "запретить", "нельзя", "ban", "forbid") && has(b, "разрешить", "можно", "allow", "permit"))
                || (has(b, "запретить", "нельзя", "ban") && has(a, "разрешить", "можно", "allow"));
    }

    private static boolean anchoring(String t) {
        return has(t, "первое названное число", "начальная цифра", "first number", "anchor")
                && has(t, "около него", "останемся рядом", "around it", "stay near")
                && !has(t, "отдельно оценил", "independent estimate", "пересчитал");
    }

    private static boolean gambler(String t) {
        return has(t, "три раза подряд", "серия неудач", "three times in a row", "streak of losses")
                && has(t, "должно выпасть", "вероятность выросла", "due for", "must come")
                && !has(t, "независим", "independent", "вероятность не меняется");
    }

    private static boolean texasSharpshooter(String t) {
        return has(t, "после данных выбрали", "нарисовали круг", "cluster after the fact",
                "drew the target after", "cherry-picked the cluster")
                && has(t, "поэтому паттерн", "значит закономерность", "therefore a pattern", "so it is significant")
                && !has(t, "pre-registered", "заранее зафиксирован", "hypothesis before data", "до сбора данных");
    }

    private static boolean relativePrivation(String t) {
        return has(t, "есть проблемы хуже", "не так плохо как", "not as bad as", "there are worse",
                "по сравнению с худшим", "compared to worse")
                && has(t, "поэтому не важно", "не стоит обращать", "therefore ignore", "so it does not matter",
                "можно не учитывать")
                && !has(t, "оба важны", "both matter", "нужно сравнивать масштаб", "scale still matters");
    }

    private static boolean nirvana(String t) {
        return has(t, "не идеально", "не совершенн", "not perfect", "not ideal", "есть недостатки", "не ідеально")
                && has(t, "поэтому отвергнуть", "нельзя принимать", "reject the option", "so discard",
                "лучше ничего", "тому відкинути")
                && !has(t, "лучше текущего", "better than status quo", "incremental", "поэтапн", "улучшает", "краще поточного");
    }

    private static boolean continuum(String t) {
        return has(t, "нет чёткой границы", "нет резкой границы", "no sharp boundary", "no clear cutoff",
                "нема чіткої межі", "відсутня чітка межа", "gray area means")
                && has(t, "значит нет различия", "поэтому различия нет", "therefore no difference",
                "so the distinction fails", "тому різниці немає", "отже відмінності немає")
                && !has(t, "различие всё равно", "difference still matters", "threshold is useful",
                "пороговый критерий", "різниця все одно важлива");
    }

    private static boolean anecdotal(String t) {
        return has(t, "мой знакомый", "у меня был случай", "my friend", "i know someone", "личный опыт",
                "anecdote", "anecdotal", "один раз видел", "мій знайомий", "у мене був випадок")
                && has(t, "значит всегда", "поэтому все", "therefore always", "so everyone", "тому завжди",
                "вероятность высока", "часто так")
                && !has(t, "базовая частота", "base rate", "статистика", "reference class", "не обобщать",
                "базова частота", "не узагальнювати");
    }

    private static int count(String text, String token) {
        int n = 0;
        int from = 0;
        while (from < text.length()) {
            int at = text.indexOf(token, from);
            if (at < 0) break;
            n++;
            from = at + token.length();
        }
        return n;
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
