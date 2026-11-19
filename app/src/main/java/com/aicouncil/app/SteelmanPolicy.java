package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local steelman signals.
 * Flags a verdict that rejects an opposing view by attacking a weak or cartoon
 * version, without first stating the strongest fair version. Naming the gap is
 * not itself an error. The coordinator cannot close on a caricature.
 */
public final class SteelmanPolicy {
    private static final int MAX_SIGNALS = 2;

    private SteelmanPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, caricatureWithoutSteelman(a) || caricatureWithoutSteelman(b),
                "steelman: вердикт отвергает чужую позицию через слабую карикатуру, без сильнейшей честной формулировки");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на карикатуре без стального человека");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "steelman (possible signal, requires context): " + core;
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
                "steelman", "стальной человек",
                "сильнейшая честная", "сильнейшую честную",
                "strongest version", "charitable reading",
                "честная формулировка", "честную версию");
    }

    private static boolean caricatureWithoutSteelman(String t) {
        boolean cartoon = has(t,
                "карикатур", "straw man", "strawman",
                "соломенн", "абсурдная версия", "absurd version",
                "никто в здравом уме", "only a fool",
                "глупая позиция", "weak version of",
                "they stupidly claim");
        boolean decisive = has(t,
                "поэтому отверг", "значит отверг",
                "therefore we reject", "so we reject",
                "позици", "неверна", "dismiss",
                "закрываем против", "опровергнут");
        return cartoon && decisive && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return caricatureWithoutSteelman(a) && caricatureWithoutSteelman(b);
    }
}
