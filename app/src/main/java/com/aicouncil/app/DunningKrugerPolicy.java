package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local Dunning-Kruger / illusory superiority signals.
 * Claiming high personal competence, that something is "obvious to anyone who knows",
 * or superior insight without acknowledging limits of knowledge, calibration,
 * or the base rate of expertise is a hypothesis, not evidence.
 * Distinct from overconfidence (certainty language), naive realism, and authority bias.
 * Coordinator must not treat "I/we know better" or "only the competent see this" as settled.
 */
public final class DunningKrugerPolicy {
    private static final int MAX_SIGNALS = 2;

    private DunningKrugerPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "dunning-kruger / illusory superiority: заявление высокой собственной компетентности или «очевидности для знающего» без калибровки границ знаний или base rate экспертизы");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта считают себя более компетентными без признания ограничений знаний или независимой калибровки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "dunning-kruger (possible signal, requires context): " + core;
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
                "dunning-kruger", "эффект даннинга-крюгера",
                "границы моих знаний", "limits of my knowledge",
                "возможно, я недооцениваю сложность", "i may underestimate complexity",
                "нужна независимая калибровка компетентности", "need independent competence calibration",
                "экспертиза не гарантирует правильность", "expertise does not guarantee correctness");
    }

    private static boolean naivePattern(String t) {
        boolean superiority = has(t,
                "я знаю лучше", "мы компетентнее", "очевидно для знающего",
                "любой компетентный видит", "только некомпетентные сомневаются",
                "i know better", "we are more competent", "obvious to anyone who knows",
                "any competent person sees this", "only the incompetent disagree",
                "моя экспертиза выше", "это видно только профессионалам");
        boolean dismissesOthers = has(t,
                "они просто не понимают в тему", "не хватает компетенции чтобы понять",
                "they just don't understand the subject", "lack the competence to see it"
        ) && !has(t, "возможно и я", "i might also");

        return (superiority || dismissesOthers) && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
