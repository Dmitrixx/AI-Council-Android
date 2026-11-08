package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local psychological-reactance signals.
 * Rejecting or opposing a recommendation primarily because it is framed as
 * mandatory, forced or freedom-restricting ("you must", "no choice", "forced to",
 * "нельзя отказать") — without independent evaluation of the substance — is a
 * hypothesis, not a verdict.
 * Distinct from reactive devaluation (source of the idea), status-quo bias
 * (preference for current state), action bias (urge to act) and authority bias
 * (deference to rank): here the trigger is perceived threat to autonomy.
 * Coordinator must not treat "this feels forced" as sufficient reason to discard
 * an otherwise sound option, nor treat hard directive language as proof of merit.
 */
public final class ReactancePolicy {
    private static final int MAX_SIGNALS = 2;

    private ReactancePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "reactance: рекомендация / совет отвергается потому что звучит как принуждение или ограничение свободы, без оценки по существу");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта отвергают вариант из-за ощущения принуждения, а не по качеству аргументов");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "reactance (possible signal, requires context): " + core;
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
                "reactance", "реактивность",
                "психологическая реактивность",
                "ощущение принуждения не аргумент",
                "автономия важна, но содержание важнее",
                "freedom threat is not the merits",
                "coercion feeling is separate from the case",
                "отделяю чувство давления от существа");
    }

    private static boolean naivePattern(String t) {
        boolean rejectBecauseForced = has(t,
                "отвергаю, потому что заставляют",
                "не потому что плохо, а потому что заставляют",
                "раз нет выбора — значит против",
                "не буду, раз принуждают",
                "это звучит как приказ, поэтому нет",
                "не люблю, когда диктуют",
                "отвергаю директивный тон",
                "reject because it feels forced",
                "reject because there is no choice",
                "oppose because it is mandatory",
                "don't like being told what to do",
                "sounds like an order, so no",
                "i resist because they demand",
                "против, раз нельзя отказаться",
                "раз обязали — значит плохо",
                "не хочу, когда ограничивают свободу");
        boolean coerciveFrame = has(t,
                "вы обязаны",
                "нет выбора",
                "единственный допустимый",
                "принудительно",
                "вас заставят",
                "you must",
                "you have no choice",
                "forced to",
                "mandatory with no alternative",
                "non-negotiable",
                "отказать нельзя",
                "обязаны выполнить");
        boolean autonomyReject = has(t,
                "поэтому отказываюсь",
                "поэтому против",
                "поэтому не принимаю",
                "therefore i refuse",
                "so i reject it",
                "so i oppose");
        boolean combo = coerciveFrame && autonomyReject;
        return (rejectBecauseForced || combo) && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
