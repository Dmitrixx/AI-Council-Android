package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local equivocation / etymological-fallacy policy.
 * Detects a conclusion drawn from a name, label, or "original" word meaning
 * («само название доказывает», «исконное значение доказывает», «the name itself proves»).
 * Naming the error, or saying a name is not an argument, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class EquivocationPolicy {
    private static final int MAX_SIGNALS = 2;

    private EquivocationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "equivocation: вывод из названия, ярлыка или исконного значения слова");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта выводят тезис из названия или этимологии");
        add(conflicts, initial, hitA ^ hitB,
                "equivocation: один эксперт выводит тезис из названия");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "equivocation (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "само название доказывает",
                "уже в самом названии ответ",
                "раз так называется, значит так и есть",
                "исконное значение доказывает",
                "этимология показывает, что это правда",
                "изначально слово значило, значит",
                "сама назва доводить",
                "the name itself proves",
                "the original meaning proves",
                "etymology shows it is true",
                "the label proves the claim",
                "as the word literally means, it must");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "equivocation",
                "этимологическая ошибка",
                "этимология не доказательство",
                "etymological fallacy",
                "название не доказательство",
                "не выводи истину из названия",
                "the name is not an argument",
                "original meaning is not evidence",
                "не выводи смысл из корня слова");
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
