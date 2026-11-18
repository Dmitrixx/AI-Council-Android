package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local information-cascade / herding signals.
 * Flags a verdict that treats observed majority action or sequential adoption
 * as independent evidence, without asking whether later agents ignored their
 * private signals and copied earlier movers. Naming the gap is not itself an
 * error. Distinct from pure social proof: here the order and private signals matter.
 * The coordinator cannot close solely because "everyone is already doing it".
 */
public final class InformationCascadePolicy {
    private static final int MAX_SIGNALS = 2;

    private InformationCascadePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresCascade(a) || ignoresCascade(b),
                "information cascade: вердикт закрывает выбор так, будто массовое следование — независимые сигналы");
        add(conflicts, initial, ignoresCascade(a) && ignoresCascade(b),
                "оба эксперта закрывают без учёта информационной каскадной зависимости");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "information cascade (possible signal, requires context): " + core;
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
                "information cascade", "информационный каскад", "інформаційний каскад",
                "private signal ignored", "частный сигнал проигнорирован",
                "herding without info", "следование без своей информации",
                "sequential adoption", "каскадная зависимость",
                "early movers only", "ранние двигатели");
    }

    private static boolean ignoresCascade(String t) {
        boolean herd = has(t,
                "все уже перешли", "everyone already switched", "большинство уже выбрало",
                "majority already chose", "последовали за первыми", "followed the first movers",
                "все делают так", "everyone is doing it", "массово приняли",
                "widely adopted already", "после первых остальные тоже",
                "once a few joined the rest followed");
        boolean independent = has(t,
                "значит каждый независимо прав", "each independently agrees",
                "независимые подтверждения", "independent confirmations",
                "много независимых сигналов", "many independent signals",
                "консенсус доказывает", "consensus proves", "массовость = доказательства",
                "adoption proves quality");
        return herd && independent && !acknowledges(t);
    }
}
