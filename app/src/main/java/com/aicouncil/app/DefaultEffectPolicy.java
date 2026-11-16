package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local default-effect / status-quo-by-default signals.
 * Flags a verdict that treats the pre-selected, default, or current option as clearly
 * superior solely because it is the default, without stating absolute value on the
 * relevant dimensions, independent comparison, or that the default is irrelevant to the real choice.
 * Naming the gap is not itself an error.
 * The coordinator cannot close on the mere fact of default status alone.
 */
public final class DefaultEffectPolicy {
    private static final int MAX_SIGNALS = 2;

    private DefaultEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, defaultWithoutAbsolute(a) || defaultWithoutAbsolute(b),
                "default effect: вердикт закрывает выбор на предустановленном/дефолтном варианте только потому что он дефолтный, без абсолютной оценки по измерениям или явного «дефолт нерелевантен»");
        add(conflicts, initial, bothDefault(a, b),
                "оба эксперта закрывают на дефолте без абсолютной ценности или независимого сравнения");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "default effect (possible signal, requires context): " + core;
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
                "default effect", "эффект дефолта",
                "status quo bias by default", "дефолтный статус-кво",
                "absolute assessment of default", "абсолютная оценка дефолта",
                "default is irrelevant", "дефолт нерелевантен",
                "independent of pre-selection", "независимо от предустановки");
    }

    private static boolean defaultWithoutAbsolute(String t) {
        boolean defaultLang = has(t,
                "дефолтный лучше", "лучше оставить как есть",
                "предустановленный вариант лучше", "берём дефолт",
                "default is better", "stick with the default",
                "pre-selected is superior", "better to keep the current",
                "не менять дефолт", "дефолт решает",
                "as is is preferable", "leave as default");
        boolean decisive = has(t,
                "поэтому выбира", "значит берём",
                "закрываем", "recommend the default",
                "therefore we take the pre-selected", "so we choose the default",
                "этого достаточно", "решает дефолт");
        boolean mentionsAbsolute = has(t,
                "абсолютная ценность", "absolute value",
                "независимо от дефолта", "independent of default",
                "по самим измерениям", "on the dimensions themselves",
                "дефолт не влияет", "default does not affect");
        return defaultLang && decisive && !mentionsAbsolute && !acknowledges(t);
    }

    private static boolean bothDefault(String a, String b) {
        return defaultWithoutAbsolute(a) && defaultWithoutAbsolute(b);
    }
}
