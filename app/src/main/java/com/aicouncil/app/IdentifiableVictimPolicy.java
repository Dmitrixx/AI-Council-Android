package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local identifiable-victim policy.
 * Flags a verdict that treats one vivid, named case as weightier than a
 * frequency, or that dismisses a counted harm because it has no face.
 * Naming the error is not the error. Signals remain hypotheses that require context.
 */
public final class IdentifiableVictimPolicy {
    private static final int MAX_SIGNALS = 2;

    private IdentifiableVictimPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "identifiable victim: яркая история или «просто цифра» закрыли масштаб");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают вопрос лицом либо безликостью числа");
        add(conflicts, initial, hitA ^ hitB,
                "identifiable victim: один эксперт подменяет частоту одной историей");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "identifiable victim (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "одна история важнее цифр",
                "живой человек значит важнее статистики",
                "статистика безлика поэтому не считается",
                "раз нет лица значит проблемы нет",
                "это просто цифры поэтому неважно",
                "лицо важнее частоты",
                "a single story outweighs the statistics",
                "statistics have no face so they do not count",
                "a named story is proof enough",
                "because we can name them the numbers do not matter",
                "it's just a statistic so it doesn't matter",
                "a named person outweighs the base rate");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "identifiable victim",
                "эффект опознаваемой жертвы",
                "яркая история не отменяет базовую частоту",
                "яркая история не перевешивает частоту",
                "a vivid story does not outweigh the base rate",
                "отсутствие лица не отменяет вред",
                "a missing face does not erase the count");
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
