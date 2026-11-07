package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local endowment-effect signals.
 * Treating currently owned, held or status-quo assets, options or positions as intrinsically
 * more valuable than equivalent alternatives solely because they are already "mine",
 * without willingness-to-pay / willingness-to-accept calibration or market comparison,
 * is a hypothesis, not evidence.
 * Distinct from status-quo bias (inertia of the default) and loss aversion (asymmetric pain of loss).
 * Coordinator must not treat "I already have it so it is better" as settled without independent valuation.
 */
public final class EndowmentEffectPolicy {
    private static final int MAX_SIGNALS = 2;

    private EndowmentEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "endowment effect: вывод, что уже имеющееся (owned / held / current option) ценнее эквивалентной альтернативы только потому что оно уже «моё», без калибровки WTP/WTA или рыночного сравнения");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта завышают ценность текущего владения без независимой оценки альтернатив или готовности платить");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "endowment effect (possible signal, requires context): " + core;
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
                "endowment effect", "эффект владения", "эффект эндаумента",
                "willingness to pay", "wtp", "willingness to accept", "wta",
                "рыночная оценка", "market value", "независимая оценка",
                "эквивалентная альтернатива", "equivalent alternative",
                "не потому что уже есть", "not just because I own it");
    }

    private static boolean naivePattern(String t) {
        boolean endowment = has(t,
                "уже есть", "уже имею", "это уже моё", "я уже владею",
                "не отдавать то что есть", "keep what i have",
                "it is already mine", "because i own it", "already invested in this",
                "не менять то что работает у меня", "своё лучше чужого",
                "я вложил силы поэтому ценнее", "because i built it",
                "endowment", "владение делает ценнее");
        return endowment && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
