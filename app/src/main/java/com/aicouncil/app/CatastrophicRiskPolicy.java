package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local catastrophic / asymmetric downside signals.
 * Flags a verdict that treats a high-stakes decision as ordinary expected-value
 * calculation when the downside is severe, irreversible, or potentially ruinous,
 * without explicitly weighing the magnitude of the worst plausible outcome,
 * considering robust safeguards, or acknowledging that the upside does not
 * automatically justify the tail risk.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on average expected value when the left
 * tail is fat or the downside is asymmetric.
 */
public final class CatastrophicRiskPolicy {
    private static final int MAX_SIGNALS = 2;

    private CatastrophicRiskPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresTail(a) || ignoresTail(b),
                "catastrophic risk: вердикт закрывает выбор по средней ожидаемой ценности, игнорируя тяжёлый или необратимый downside / fat tail");
        add(conflicts, initial, ignoresTail(a) && ignoresTail(b),
                "оба эксперта закрывают без явного учёта асимметричного катастрофического риска");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "catastrophic risk (possible signal, requires context): " + core;
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
                "catastrophic", "катастрофический риск", "fat tail", "тяжёлый хвост",
                "ruin", "разорение", "asymmetric downside", "асимметричный downside",
                "worst plausible", "наихудший правдоподобный", "tail risk",
                "страховка от", "safeguard against ruin", "нельзя позволить себе");
    }

    private static boolean ignoresTail(String t) {
        boolean highStakes = has(t,
                "высокий риск", "серьёзные последствия", "может быть катастрофа",
                "irreversible harm", "необратимый вред", "existentially", "экзистенциальный",
                "bankrupt", "обанкротиться", "потерять всё", "lose everything");
        boolean closesOnAverage = has(t,
                "ожидаемая ценность положительная", "expected value is positive",
                "в среднем лучше", "on average better", "поэтому делаем",
                "ev positive so we proceed", "математическое ожидание в плюсе");
        boolean mentionsTail = has(t,
                "но downside", "однако риск разорения", "с учётом хвоста",
                "despite the tail", "with ruin in mind");
        return highStakes && closesOnAverage && !mentionsTail && !acknowledges(t);
    }
}
