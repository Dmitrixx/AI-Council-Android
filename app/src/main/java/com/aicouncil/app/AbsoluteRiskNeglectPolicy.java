package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local absolute-risk neglect signals.
 * Flags a verdict that treats a relative risk reduction, percentage improvement,
 * or ratio as decisive while never stating the absolute risk difference,
 * baseline rate, or number needed to treat/harm. Naming the gap is not itself an error.
 * The coordinator cannot close on relative figures alone.
 */
public final class AbsoluteRiskNeglectPolicy {
    private static final int MAX_SIGNALS = 2;

    private AbsoluteRiskNeglectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, relativeWithoutAbsolute(a) || relativeWithoutAbsolute(b),
                "absolute risk neglect: вердикт закрывает выбор относительным снижением риска / процентным улучшением без абсолютного риска, базовой частоты или NNT");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на относительном показателе без абсолютного риска");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "absolute risk neglect (possible signal, requires context): " + core;
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
                "absolute risk", "абсолютный риск",
                "absolute risk reduction", "абсолютное снижение риска",
                "baseline rate", "базовая частота",
                "nnt", "number needed to treat",
                "nn h", "number needed to harm",
                "absolute difference", "абсолютная разница");
    }

    private static boolean relativeWithoutAbsolute(String t) {
        boolean relative = has(t,
                "снижение риска на", "relative risk reduction", "rrr",
                "процентное снижение", "percentage reduction",
                "относительное снижение", "relative reduction",
                "улучшение на %", "improvement of %",
                "снижает риск на", "reduces risk by");
        boolean decisive = has(t,
                "поэтому стоит", "значит надо",
                "therefore recommend", "so we should",
                "recommend", "выбираем", "закрываем",
                "proves we should", "доказывает что надо");
        boolean mentionsAbs = has(t,
                "абсолютн", "absolute",
                "базовая частота", "baseline",
                "nnt", "number needed",
                "из 100", "per 100", "out of 100");
        return relative && decisive && !mentionsAbs && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return relativeWithoutAbsolute(a) && relativeWithoutAbsolute(b);
    }
}
