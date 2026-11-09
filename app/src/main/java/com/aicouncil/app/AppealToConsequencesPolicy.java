package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local appeal-to-consequences signals (argumentum ad consequentiam).
 * Treating a wished-for or disastrous outcome as proof that a claim is
 * true or false is a hypothesis, not evidence.
 * Naming the fallacy is not itself the error.
 * The coordinator must not close a verdict on consequences alone.
 */
public final class AppealToConsequencesPolicy {
    private static final int MAX_SIGNALS = 2;

    private AppealToConsequencesPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, ignores(a) || ignores(b),
                "апелляция к последствиям: желанный или страшный исход объявляется доказательством истинности или ложности");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта закрывают вывод последствиями, а не независимым основанием");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "appeal to consequences (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }

    private static boolean naivePattern(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "не может быть правдой, потому что иначе",
                "не может быть правдой, потому что тогда",
                "не может быть истиной, потому что тогда",
                "это правда, потому что иначе будет",
                "это верно, потому что иначе",
                "должно быть правдой, иначе",
                "должно быть истиной, иначе",
                "слишком страшные последствия, значит",
                "последствия слишком плохие, значит это не",
                "раз так будет плохо, значит это неправда",
                "это неверно, потому что последствия неприемлемы",
                "это правда, потому что так спокойнее",
                "can't be true because otherwise",
                "cannot be true because otherwise",
                "can't be true because then",
                "cannot be true because then",
                "must be true because otherwise",
                "must be false because otherwise",
                "it is false because the consequences",
                "it is true because the alternative is unacceptable",
                "too terrible to be true",
                "too dire to be false",
                "the consequences would be terrible, so it isn't true",
                "we must believe it or else",
                "true because we need it to be");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "appeal to consequences",
                "argumentum ad consequentiam",
                "апелляция к последствиям",
                "последствия не доказательство",
                "последствия не делают",
                "consequences are not evidence",
                "consequences do not decide",
                "desired consequences are not proof",
                "страшные последствия не доказательство",
                "желаемый исход не доказательство");
    }

    private static boolean has(String text, String... phrases) {
        if (text == null) return false;
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
