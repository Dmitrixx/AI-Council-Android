package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Conservative offline reasoning-risk detectors (v2.36+ … v2.42).
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

        // v2.41: composition / division / incredulity / equivocation
        add(conflicts, initial, composition(a) || composition(b),
                "ошибка композиции: свойства частей автоматически переносятся на целое");
        add(conflicts, initial, division(a) || division(b),
                "ошибка деления: свойства целого автоматически переносятся на части");
        add(conflicts, initial, personalIncredulity(a) || personalIncredulity(b),
                "личная недостоверность: «я не понимаю / не верю» используется как опровержение");
        add(conflicts, initial, equivocation(a) || equivocation(b),
                "двусмысленность / эквивокация: одно слово меняет смысл между посылками");

        // v2.39: nature, genetic, burden shift, moving goalposts
        add(conflicts, initial, appealToNature(a) || appealToNature(b),
                "апелляция к природе: «естественно» отождествляется с «хорошо / правильно»");
        add(conflicts, initial, geneticFallacy(a) || geneticFallacy(b),
                "генетическая ошибка: происхождение тезиса подменяет его содержание");
        add(conflicts, initial, burdenShift(a) || burdenShift(b),
                "сдвиг бремени доказательства: требующий утверждение перекладывает доказательство на оппонента");
        add(conflicts, initial, movingGoalposts(a) || movingGoalposts(b),
                "перемещение ворот: критерии успеха меняются после того, как они были удовлетворены");

        // v2.38: emotion, false equivalence, no true scotsman
        add(conflicts, initial, appealToEmotion(a) || appealToEmotion(b),
                "апелляция к эмоциям: сильное чувство подменяет проверку утверждения");
        add(conflicts, initial, falseEquivalence(a) || falseEquivalence(b),
                "ложная эквивалентность: разные по масштабу/типу вещи уравниваются без обоснования");
        add(conflicts, initial, noTrueScotsman(a) || noTrueScotsman(b),
                "никакой настоящий … : определение группы сужается ad hoc, чтобы исключить контрпример");

        // v2.37: authority, tradition, tu quoque, dichotomy, red herring
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

        // Additional high-value conservative detectors
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
    }

    private static void add(List<String> out, int initial, boolean condition, String message) {
        String formatted = "логический риск — возможный сигнал, требуется проверка контекста: " + message;
        if (!condition || out.size() - initial >= MAX_SIGNALS || out.contains(formatted)) return;
        out.add(formatted);
    }

    // --- detectors (conservative multi-marker heuristics) ---

    private static boolean composition(String t) {
        return has(t, "каждая часть", "каждый элемент", "все части") && has(t, "значит целое", "поэтому всё", "hence the whole")
                || has(t, "parts are", "each component") && has(t, "therefore the whole", "so the system");
    }

    private static boolean division(String t) {
        return has(t, "целое", "система", "the whole") && has(t, "значит каждая часть", "поэтому каждая", "so each part")
                || has(t, "group is") && has(t, "therefore every member", "so each member");
    }

    private static boolean personalIncredulity(String t) {
        return has(t, "я не понимаю", "я не верю", "невозможно представить", "i don't understand", "i can't believe",
                "can't imagine how", "не могу представить") && has(t, "значит ложно", "значит неверно", "so it is false", "therefore false");
    }

    private static boolean equivocation(String t) {
        return has(t, "в одном смысле", "в другом смысле", "в зависимости от значения", "ambiguous term",
                "word means", "разные значения слова") && has(t, "поэтому", "therefore", "значит");
    }

    private static boolean appealToNature(String t) {
        return has(t, "потому что естественно", "это природно", "natural therefore", "because it is natural",
                "природа так устроила") && has(t, "хорошо", "правильно", "нужно", "good", "right", "should");
    }

    private static boolean geneticFallacy(String t) {
        return has(t, "происходит из", "придумал", "came from", "invented by", "источник идеи")
                && has(t, "поэтому ложно", "поэтому верно", "so it is false", "so it is true")
                && !has(t, "содержание", "содержание аргумента", "the content of");
    }

    private static boolean burdenShift(String t) {
        return has(t, "докажи что нет", "докажите обратное", "prove it is not", "prove the opposite",
                "если не можете опровергнуть") && has(t, "значит верно", "значит правда", "therefore true");
    }

    private static boolean movingGoalposts(String t) {
        return has(t, "теперь нужно ещё", "этого недостаточно, нужно", "now you also need", "that is not enough, also")
                || has(t, "изменим критерий", "новый критерий после", "goalposts moved");
    }

    private static boolean appealToEmotion(String t) {
        return has(t, "представьте ребёнка", "подумайте о семье", "how would you feel", "imagine the suffering")
                && has(t, "поэтому нужно", "поэтому нельзя", "therefore we must", "so we cannot")
                && !has(t, "данные", "evidence", "исследован", "study");
    }

    private static boolean falseEquivalence(String t) {
        return has(t, "это то же самое что", "ничем не отличается от", "exactly the same as", "no different from")
                && has(t, "поэтому", "therefore") && !has(t, "по масштабу", "by scale", "in degree");
    }

    private static boolean noTrueScotsman(String t) {
        return has(t, "настоящий", "истинный", "true scotsman", "no true", "настоящий специалист")
                && has(t, "не стал бы", "никогда не", "would never", "не сделал бы");
    }

    private static boolean appealToAuthority(String t) {
        return has(t, "эксперт сказал", "учёный утверждает", "authority says", "expert said")
                && has(t, "значит верно", "therefore true", "so it is settled")
                && !has(t, "данные", "метод", "исследование", "evidence", "study design");
    }

    private static boolean appealToTradition(String t) {
        return has(t, "всегда так делали", "традиционно", "we've always", "tradition says", "испокон веков")
                && has(t, "поэтому правильно", "therefore right", "so we should keep");
    }

    private static boolean tuQuoque(String t) {
        return has(t, "а сам ты", "ты тоже", "you also", "you yourself", "а вы сами")
                && has(t, "поэтому твой аргумент", "so your argument", "значит ты не прав");
    }

    private static boolean falseDichotomy(String t) {
        return has(t, "либо … либо", "только два варианта", "either … or", "only two options", "третьего не дано")
                && has(t, "поэтому", "therefore") && !has(t, "промежуточн", "continuum", "spectrum", "градаци");
    }

    private static boolean redHerring(String t) {
        return has(t, "но давайте поговорим о", "а что насчёт", "but what about", "instead let's discuss")
                && has(t, "это важнее", "главное же", "the real issue is");
    }

    private static boolean slipperySlope(String t) {
        return has(t, "если разрешить", "это приведёт к", "if we allow", "this will lead to", "скользкий склон")
                && has(t, "затем", "потом", "eventually", "в итоге") && !has(t, "вероятност", "probability", "оценка шага");
    }

    private static boolean strawMan(String t) {
        return has(t, "оппонент якобы считает", "они хотят сказать", "straw man", "искажённая позиция",
                "упрощённая версия аргумента") && has(t, "но это абсурд", "легко опровергнуть", "obviously false");
    }

    private static boolean adHominem(String t) {
        return has(t, "он сам", "его мотивы", "ad hominem", "character attack", "личный интерес автора")
                && has(t, "поэтому аргумент неверен", "so the argument fails", "значит нельзя доверять");
    }

    private static boolean postHoc(String t) {
        return has(t, "после этого", "затем произошло", "after this", "post hoc", "сразу после")
                && has(t, "значит из-за", "поэтому причина", "caused by", "caused it")
                && !has(t, "контролир", "controlled", "рандомиз", "confound");
    }

    private static boolean circular(String t) {
        return has(t, "потому что это так", "истинно потому что истинно", "begging the question",
                "circular", "доказывает само себя") || (has(t, "определение") && has(t, "уже содержит вывод"));
    }

    private static boolean hastyGeneralization(String t) {
        return has(t, "на основе одного", "всего несколько случаев", "from a single", "only a few cases",
                "в моей практике все") && has(t, "всегда", "все", "always", "everyone", "никто")
                && !has(t, "выборка", "sample size", "статистически", "representative");
    }

    private static boolean correlationCausation(String t) {
        return has(t, "коррелирует", "вместе растут", "correlated", "associated with")
                && has(t, "значит вызывает", "поэтому причина", "causes", "causal")
                && !has(t, "причинн", "causal evidence", "эксперимент", "intervention", "механизм");
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
