package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local curse-of-knowledge policy.
 * Flags a verdict that treats private expertise, jargon, or "it is obvious / everyone knows"
 * as sufficient shared ground, without making the key premise, definition, or evidence explicit.
 * Naming the error is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class CurseOfKnowledgePolicy {
    private static final int MAX_SIGNALS = 2;

    private CurseOfKnowledgePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "curse of knowledge: \u00ab\u043e\u0447\u0435\u0432\u0438\u0434\u043d\u043e / \u0432\u0441\u0435 \u0437\u043d\u0430\u044e\u0442\u00bb \u043f\u0440\u0438\u043d\u0438\u043c\u0430\u0435\u0442\u0441\u044f \u0437\u0430 \u043e\u0431\u0449\u0435\u0435 \u043e\u0441\u043d\u043e\u0432\u0430\u043d\u0438\u0435 \u0431\u0435\u0437 \u044f\u0432\u043d\u043e\u0439 \u043f\u043e\u0441\u044b\u043b\u043a\u0438 \u0438\u043b\u0438 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d\u0438\u044f");
        add(conflicts, initial, hitA && hitB,
                "\u043e\u0431\u0430 \u044d\u043a\u0441\u043f\u0435\u0440\u0442\u0430 \u043e\u043f\u0438\u0440\u0430\u044e\u0442\u0441\u044f \u043d\u0430 \u043d\u0435\u044f\u0432\u043d\u043e\u0435 \u0437\u043d\u0430\u043d\u0438\u0435 \u043a\u0430\u043a \u043d\u0430 \u0434\u043e\u0441\u0442\u0430\u0442\u043e\u0447\u043d\u043e\u0435 \u043e\u0441\u043d\u043e\u0432\u0430\u043d\u0438\u0435");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "curse of knowledge (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "\u043e\u0447\u0435\u0432\u0438\u0434\u043d\u043e \u0447\u0442\u043e",
                "\u043e\u0447\u0435\u0432\u0438\u0434\u043d\u043e,",
                "\u0432\u0441\u0435\u043c \u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e",
                "\u0432\u0441\u0435 \u0437\u043d\u0430\u044e\u0442",
                "\u043a\u0430\u043a \u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e",
                "\u043d\u0435 \u043d\u0443\u0436\u043d\u043e \u043e\u0431\u044a\u044f\u0441\u043d\u044f\u0442\u044c",
                "\u0438 \u0442\u0430\u043a \u043f\u043e\u043d\u044f\u0442\u043d\u043e",
                "\u0441\u0430\u043c\u043e \u0441\u043e\u0431\u043e\u0439 \u0440\u0430\u0437\u0443\u043c\u0435\u0435\u0442\u0441\u044f",
                "\u044d\u0442\u043e \u0431\u0430\u0437\u043e\u0432\u044b\u0435 \u0437\u043d\u0430\u043d\u0438\u044f",
                "anyone knows",
                "everyone knows",
                "it is obvious that",
                "it is obvious,",
                "obviously we should",
                "as is well known",
                "needless to say",
                "no need to explain",
                "self-evident that",
                "trivially true that");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "curse of knowledge",
                "\u043f\u0440\u043e\u043a\u043b\u044f\u0442\u0438\u0435 \u0437\u043d\u0430\u043d\u0438\u044f",
                "\u043d\u0435\u044f\u0432\u043d\u043e\u0435 \u0437\u043d\u0430\u043d\u0438\u0435 \u043d\u0435 \u043e\u0441\u043d\u043e\u0432\u0430\u043d\u0438\u0435",
                "implicit knowledge is not shared ground",
                "make the premise explicit",
                "\u0441\u0434\u0435\u043b\u0430\u0439 \u043f\u043e\u0441\u044b\u043b\u043a\u0443 \u044f\u0432\u043d\u043e\u0439",
                "\u043d\u0435 \u0441\u0447\u0438\u0442\u0430\u044e \u044d\u0442\u043e \u043e\u0447\u0435\u0432\u0438\u0434\u043d\u044b\u043c");
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
