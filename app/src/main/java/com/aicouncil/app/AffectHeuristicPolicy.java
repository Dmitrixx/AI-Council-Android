package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local affect-heuristic signals.
 * Liking, disgust or a "good feeling" is not a risk estimate and not a benefit.
 * Hypotheses, not verdicts.
 */
public final class AffectHeuristicPolicy {
    private static final int MAX_SIGNALS = 2;

    private AffectHeuristicPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, affectAsProof(a) || affectAsProof(b),
                "симпатия или отвращение заменяют оценку риска и пользы");
        add(conflicts, initial, bothAffectOnly(a, b),
                "оба эксперта закрывают вывод вкусом, а не риском и пользой");
        add(conflicts, initial, fork(a, b),
                "развилка: вердикт из симпатии против отдельной оценки риска");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "affect heuristic (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(36, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean affectAsProof(String text) {
        return hasAffectVerdict(text) && !hasSeparation(text);
    }

    private static boolean bothAffectOnly(String a, String b) {
        return affectAsProof(a) && affectAsProof(b);
    }

    private static boolean fork(String a, String b) {
        return (affectAsProof(a) && hasSeparation(b))
                || (affectAsProof(b) && hasSeparation(a));
    }

    private static boolean hasAffectVerdict(String text) {
        return contains(text,
                "нравится, значит риск", "раз нравится, риск",
                "нравится — значит выгодно", "нравится, значит выгодно",
                "противный, значит бесполезный", "отвращение доказывает",
                "бесит, значит провалится", "хорошее чувство доказывает",
                "мне нравится проект, риски", "раз противно, пользы нет",
                "i like it so the risk", "love it so the downside",
                "feels wrong so it won't", "disgusting so the benefits",
                "feels good so the risk", "i hate it so it will fail");
    }

    private static boolean hasSeparation(String text) {
        return contains(text,
                "симпатия не доказательство", "отделим вкус от риска",
                "независимо от того, нравится", "вкус не оценка риска",
                "affect is not evidence", "liking is not a risk",
                "disgust is not a benefit", "separate taste from risk");
    }

    private static boolean contains(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
