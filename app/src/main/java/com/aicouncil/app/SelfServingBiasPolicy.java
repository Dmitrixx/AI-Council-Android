package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local self-serving bias signals.
 * Attributing success to skill/character of self (or in-group) and failure to luck/situation,
 * or the reverse for others, without balancing both sides, is a hypothesis, not evidence.
 * Distinct from fundamental attribution (character for anyone) and from halo effect.
 * Coordinator must not average it away.
 */
public final class SelfServingBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private SelfServingBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "self-serving bias: успех себе/навыку, неудачу внешнему без баланса сторон");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта атрибутируют исход в свою пользу без учёта альтернативы");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "self-serving bias (возможный сигнал, требует контекста): " + core;
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
                "self-serving", "самообслуживающ", "self serving bias",
                "атрибуция успеха и неудачи", "баланс причин",
                "и навык и обстоятельства", "both skill and situation",
                "не только навык", "not only skill", "not only luck",
                "внешние и внутренние факторы", "internal and external factors",
                "честная атрибуция", "fair attribution");
    }

    private static boolean selfServingPattern(String t) {
        boolean successInternal = has(t,
                "мы выиграли благодаря навыку", "я добился благодаря", "наш успех — результат компетентности",
                "succeeded because of skill", "won due to talent", "our success proves competence",
                "я смог потому что умный", "мы смогли потому что лучше",
                "это мой заслуженный результат", "заслуженный успех",
                "because we are competent", "because i am skilled");
        boolean failureExternal = has(t,
                "проиграли из-за обстоятельств", "не повезло с ситуацией", "внешние факторы помешали",
                "failed because of bad luck", "lost due to circumstances", "the situation caused the failure",
                "не наша вина, что", "обстоятельства против нас",
                "не повезло", "bad luck explains the loss",
                "рынок виноват", "система виновата");
        boolean otherSuccessExternal = has(t,
                "им просто повезло", "их успех — случайность", "они выиграли по удаче",
                "they just got lucky", "their success is luck", "won by chance not skill",
                "им помогли обстоятельства", "не их заслуга");
        boolean otherFailureInternal = has(t,
                "они проиграли потому что некомпетентны", "их неудача — их характер",
                "they failed because they are incompetent", "their failure shows character",
                "они сами виноваты", "это их вина как людей");

        boolean positiveSelf = successInternal || (has(t, "наш успех", "мой успех", "our win", "my achievement")
                && has(t, "навык", "компетент", "талант", "skill", "talent", "competence"));
        boolean negativeOther = otherFailureInternal;
        boolean negativeSelf = failureExternal;
        boolean positiveOther = otherSuccessExternal;

        return (positiveSelf && !has(t, "но также", "однако обстоятельства", "also situation"))
                || (negativeSelf && !has(t, "но также наш", "also our fault", "часть нашей ответственности"))
                || positiveOther || negativeOther;
    }

    private static boolean ignores(String t) {
        return selfServingPattern(t) && !acknowledges(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
