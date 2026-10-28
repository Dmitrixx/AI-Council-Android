package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Conservative, offline detectors for explicit argument-pattern risks.
 *
 * <p>These are triage signals, not proof that a speaker committed a fallacy:
 * every returned note asks the coordinator to inspect the surrounding context.
 * The rules intentionally require narrow phrase combinations to reduce false positives.</p>
 */
public final class ReasoningFallacyPolicy {
    private static final int MAX_SIGNALS = 10;

    private ReasoningFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = normalize(answerA);
        String b = normalize(answerB);
        int initialSize = conflicts.size();

        addIf(conflicts, initialSize, appealToIgnorance(a) || appealToIgnorance(b),
                "апелляция к незнанию: отсутствие доказательств используется как доказательство истинности/ложности вывода");
        addIf(conflicts, initialSize, bandwagon(a) || bandwagon(b),
                "апелляция к большинству: популярность или распространённость подменяет доказательство");
        addIf(conflicts, initialSize, postHoc(a) || postHoc(b),
                "post hoc: последовательность событий может быть ошибочно принята за причинную связь");
        addIf(conflicts, initialSize, specialPleading(a) || specialPleading(b),
                "особое исключение: правило применяется избирательно без указанного обоснованного критерия");
        addIf(conflicts, initialSize, slipperySlope(a) || slipperySlope(b),
                "скользкий склон: цепочка крайних последствий представлена как неизбежная без обоснованных промежуточных шагов");
        addIf(conflicts, initialSize, hastyGeneralization(a) || hastyGeneralization(b),
                "поспешное обобщение: единичный случай используется для вывода обо всей группе");
        addIf(conflicts, initialSize, strawman(a) || strawman(b),
                "искажение позиции (strawman): аргумент другого участника может быть заменён более слабой версией");
        addIf(conflicts, initialSize, adHominem(a) || adHominem(b),
                "переход на личность (ad hominem): оценка человека может подменять разбор его аргумента");
        addIf(conflicts, initialSize, circularReasoning(a) || circularReasoning(b),
                "циркулярное рассуждение: вывод повторяет предпосылку, не добавляя независимого основания");
        addIf(conflicts, initialSize, appealToAuthority(a) || appealToAuthority(b),
                "апелляция к авторитету без опоры: мнение эксперта или источника подменяет разбор данных и аргументов");
        addIf(conflicts, initialSize, appealToTradition(a) || appealToTradition(b),
                "апелляция к традиции: длительность или привычность практики подменяет обоснование её правильности");
        addIf(conflicts, initialSize, tuQuoque(a) || tuQuoque(b),
                "tu quoque: указание на непоследовательность оппонента подменяет разбор самого аргумента");
        addIf(conflicts, initialSize, falseDichotomy(a) || falseDichotomy(b),
                "ложная дихотомия: сведение выбора к двум крайним вариантам без учёта промежуточных");
        addIf(conflicts, initialSize, redHerring(a) || redHerring(b),
                "отвлекающий манёвр (red herring): обсуждение смещается на побочную тему вместо основного вопроса");
        addIf(conflicts, initialSize, appealToEmotion(a) || appealToEmotion(b),
                "апелляция к эмоциям: сильный эмоциональный призыв подменяет разбор аргументов и данных");
        addIf(conflicts, initialSize, falseEquivalence(a) || falseEquivalence(b),
                "ложная эквивалентность: существенно разные явления представлены как равноценные без обоснования сопоставимости");
        addIf(conflicts, initialSize, noTrueScotsman(a) || noTrueScotsman(b),
                "«нет настоящего шотландца»: определение группы сужается после контрпримера, чтобы исключить неудобные случаи");
        addIf(conflicts, initialSize, appealToNature(a) || appealToNature(b),
                "апелляция к природе: естественность явления подменяет обоснование его желательности или правильности");
        addIf(conflicts, initialSize, geneticFallacy(a) || geneticFallacy(b),
                "генетическая ошибка: происхождение или источник идеи используется для оценки её истинности без разбора содержания");
        addIf(conflicts, initialSize, burdenShift(a) || burdenShift(b),
                "перенос бремени доказательства: требование опровергнуть утверждение вместо предоставления положительных оснований");
        addIf(conflicts, initialSize, movingGoalposts(a) || movingGoalposts(b),
                "перенос ворот: критерии принятия меняются после того, как первоначальные условия были выполнены");
    }

    private static void addIf(List<String> out, int initialSize, boolean condition, String message) {
        String formatted = "логический риск — возможный сигнал, требуется проверка контекста: " + message;
        if (!condition || out.size() - initialSize >= MAX_SIGNALS || out.contains(formatted)) return;
        out.add(formatted);
    }

    private static boolean appealToIgnorance(String text) {
        boolean absence = hasAny(text, "нет доказательств", "отсутствие доказательств", "не нашли доказательств",
                "no evidence", "absence of evidence", "no proof", "немає доказів", "відсутність доказів");
        boolean inference = hasAny(text, "значит", "следовательно", "доказывает", "therefore", "thus", "proves", "отже", "отож");
        boolean absoluteConclusion = hasAny(text, "не существует", "невозможно", "точно лож", "точно вер",
                "неверно", "ложь", "does not exist", "doesn't exist", "impossible", "must be false", "must be true",
                "це неправда", "не існує", "неможливо");
        return hasAny(text, "argument from ignorance", "апелляция к незнанию", "довід від незнання")
                || (absence && inference && absoluteConclusion);
    }

    private static boolean bandwagon(String text) {
        if (hasAny(text, "ad populum", "argumentum ad populum", "апелляция к большинству")) return true;
        boolean crowd = hasAny(text, "все так делают", "все так считают", "все используют", "все это делают",
                "everyone does it", "everyone believes it", "everybody uses it", "everyone uses it",
                "most people do it", "усі так роблять", "усі так вважають", "всі використовують");
        boolean validation = hasAny(text, "значит правильно", "поэтому правильно", "значит это верно",
                "so it is right", "therefore it is correct", "so it must be true", "so it must be correct", "therefore it must be correct", "отже це правильно", "тому це вірно");
        return crowd && validation;
    }

    private static boolean postHoc(String text) {
        if (hasAny(text, "post hoc ergo propter hoc", "post hoc fallacy", "ошибка post hoc")) return true;
        boolean after = hasAny(text, "после этого", "после того как", "after this", "after that", "afterwards", "після цього", "після того як");
        boolean cause = hasAny(text, "значит из-за этого", "следовательно из-за", "поэтому это вызвало",
                "значит это причина", "therefore because of this", "so this caused", "must have caused",
                "отже через це", "тому це спричинило");
        return after && cause;
    }

    private static boolean specialPleading(String text) {
        return hasAny(text, "special pleading", "особое исключение без обоснования", "специальное исключение без обоснования")
                || (hasAny(text, "правило для всех кроме нас", "правило для всех, кроме меня",
                            "правило не относится ко мне", "правило не относится к нам",
                            "rules apply to everyone except me", "rules apply to everyone except us",
                            "same rule does not apply to me", "для інших це правило діє, але не для мене",
                            "для всіх, крім нас")
                    && hasAny(text, "потому что я", "потому что мы", "because i", "because we", "просто так", "без объяснения", "без обґрунтування"));
    }

    private static boolean slipperySlope(String text) {
        if (hasAny(text, "slippery slope", "скользкий склон", "ефект доміно без проміжних доказів")) return true;
        boolean opening = hasAny(text, "если разрешить", "если допустить", "если позволить", "if we allow", "if this is allowed",
                "if you let this happen", "якщо дозволити", "якщо допустити");
        boolean inevitability = hasAny(text, "неизбежно приведёт", "в конце концов все", "потом все закончится",
                "will inevitably lead", "then everything will", "will lead to chaos", "eventually everyone",
                "неодмінно призведе", "зрештою всі");
        return opening && inevitability;
    }

    private static boolean hastyGeneralization(String text) {
        if (hasAny(text, "hasty generalization", "поспішне узагальнення", "поспешное обобщение")) return true;
        boolean sample = hasAny(text, "по одному случаю", "на одном примере", "один пример доказывает",
                "one example proves", "from one case", "based on a single case", "за одним випадком");
        boolean broad = hasAny(text, "все люди", "все такие", "всегда так", "никто никогда",
                "everyone is", "all people are", "they all", "always do this", "усі люди", "всі такі");
        return sample && broad;
    }

    private static boolean strawman(String text) {
        return hasAny(text, "strawman", "straw man", "соломенное чучело", "искажает позицию",
                "исказили мою позицию", "перекручивает мои слова", "misrepresents my position",
                "misrepresented the argument", "спотворює позицію", "перекручує мої слова");
    }

    private static boolean adHominem(String text) {
        if (hasAny(text, "ad hominem", "атака на личность", "переход на личности")) return true;
        boolean insult = hasAny(text, "идиот", "дурак", "глупый человек", "ты некомпетентен",
                "некомпетентный человек", "you are an idiot", "you are stupid", "you are incompetent",
                "непрофесіонал", "ти дурний", "ти некомпетентний");
        boolean argument = hasAny(text, "поэтому твой аргумент неверен", "поэтому вас нельзя слушать",
                "so your argument is wrong", "therefore we should ignore you", "тому твій аргумент хибний");
        return insult && argument;
    }

    private static boolean circularReasoning(String text) {
        return hasAny(text, "circular reasoning", "begging the question", "циркулярное рассуждение",
                "круговое доказательство", "тому що це правда, бо це правда",
                "because it is true because", "это верно, потому что это верно",
                "правда, потому что это правда", "істинно, бо воно істинне");
    }

    private static boolean appealToAuthority(String text) {
        if (hasAny(text, "appeal to authority", "апелляция к авторитету", "argumentum ad verecundiam")) return true;
        boolean authority = hasAny(text, "эксперт сказал", "учёные доказали", "авторитетный источник утверждает",
                "expert says", "scientists say", "authority claims", "професор сказав", "експерт стверджує");
        boolean withoutEvidence = hasAny(text, "поэтому это верно", "значит так и есть", "не нужно проверять",
                "so it is true", "therefore it must be", "no need to check", "отже це правда");
        return authority && withoutEvidence;
    }

    private static boolean appealToTradition(String text) {
        if (hasAny(text, "appeal to tradition", "апелляция к традиции", "argumentum ad antiquitatem")) return true;
        boolean tradition = hasAny(text, "так всегда делали", "так принято", "по традиции", "всегда было так",
                "it has always been done this way", "traditional practice", "так завжди робили", "за традицією");
        boolean validation = hasAny(text, "значит правильно", "поэтому так и нужно", "so it is right",
                "therefore we should continue", "отже так і треба");
        return tradition && validation;
    }

    private static boolean tuQuoque(String text) {
        if (hasAny(text, "tu quoque", "ты тоже", "а ты сам", "you also", "what about you")) return true;
        boolean hypocrisy = hasAny(text, "а ты сам так делаешь", "ты тоже нарушаешь", "you do the same",
                "you are a hypocrite", "а ти сам так робиш");
        boolean dismiss = hasAny(text, "поэтому твой аргумент неверен", "значит тебя нельзя слушать",
                "so your point is invalid", "therefore ignore you", "тому твій аргумент недійсний");
        return hypocrisy && dismiss;
    }

    private static boolean falseDichotomy(String text) {
        if (hasAny(text, "false dichotomy", "ложная дихотомия", "false dilemma", "ложная дилемма")) return true;
        boolean eitherOr = hasAny(text, "либо ... либо", "или ... или", "either ... or", "only two choices",
                "либо соглашайся, либо", "або ... або");
        boolean noMiddle = hasAny(text, "других вариантов нет", "третьего не дано", "no other option",
                "nothing in between", "інших варіантів немає");
        return eitherOr && noMiddle;
    }

    private static boolean redHerring(String text) {
        if (hasAny(text, "red herring", "отвлекающий манёвр", "red herring fallacy")) return true;
        boolean divert = hasAny(text, "но что насчёт", "а как же", "what about", "but what about",
                "але що щодо", "а як же");
        boolean ignoreMain = hasAny(text, "вместо ответа", "не отвечает на вопрос", "avoids the question",
                "instead of addressing", "замість відповіді");
        return divert && ignoreMain;
    }

    private static boolean appealToEmotion(String text) {
        if (hasAny(text, "appeal to emotion", "апелляция к эмоциям", "argumentum ad passiones")) return true;
        boolean emotion = hasAny(text, "подумайте о детях", "это ужасно", "как вы можете быть таким жестоким",
                "think of the children", "this is horrible", "how can you be so heartless",
                "подумайте про дітей", "це жахливо", "як ви можете бути таким жорстоким");
        boolean insteadOf = hasAny(text, "вместо аргументов", "не нужно данных", "просто почувствуйте",
                "instead of arguments", "no need for data", "just feel it", "замість аргументів", "не потрібно даних");
        return emotion && insteadOf;
    }

    private static boolean falseEquivalence(String text) {
        if (hasAny(text, "false equivalence", "ложная эквивалентность", "false comparison")) return true;
        boolean equate = hasAny(text, "это одно и то же", "ничем не отличается", "такое же самое",
                "it's the same thing", "no different from", "exactly the same as",
                "це одне й те саме", "нічим не відрізняється");
        boolean different = hasAny(text, "хотя они разные", "несмотря на различия", "even though different",
                "despite the differences", "хоча вони різні");
        return equate && different;
    }

    private static boolean noTrueScotsman(String text) {
        if (hasAny(text, "no true scotsman", "нет настоящего шотландца", "немає справжнього шотландця")) return true;
        boolean redefine = hasAny(text, "настоящий ... никогда бы", "истинный ... не сделал бы",
                "a real ... would never", "no true ... would",
                "справжній ... ніколи б не");
        boolean exclude = hasAny(text, "значит он не настоящий", "поэтому он не считается",
                "so he is not a real", "therefore not a true",
                "отже він не справжній");
        return redefine && exclude;
    }

    private static boolean appealToNature(String text) {
        if (hasAny(text, "appeal to nature", "апелляция к природе", "argumentum ad naturam")) return true;
        boolean natural = hasAny(text, "это естественно", "натурально", "природно", "natural is", "because it is natural",
                "це природно", "натуральне");
        boolean good = hasAny(text, "значит хорошо", "поэтому правильно", "so it is good", "therefore it is better",
                "отже добре", "тому правильно");
        return natural && good;
    }

    private static boolean geneticFallacy(String text) {
        if (hasAny(text, "genetic fallacy", "генетическая ошибка", "генетична помилка")) return true;
        boolean origin = hasAny(text, "произошло от", "исходит из", "originates from", "comes from",
                "походить від", "виникло з");
        boolean dismiss = hasAny(text, "поэтому плохо", "значит неверно", "so it is bad", "therefore invalid",
                "отже погано", "тому невірно");
        return origin && dismiss;
    }

    private static boolean burdenShift(String text) {
        if (hasAny(text, "shifting the burden of proof", "перенос бремени доказательства", "перекладення тягаря доведення")) return true;
        boolean shift = hasAny(text, "докажи что это не так", "докажите обратное", "prove it is not", "disprove it",
                "докажіть що це не так");
        boolean claim = hasAny(text, "я утверждаю", "это так", "i claim", "it is true",
                "я стверджую", "це правда");
        return shift && claim;
    }

    private static boolean movingGoalposts(String text) {
        if (hasAny(text, "moving the goalposts", "перенос ворот", "переміщення воріт")) return true;
        boolean move = hasAny(text, "но этого недостаточно", "нужно ещё", "that is not enough", "need more",
                "але цього недостатньо", "потрібно ще");
        boolean prior = hasAny(text, "хотя ранее требовалось", "раньше было достаточно", "previously required",
                "хоча раніше вимагалося");
        return move && prior;
    }

    private static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT)
                .replace('\u2019', '\'').replace('\u2018', '\'')
                .replace('\u201c', '"').replace('\u201d', '"')
                .replace('\u00a0', ' ').trim();
    }

    private static boolean hasAny(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }
}
