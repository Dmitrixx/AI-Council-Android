package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local denominator-neglect / relative-risk signals.
 * Flags a verdict that treats a relative reduction ("halves the risk", "90% effective",
 * "снижает риск на 50%") as decisive without stating the absolute baseline, base rate,
 * or number needed to treat. Naming the gap is not itself an error.
 * The coordinator cannot close on the relative percentage alone.
 */
public final class DenominatorNeglectPolicy {
    private static final int MAX_SIGNALS = 2;

    private DenominatorNeglectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, relativeWithoutAbsolute(a) || relativeWithoutAbsolute(b),
                "denominator neglect: вердикт закрывает выбор относительным снижением риска без абсолютного риска, базовой частоты или числа, которых нужно пролечить");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на относительном проценте без независимого абсолютного знаменателя");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "denominator neglect (possible signal, requires context): " + core;
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
                "denominator neglect", "пренебрежение знаменателем",
                "absolute risk", "абсолютный риск", "абсолютное снижение",
                "number needed", "nnt",
                "даже абсолютный", "absolute baseline");
    }

    private static boolean relativeWithoutAbsolute(String t) {
        boolean relative = has(t,
                "снижает риск на", "снижает риск в", "вдвое снижает",
                "в два раза снижает", "сокращает риск на", "риск падает на",
                "относительное снижение", "на 90% эффектив", "на 50% эффектив",
                "reduces risk by", "cuts risk by", "cuts the risk",
                "halves the risk", "relative risk reduction", "twice as safe",
                "50% more effective", "90% effective");
        boolean decisive = has(t,
                "поэтому выбира", "значит надо", "значит берём", "закрываем",
                "recommend", "therefore we", "so we should", "so we take",
                "этого достаточно", "процент решает");
        boolean absolute = has(t,
                "абсолютн", "absolute risk", "absolute reduction",
                "базовая частота", "base rate",
                "number needed", "nnt",
                "из 1000", "из 100", "per 1000", "per 100",
                "знаменател", "denominator");
        return relative && decisive && !absolute && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return relativeWithoutAbsolute(a) && relativeWithoutAbsolute(b);
    }
}
