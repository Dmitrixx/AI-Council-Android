package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local whataboutism / tu quoque signals.
 * Deflecting criticism of a claim or action by pointing to a similar (or unrelated) fault of the critic or a third party
 * ("but what about X?", "you do the same", "others do worse", "а что насчёт…") instead of addressing the substance is a hypothesis, not evidence.
 * Distinct from ad hominem (personal attack on character), reactive devaluation, and straw man.
 * Coordinator must not treat deflection as sufficient to dismiss an argument, nor treat the absence of deflection as proof the argument is sound.
 */
public final class WhataboutismPolicy {
    private static final int MAX_SIGNALS = 2;

    private WhataboutismPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "whataboutism: уход от разбора сути через «а что насчёт…» / «вы сами так делаете» вместо ответа на претензию");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта уходят от сути через whataboutism, а не разбирают исходный аргумент");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "whataboutism (possible signal, requires context): " + core;
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
                "whataboutism",
                "ту квокве",
                "уход от темы",
                "это не отвечает на претензию",
                "deflection is not a rebuttal",
                "addressing the original claim");
    }

    private static boolean naivePattern(String t) {
        boolean deflection = has(t,
                "а что насчёт",
                "а как же",
                "а вы сами",
                "вы тоже так делаете",
                "другие делают хуже",
                "what about",
                "you do the same",
                "others are worse",
                "but you also",
                "tu quoque",
                "это не отвечает на вопрос, а вот");
        boolean noSubstance = !has(t,
                "разберём исходную претензию",
                "независимо от других случаев",
                "address the original point",
                "the original claim stands or falls on",
                "отвечаю по существу");
        return deflection && noSubstance && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
