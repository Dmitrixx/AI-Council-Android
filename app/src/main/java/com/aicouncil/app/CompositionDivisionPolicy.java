package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local composition / division fallacy policy.
 * Detects attributing a property of the parts to the whole (composition)
 * or a property of the whole to the parts (division) without independent justification.
 * Naming the error is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class CompositionDivisionPolicy {
    private static final int MAX_SIGNALS = 2;

    private CompositionDivisionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "composition/division: свойство частей (или целого) перенесено на целое (или части) без основания");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта переносят свойства между частями и целым");
        add(conflicts, initial, hitA ^ hitB,
                "composition/division: один эксперт делает вывод о целом из свойств частей (или наоборот)");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "composition/division (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "каждая часть хороша, значит целое хорошо",
                "все члены команды сильны, значит команда сильна",
                "атом не живой, значит молекула не живая",
                "the parts are efficient so the whole is efficient",
                "each member is honest so the organization is honest",
                "the average is high so every individual is high",
                "because the system works the components must work",
                "composition fallacy",
                "division fallacy",
                "what is true of the parts is true of the whole",
                "what is true of the whole is true of the parts");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "composition fallacy",
                "division fallacy",
                "свойства частей не всегда свойства целого",
                "нельзя переносить свойства с целого на части",
                "what holds for parts need not hold for the whole",
                "the whole can have properties the parts lack",
                "не выводи свойства целого только из свойств частей",
                "emergence means the whole is not just the sum");
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
