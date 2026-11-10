package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local special-pleading / double-standard policy.
 * Detects an exemption from a rule for one's own side when no relevant
 * difference is named («для нас правила не действуют», «себе можно, другим нельзя»,
 * «the rules don't apply to us»).
 * Naming the fallacy or demanding a relevant difference is not the error.
 * Signals remain hypotheses that require context.
 */
public final class SpecialPleadingPolicy {
    private static final int MAX_SIGNALS = 2;

    private SpecialPleadingPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean pleadA = pleads(a);
        boolean pleadB = pleads(b);
        add(conflicts, initial, pleadA || pleadB,
                "special pleading: исключение из правила без релевантного различия");
        add(conflicts, initial, pleadA && pleadB,
                "оба эксперта делают исключение для своей стороны");
        add(conflicts, initial, pleadA ^ pleadB,
                "special pleading: один эксперт освобождает свою сторону от правила");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "special pleading (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    /** Exemption for one's own side, without treating the name of the fallacy as the error. */
    private static boolean pleads(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "для нас правила не действуют",
                "к нам это правило не относится",
                "себе можно, другим нельзя",
                "для своих сделаем исключение",
                "стандарт строже к ним, чем к нам",
                "в нашем случае это не считается",
                "принцип только для других",
                "мы исключение из этого правила",
                "к конкурентам применять, себе нет",
                "двойной стандарт в нашу пользу",
                "the rules don't apply to us",
                "rules don't apply to us",
                "exception because it's us",
                "doesn't apply when we do it",
                "only applies to them not us",
                "standards for thee but not for me",
                "we are exempt from the rule",
                "exempt ourselves from",
                "double standard in our favor",
                "the principle applies only to others");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "special pleading",
                "двойной стандарт не аргумент",
                "двойной стандарт — не аргумент",
                "релевантное различие",
                "relevant difference",
                "не делай исключение без различия",
                "исключение без различия — не основание",
                "without a relevant difference");
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
