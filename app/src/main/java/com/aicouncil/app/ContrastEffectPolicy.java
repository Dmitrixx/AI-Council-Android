package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local contrast-effect signals.
 * Flags a verdict that treats an option as clearly superior or inferior solely because it is
 * compared to a recent extreme (very good or very bad) alternative, without stating absolute
 * value on the relevant dimensions or that the contrast is irrelevant to the real choice.
 * Naming the gap is not itself an error.
 * The coordinator cannot close on the contrast-relative preference alone.
 */
public final class ContrastEffectPolicy {
    private static final int MAX_SIGNALS = 2;

    private ContrastEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, contrastWithoutAbsolute(a) || contrastWithoutAbsolute(b),
                "contrast effect: вердикт закрывает выбор на относительном сравнении с недавним крайним вариантом без абсолютной оценки по измерениям или явного «контраст нерелевантен»");
        add(conflicts, initial, bothContrast(a, b),
                "оба эксперта закрывают на контрасте с крайностью без абсолютной ценности или независимого сравнения");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "contrast effect (possible signal, requires context): " + core;
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
                "contrast effect", "эффект контраста",
                "relative to recent extreme", "относительно недавней крайности",
                "absolute assessment", "абсолютная оценка",
                "contrast is irrelevant", "контраст нерелевантен",
                "independent of previous", "независимо от предыдущего");
    }

    private static boolean contrastWithoutAbsolute(String t) {
        boolean contrastLang = has(t,
                "после того крайнего", "в сравнении с предыдущим крайним",
                "after the extreme", "compared to the previous extreme",
                "looks better after the bad one", "выглядит лучше после плохого",
                "seems worse next to the great one", "кажется хуже рядом с отличным",
                "contrast makes it", "контраст делает его",
                "relative to the recent extreme option", "относительно недавнего крайнего варианта");
        boolean decisive = has(t,
                "поэтому выбира", "значит берём",
                "закрываем", "recommend",
                "therefore we take", "so we choose",
                "этого достаточно", "решает контраст");
        boolean mentionsAbsolute = has(t,
                "абсолютная ценность", "absolute value",
                "независимо от контраста", "independent of contrast",
                "по самим измерениям", "on the dimensions themselves",
                "контраст не влияет", "contrast does not affect");
        return contrastLang && decisive && !mentionsAbsolute && !acknowledges(t);
    }

    private static boolean bothContrast(String a, String b) {
        return contrastWithoutAbsolute(a) && contrastWithoutAbsolute(b);
    }
}
