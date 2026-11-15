package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local decoy-effect / asymmetric-dominance signals.
 * Flags a verdict that treats one option as clearly superior solely because
 * a dominated (worse on all relevant dimensions) decoy makes it look better,
 * without stating absolute value, independent comparison to other options,
 * or that the decoy is irrelevant to the real choice.
 * Naming the gap is not itself an error.
 * The coordinator cannot close on the decoy-relative preference alone.
 */
public final class DecoyEffectPolicy {
    private static final int MAX_SIGNALS = 2;

    private DecoyEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, decoyWithoutAbsolute(a) || decoyWithoutAbsolute(b),
                "decoy effect: вердикт закрывает выбор сравнением с заведомо худшей приманкой (асимметричное доминирование) без абсолютной ценности, независимого сравнения или явного «приманка нерелевантна»");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на относительном превосходстве над приманкой без абсолютной оценки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "decoy effect (possible signal, requires context): " + core;
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
                "decoy effect", "эффект приманки",
                "asymmetric dominance", "асимметричное доминирование",
                "decoy", "приманка",
                "dominated option", "доминируемый вариант",
                "absolute value", "абсолютная ценность",
                "irrelevant decoy", "приманка нерелевантна");
    }

    private static boolean decoyWithoutAbsolute(String t) {
        boolean decoyLang = has(t,
                "лучше чем этот вариант", "явно лучше чем приманка",
                "better than the worse option", "clearly dominates the decoy",
                "на фоне этого выглядит лучше", "compared to the inferior",
                "этот вариант хуже по всем", "dominated option makes it attractive");
        boolean decisive = has(t,
                "поэтому выбираем", "значит берём этот",
                "therefore choose", "so we pick",
                "recommend this", "закрываем на",
                "это лучший выбор потому что");
        boolean mentionsAbsolute = has(t,
                "абсолютная", "absolute value", "независимо от приманки",
                "другие опции", "other options", "базовая ценность",
                "приманка не влияет", "decoy is irrelevant");
        return decoyLang && decisive && !mentionsAbsolute && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return decoyWithoutAbsolute(a) && decoyWithoutAbsolute(b);
    }
}
