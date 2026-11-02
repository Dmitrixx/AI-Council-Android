package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local ergodicity / ruin signals.
 * A positive ensemble average is not the wealth path of one player who can be wiped out.
 * Hypotheses requiring context, not verdicts. The coordinator must not average them away.
 */
public final class ErgodicityPolicy {
    private static final int MAX_SIGNALS = 4;

    private ErgodicityPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, positiveAverageIgnoresWipeout(a) || positiveAverageIgnoresWipeout(b),
                "эргодичность: положительное среднее предлагают как причину ставить капитал, который один проигрыш может обнулить");
        add(conflicts, initial, repeatedBetUsesEnsemble(a) || repeatedBetUsesEnsemble(b),
                "путь против ансамбля: серию повторяемых ставок оценивают средним, без медианы капитала и доли банка");
        add(conflicts, initial, bothNoSurvivalLimit(a, b),
                "оба без предела позиции: плечо или весь капитал обсуждаются, но ни один эксперт не назвал долю банка или критерий выживания");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "эргодичность (возможный сигнал, требует контекста): " + core;
        for (String existing : conflicts) {
            if (existing.contains(core.substring(0, Math.min(40, core.length())))) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean survival(String t) {
        return has(t,
                "доля банка", "kelly", "келли", "fraction of bankroll", "размер позиции",
                "не весь капитал", "не все деньги", "критерий выжив", "эргодич", "ergodic",
                "геометрическ", "geometric return", "медиана капитала", "time average",
                "частка банку", "критерій вижив");
    }

    private static boolean positiveAverage(String t) {
        return has(t,
                "положительное матожидание", "матожидание положитель", "positive expected value",
                "expected value is positive", "в среднем плюс", "в среднем выгодно",
                "средний выигрыш", "ожидаемая доходность положитель", "ev положитель",
                "у середньому вигідно");
    }

    private static boolean wipeout(String t) {
        return has(t,
                "all-in", "всё или ничего", "все или ничего", "весь капитал", "все деньги",
                "всю сумму", "кредитное плечо", "кредитным плечом", "с плечом", "leverage",
                "обнулить капитал", "обнулить депозит", "потерять всё", "потерять все",
                "разорит", "банкротств", "увесь капітал", "кредитне плече");
    }

    private static boolean positiveAverageIgnoresWipeout(String t) {
        return positiveAverage(t) && wipeout(t) && !survival(t);
    }

    private static boolean repeatedBetUsesEnsemble(String t) {
        boolean repeated = has(t,
                "если повторять", "каждую сделку", "серия ставок", "repeated bet",
                "каждый раунд", "много раз подряд", "кожен раунд");
        return positiveAverage(t) && repeated && !survival(t);
    }

    private static boolean bothNoSurvivalLimit(String a, String b) {
        String both = a + "\n" + b;
        boolean exposure = has(both,
                "кредитное плечо", "кредитным плечом", "с плечом", "leverage", "all-in",
                "весь капитал", "увесь капітал", "кредитне плече");
        return exposure && goAhead(both) && !survival(a) && !survival(b);
    }

    /** A negated "не стоит вкладывать" must not count as a recommendation to bet. */
    private static boolean goAhead(String t) {
        String stripped = t
                .replace("не стоит влож", "")
                .replace("не стоит постав", "")
                .replace("не нужно вложить", "")
                .replace("не рекомендую влож", "")
                .replace("не следует постав", "");
        return has(stripped,
                "стоит влож", "стоит постав", "нужно вложить", "следует постав",
                "рекомендую влож", "рекомендую постав");
    }
}
