package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local unknown-unknowns / higher-order uncertainty check.
 * Flags a verdict that treats the decision problem as fully specified and known
 * (all relevant factors, risks, and responses are in the model) without naming residual
 * unknown unknowns, black-swan possibilities, model incompleteness, or the need for
 * humility about what has not been considered.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on a fully enumerated risk list when higher-order
 * uncertainty is material.
 */
public final class UnknownUnknownsPolicy {
    private static final int MAX_SIGNALS = 2;

    private UnknownUnknownsPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesWithoutUnknowns(a) || closesWithoutUnknowns(b),
                "unknown unknowns: вердикт закрывает на полностью известном наборе рисков и факторов без упоминания неизвестных неизвестных, неполноты модели или возможности сюрпризов вне рассмотренного");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на исчерпывающем перечне известного, игнорируя высшего порядка неопределённость");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "unknown unknowns (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "unknown unknowns", "неизвестные неизвестные", "black swan", "чёрный лебедь",
                "высшего порядка неопределённость", "higher-order uncertainty", "model incompleteness",
                "неполнота модели", "что мы не учли", "what we have not considered",
                "остаточный риск вне модели", "residual model risk");
    }

    private static boolean closesWithoutUnknowns(String t) {
        boolean completeLang = has(t,
                "все риски учтены", "all risks are accounted", "полный перечень",
                "модель полная", "the model covers everything", "нет других факторов");
        boolean closing = has(t,
                "поэтому закрываем", "therefore we close", "решение однозначно",
                "the choice is settled", "все учтено, делаем");
        return completeLang && closing && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesWithoutUnknowns(a) && closesWithoutUnknowns(b);
    }
}
