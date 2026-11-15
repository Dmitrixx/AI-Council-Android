package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local decoy-effect / asymmetric-dominance signals.
 * Flags a verdict that treats one option as clearly superior because a third
 * dominated (decoy) option makes it look better, without stating that the
 * preference may be induced by the decoy, the dominance relation, or a
 * preference that should be stable without the decoy.
 * Naming the gap is not itself an error.
 * The coordinator cannot close on the decoy-induced ranking alone.
 */
public final class DecoyEffectPolicy {
    private static final int MAX_SIGNALS = 2;

    private DecoyEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, decoyInducedPreference(a) || decoyInducedPreference(b),
                "decoy effect: вердикт закрывает выбор на опции, которая выглядит лучше из-за третьей доминируемой (приманки-приманки) опции, без упоминания асимметричного доминирования или стабильности предпочтения без декоя");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на ранжировании, индуцированном декоем, без проверки стабильности");
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
                "decoy", "decoy effect", "asymmetric dominance", "асимметричное доминирование",
                "приманка-приманка", "decoy option", "dominated option",
                "стабильность предпочтения", "preference stability");
    }

    private static boolean decoyInducedPreference(String t) {
        boolean mentionsDecoyOrThird = has(t,
                "третий вариант", "third option", "decoy", "приманка",
                "хуже обоих", "dominated", "доминируется",
                "делает лучше", "makes the other look better");
        boolean decisive = has(t,
                "поэтому выбираем", "ясно лучше", "therefore choose",
                "recommend this", "закрываем на", "clearly better");
        boolean notesIndependence = has(t,
                "без третьего", "without the decoy", "стабильно",
                "независимо от", "preference should be");
        return mentionsDecoyOrThird && decisive && !notesIndependence && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return decoyInducedPreference(a) && decoyInducedPreference(b);
    }
}
