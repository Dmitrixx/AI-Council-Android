package com.aicouncil.app;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Local Fermi-decomposition signals.
 * Flags a verdict that treats a precise figure as measured while never
 * decomposing it into estimable factors. Naming the gap is not itself an error.
 * The coordinator cannot close on an undecomposed precise number.
 */
public final class FermiDecompositionPolicy {
    private static final int MAX_SIGNALS = 2;
    private static final Pattern PRECISE = Pattern.compile(
            "(\\d+[.,]\\d+\\s*%)|(ровно\\s+\\d)|(exactly\\s+\\d)|(с точностью до)|(to the decimal)");

    private FermiDecompositionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, preciseWithoutFactors(a) || preciseWithoutFactors(b),
                "fermi decomposition: вердикт закрывает выбор точным числом без разложения на оцениваемые множители");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на неразложенном точном числе");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "fermi decomposition (possible signal, requires context): " + core;
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
                "ферми", "fermi",
                "разлож", "множител",
                "decomposition", "estimable factor",
                "это допущение", "guess, not a measurement",
                "не измерение");
    }

    private static boolean preciseWithoutFactors(String t) {
        boolean precise = PRECISE.matcher(t).find();
        boolean decisive = has(t,
                "поэтому выбираем", "значит надо",
                "therefore we should", "so we should",
                "оценка равна", "the figure is decisive",
                "этого достаточно чтобы", "закрываем выбор");
        return precise && decisive && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return preciseWithoutFactors(a) && preciseWithoutFactors(b);
    }
}
