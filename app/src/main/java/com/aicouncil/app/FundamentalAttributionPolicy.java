package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local fundamental-attribution signals.
 * Explaining an action by character, nature, or "the type of person" without the
 * situation, incentives, or the base rate of that behavior is a hypothesis, not evidence.
 * Distinct from halo effect (unrelated trait) and from belief bias (wanting the conclusion).
 * Coordinator must not average it away.
 */
public final class FundamentalAttributionPolicy {
    private static final int MAX_SIGNALS = 2;

    private FundamentalAttributionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "fundamental attribution: поступок объяснён характером без ситуации, стимулов или базовой частоты");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта сводят поведение к характеру и не называют ситуацию или стимулы");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "fundamental attribution (возможный сигнал, требует контекста): " + core;
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
                "fundamental attribution", "фундаментальн ошибк атрибуц", "фундаментальная атрибуция",
                "ситуац", "обстоятельств", "стимул", "incentive",
                "не только характер", "not only personality", "not only character",
                "situation, not", "ролевое давление", "role pressure",
                "базовая частота поведен", "base rate of the behavior", "base rate of behavior");
    }

    private static boolean characterVerdict(String t) {
        return has(t,
                "он такой человек", "она такой человек",
                "это в его природе", "это в её природе", "это в ее природе",
                "по натуре он", "по натуре она",
                "по характеру он", "по характеру она",
                "плохой человек, поэтому", "хороший человек, поэтому",
                "он просто ленивый, поэтому", "она просто ленивая, поэтому",
                "слабый характер, значит", "сильный характер, значит",
                "he's the type of person", "she's the type of person",
                "that's just his nature", "that's just her nature",
                "it's just who he is", "it's just who she is",
                "bad character explains", "personality explains the outcome");
    }

    private static boolean ignores(String t) {
        return characterVerdict(t) && !acknowledges(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
