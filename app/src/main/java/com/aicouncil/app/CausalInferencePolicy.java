package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local causal-inference and correlation-causation signals.
 * Flags causal language (causes, leads to, results in) used without explicit
 * justification distinguishing it from mere correlation, association, or confounding.
 * Hypotheses requiring context, not automatic verdicts.
 */
public final class CausalInferencePolicy {
    private static final int MAX_SIGNALS = 5;

    private CausalInferencePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, causalWithoutJustification(a) || causalWithoutJustification(b),
                "причинность без обоснования: используется каузальный язык без отделения от корреляции или конфаундеров");
        add(conflicts, initial, correlationTreatedAsCausal(a) || correlationTreatedAsCausal(b),
                "корреляция как причина: ассоциация представлена как прямое причинно-следственное отношение");
        add(conflicts, initial, bothNoCausalCheck(a, b),
                "оба без проверки причинности: ни один не отметил необходимость различать корреляцию и каузацию");
        add(conflicts, initial, reverseCausationIgnored(a, b),
                "обратная причинность не рассмотрена: направление связи принято без альтернатив");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "causal inference (possible signal, requires context): " + core;
        for (String existing : conflicts) {
            if (existing.contains(core.substring(0, Math.min(40, core.length())))) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static boolean multi(String text, String[] groupA, String[] groupB) {
        return has(text, groupA) && has(text, groupB);
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean causalWithoutJustification(String t) {
        boolean causal = has(t, "вызывает", "приводит к", "причина", "causes", "leads to", "results in", "because of", "из-за", "спричиняє", "призводить до");
        boolean caveat = has(t, "корреляц", "ассоциац", "correlation", "association", "confound", "конфаунд", "может быть связано", "not necessarily causal");
        return causal && !caveat && t.length() > 100;
    }

    private static boolean correlationTreatedAsCausal(String t) {
        return multi(t,
                new String[]{"коррелирует", "связано с", "associated with", "correlated", "correlation"},
                new String[]{"значит", "следовательно", "therefore", "causes", "приводит", "вызывает"});
    }

    private static boolean bothNoCausalCheck(String a, String b) {
        return causalWithoutJustification(a) && causalWithoutJustification(b);
    }

    private static boolean reverseCausationIgnored(String a, String b) {
        boolean aCausal = has(a, "вызывает", "causes", "приводит к");
        boolean bCausal = has(b, "вызывает", "causes", "приводит к");
        boolean reverse = has(a, "обратная причинность", "reverse causation", "может быть наоборот") || has(b, "обратная причинность", "reverse causation");
        return (aCausal || bCausal) && !reverse;
    }
}
