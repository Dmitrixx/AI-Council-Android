package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local asymmetric error-cost check.
 * Flags a verdict that closes a choice without saying which mistake is more
 * expensive: doing something unnecessary, or missing the chance.
 * Naming the gap is not itself an error.
 * The coordinator cannot close until the costlier mistake is named or marked unknown.
 */
public final class ErrorCostAsymmetryPolicy {
    private static final int MAX_SIGNALS = 2;

    private ErrorCostAsymmetryPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesWithoutAsymmetry(a) || closesWithoutAsymmetry(b),
                "error-cost asymmetry: вердикт закрывает выбор, не сказав, какая ошибка дороже — лишнее действие или упущенный шанс");
        add(conflicts, initial, closesWithoutAsymmetry(a) && closesWithoutAsymmetry(b),
                "оба эксперта закрывают выбор без асимметрии ошибки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "error-cost asymmetry (possible signal, requires context): " + core;
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
                "error-cost", "асимметрия ошибки", "какая ошибка дороже",
                "неизвестно, какая ошибка", "unknown which error",
                "ложная тревога", "false positive", "false negative",
                "упущенный шанс дороже", "лишнее действие дороже");
    }

    private static boolean closesWithoutAsymmetry(String t) {
        boolean closes = has(t,
                "поэтому вердикт", "therefore the conclusion", "закрываем выбор",
                "поэтому выбираем", "so we go with", "однозначно стоит",
                "the choice is clear", "следует запустить");
        return closes && !acknowledges(t);
    }
}
