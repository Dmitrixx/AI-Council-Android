package com.aicouncil.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Local answer-contract check.
 * Flags a closing verdict that does not touch the question that was asked:
 * the easy or neighbouring topic is not an answer. Naming that gap is not
 * itself an error. Signals remain hypotheses requiring context.
 */
public final class AnswerContractPolicy {
    private static final int MAX_SIGNALS = 2;
    private static final int STEM = 5;

    private static final Set<String> STOP = new HashSet<String>();
    static {
        String[] words = {
            "стоит", "нужно", "можно", "будет", "чтобы", "потому", "значит",
            "следует", "поэтому", "вывод", "вопрос", "ответ", "рекомендую",
            "should", "would", "could", "there", "their", "about", "which",
            "therefore", "because", "answer", "question", "recommend"
        };
        for (String w : words) STOP.add(w);
    }

    private AnswerContractPolicy() {}

    public static void detect(String task, String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null || !isQuestion(task)) return;
        int initial = conflicts.size();
        boolean hitA = misses(task, answerA);
        boolean hitB = misses(task, answerB);
        add(conflicts, initial, hitA || hitB,
                "answer contract: verdict closes without addressing the asked question");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают вердикт, не касаясь заданного вопроса");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "answer contract (possible signal, requires context): " + core;
        String key = "answer contract";
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key) && existing.contains(core.substring(0, Math.min(24, core.length())))) {
                return;
            }
        }
        conflicts.add(signal);
    }

    private static boolean isQuestion(String task) {
        String t = lower(task);
        if (t.length() < 24) return false;
        if (t.indexOf('?') >= 0) return true;
        return has(t,
                "стоит ли", "нужно ли", "что выбрать", "какой вариант",
                "почему ", "зачем ", "should ", "whether ", "how should");
    }

    private static boolean misses(String task, String raw) {
        String t = lower(raw);
        if (t.length() < 40 || acknowledges(t) || !closes(t)) return false;
        Set<String> asked = stems(task);
        Set<String> said = stems(raw);
        if (asked.size() < 2 || said.isEmpty()) return false;
        for (String left : asked) {
            for (String right : said) {
                if (left.equals(right)) return false;
            }
        }
        return true;
    }

    private static Set<String> stems(String text) {
        Set<String> out = new HashSet<String>();
        if (text == null) return out;
        StringBuilder token = new StringBuilder();
        String lower = text.toLowerCase(Locale.ROOT);
        for (int i = 0; i <= lower.length(); i++) {
            char c = i < lower.length() ? lower.charAt(i) : ' ';
            if (Character.isLetter(c)) {
                token.append(c);
            } else if (token.length() > 0) {
                if (token.length() >= STEM) {
                    String word = token.toString();
                    if (!STOP.contains(word)) out.add(word.substring(0, STEM));
                }
                token.setLength(0);
            }
        }
        return out;
    }

    private static boolean closes(String t) {
        return has(t,
                "поэтому", "значит", "следовательно", "thus", "therefore",
                "so the answer", "вывод:", "заключение:", "рекомендую",
                "следует", "must be", "clearly", "не стоит", "не рекомендую");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "answer contract",
                "не на заданный вопрос",
                "не отвечаю на вопрос",
                "мимо вопроса",
                "другой вопрос",
                "off the asked question",
                "does not answer the question");
    }

    private static boolean has(String text, String... phrases) {
        if (text == null) return false;
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
