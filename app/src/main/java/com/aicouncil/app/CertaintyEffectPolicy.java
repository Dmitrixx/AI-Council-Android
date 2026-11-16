package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local certainty-effect / Allais-paradox signals.
 * Flags a verdict that treats a certain (probability-1) smaller outcome as clearly
 * superior to a larger uncertain outcome solely because it is guaranteed, without
 * stating expected value, a consistent risk preference, or explicit awareness that
 * certainty is being overweighted relative to near-certainty.
 * Naming the gap is not itself an error.
 * The coordinator cannot close on the mere fact of certainty alone.
 */
public final class CertaintyEffectPolicy {
    private static final int MAX_SIGNALS = 2;

    private CertaintyEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, certaintyWithoutEV(a) || certaintyWithoutEV(b),
                "certainty effect: вердикт закрывает выбор на гарантированном меньшем исходе только потому что он определённый, без ожидаемой ценности, согласованного отношения к риску или явного «эффект определённости учтён»");
        add(conflicts, initial, bothCertainty(a, b),
                "оба эксперта закрывают на «гарантированном» без сравнения ожидаемых ценностей");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "certainty effect (possible signal, requires context): " + core;
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
                "certainty effect", "эффект определённости",
                "allais", "алле",
                "expected value", "ожидаемая ценность",
                "вероятность 1 перевешивает", "certainty overweights",
                "близко к определённости", "near certainty");
    }

    private static boolean certaintyWithoutEV(String t) {
        boolean certainLang = has(t,
                "гарантированно лучше", "лучше взять наверняка",
                "сто процентов лучше", "определённо лучше",
                "sure thing is better", "certain outcome",
                "100% is preferable", "better the guarantee",
                "берём то что точно", "не рискуем — берём верное",
                "гарантия решает", "certainty wins");
        boolean decisive = has(t,
                "поэтому выбира", "значит берём",
                "закрываем", "recommend the sure",
                "therefore take the certain", "so we choose the guaranteed",
                "этого достаточно", "решает определённость");
        boolean mentionsEV = has(t,
                "ожидаемая ценность", "expected value",
                "математическое ожидание", "ev of",
                "сравнение ожиданий", "compare expectations");
        return certainLang && decisive && !mentionsEV && !acknowledges(t);
    }

    private static boolean bothCertainty(String a, String b) {
        return certaintyWithoutEV(a) && certaintyWithoutEV(b);
    }
}
