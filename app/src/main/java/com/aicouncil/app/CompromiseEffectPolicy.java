package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local compromise-effect signals.
 * Flags a verdict that treats the middle option as clearly superior solely
 * because it is the compromise between extremes, without stating absolute
 * value on the relevant dimensions, independent comparison, or that the
 * extremes are irrelevant to the real trade-off.
 * Naming the gap is not itself an error.
 * The coordinator cannot close on the middle-position preference alone.
 */
public final class CompromiseEffectPolicy {
    private static final int MAX_SIGNALS = 2;

    private CompromiseEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, compromiseWithoutAbsolute(a) || compromiseWithoutAbsolute(b),
                "compromise effect: вердикт закрывает выбор средним вариантом только потому что он компромисс между крайностями, без абсолютной ценности измерений, независимого сравнения или явного «края нерелевантны»");
        add(conflicts, initial, bothCompromise(a, b),
                "оба эксперта закрывают на позиции «середина» без абсолютной оценки по ключевым измерениям");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "compromise effect (possible signal, requires context): " + core;
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
                "compromise effect", "эффект компромисса",
                "middle option", "средний вариант",
                "extremes are irrelevant", "края нерелевантны",
                "absolute value", "абсолютная ценность",
                "independent of range", "независимо от диапазона");
    }

    private static boolean compromiseWithoutAbsolute(String t) {
        boolean compromiseLang = has(t,
                "средний вариант лучше", "компромиссный вариант",
                "берём середину", "выбираем средний",
                "middle is best", "compromise option",
                "between the extremes", "между крайностями",
                "не слишком дешёвый и не слишком дорогой",
                "золотая середина", "the middle ground is safest",
                "neither extreme", "избегаем крайностей");
        boolean decisive = has(t,
                "поэтому выбира", "значит надо", "закрываем",
                "recommend", "therefore we", "so we take",
                "этого достаточно", "решает компромисс",
                "это оптимально");
        boolean mentionsAbsolute = has(t,
                "абсолютная ценность", "absolute value",
                "независимо от крайностей", "independent of range",
                "по каждому измерению", "on each dimension",
                "края не влияют", "extremes do not matter");
        return compromiseLang && decisive && !mentionsAbsolute && !acknowledges(t);
    }

    private static boolean bothCompromise(String a, String b) {
        return compromiseWithoutAbsolute(a) && compromiseWithoutAbsolute(b);
    }
}
