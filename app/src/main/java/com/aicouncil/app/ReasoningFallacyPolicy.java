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
    private static final int MAX_SIGNALS = 8;

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
