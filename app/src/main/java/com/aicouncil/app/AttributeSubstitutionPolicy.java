package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local attribute-substitution / WYSIATI check.
 * Flags a verdict that answers a hard target question by substituting an easier
 * attribute (fluency, familiarity, vividness, recent exposure, simple count)
 * or treats the currently available information as complete without noting
 * missing data, unknown unknowns, or the need for further search.
 * Naming the gap is not itself an error.
 * Signals remain hypotheses requiring context.
 */
public final class AttributeSubstitutionPolicy {
    private static final int MAX_SIGNALS = 2;

    private AttributeSubstitutionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "attribute substitution / WYSIATI: hard question answered by easier attribute or available info treated as complete");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают сложный вопрос подменой на лёгкий атрибут или «всё, что видно»");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "attribute substitution / WYSIATI (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(48, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || hasMitigation(t)) return false;
        return (hasSubstitution(t) || hasWysiati(t)) && closes(t);
    }

    private static boolean hasSubstitution(String t) {
        return has(t,
                "проще спросить",
                "легче ответить",
                "вместо этого посмотрим",
                "достаточно знать",
                "достаточно того, что",
                "obvious from how it feels",
                "easy to see that",
                "just look at how many",
                "simply because it feels",
                "because it is familiar",
                "потому что знакомо",
                "потому что легко представить",
                "потому что ярко",
                "потому что недавно видел",
                "because it is vivid",
                "because I can easily imagine");
    }

    private static boolean hasWysiati(String t) {
        return has(t,
                "всё, что нужно",
                "больше ничего не требуется",
                "этого достаточно",
                "все данные уже есть",
                "nothing else is needed",
                "this is all we need",
                "no further information required",
                "what we have is enough",
                "available evidence is complete",
                "больше искать нечего",
                "искать дальше бессмысленно");
    }

    private static boolean closes(String t) {
        return has(t,
                "поэтому",
                "значит",
                "следовательно",
                "thus",
                "therefore",
                "so the answer is",
                "вывод:",
                "заключение:",
                "рекомендую",
                "следует",
                "must be",
                "clearly");
    }

    private static boolean hasMitigation(String t) {
        return has(t,
                "недостаточно данных",
                "нужно больше информации",
                "unknown unknowns",
                "missing information",
                "what we do not see",
                "need further search",
                "attribute substitution",
                "substituting an easier",
                "WYSIATI",
                "what you see is all there is",
                "подмена атрибута",
                "лёгкий вопрос вместо сложного");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "attribute substitution",
                "подмена атрибута",
                "WYSIATI",
                "what you see is all there is",
                "отвечаю на более лёгкий вопрос");
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
