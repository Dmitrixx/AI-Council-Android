package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local IKEA-effect signals.
 * Placing disproportionately high value on a product, plan, system or outcome solely because
 * one partially created, assembled, customized or invested personal effort into it,
 * without independent valuation, market comparison or separation of effort from quality,
 * is a hypothesis, not evidence.
 * Distinct from endowment effect (mere ownership) and sunk-cost fallacy (past irrecoverable investment).
 * Coordinator must not treat "I built/assembled it myself so it is better" as settled without external calibration.
 */
public final class IKEAEffectPolicy {
    private static final int MAX_SIGNALS = 2;

    private IKEAEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "IKEA effect: вывод, что частично созданное/собранное/настроенное мной (effort invested) ценнее эквивалентной альтернативы только потому что я вложил труд, без независимой оценки качества или рыночного сравнения");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта завышают ценность результата собственного труда/сборки без внешней калибровки качества");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "IKEA effect (possible signal, requires context): " + core;
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
                "ikea effect", "эффект икеа", "эффект ikea",
                "effort does not equal quality", "труд не равен качеству",
                "independent valuation", "независимая оценка качества",
                "market comparison", "рыночное сравнение",
                "not better just because I built it", "не лучше только потому что собрал сам",
                "separate effort from outcome");
    }

    private static boolean naivePattern(String t) {
        boolean ikea = has(t,
                "собрал сам поэтому", "собрал своими руками", "я сам сделал поэтому лучше",
                "вложил труд поэтому ценнее", "потому что я это построил",
                "because i built it", "because i assembled it", "i made it myself so",
                "my effort makes it better", "effort invested makes it superior",
                "customized by me so better", "настроил сам поэтому лучше",
                "ikea", "эффект сборки");
        return ikea && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
