package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local relative privation / "not as bad as" policy.
 * Detects conclusions that a problem, harm or claim is acceptable, minor or may be ignored
 * merely because a worse problem exists ("X is not so bad because Y is worse",
 * "at least it is not as bad as...", "there are bigger problems").
 * Naming the error, or saying a worse problem does not cancel this one, is not the error.
 * Signals remain hypotheses that require context.
 */
public final class RelativePrivationPolicy {
    private static final int MAX_SIGNALS = 2;

    private RelativePrivationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "relative privation: проблема объявляется приемлемой лишь потому, что есть хуже");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта отмахиваются от проблемы ссылкой на более тяжёлую");
        add(conflicts, initial, hitA ^ hitB,
                "relative privation: один эксперт снимает вопрос через «есть хуже»");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "relative privation (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "не так плохо, потому что есть хуже",
                "есть проблемы похуже",
                "есть и хуже",
                "не самое плохое",
                "по крайней мере не так плохо как",
                "at least it is not as bad as",
                "not as bad as",
                "there are worse problems",
                "there are bigger problems",
                "relative privation",
                "fallacy of relative privation",
                "because worse things exist this is fine",
                "worse things happen so this is acceptable",
                "не так страшно, ведь бывает хуже",
                "есть вещи похуже");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "relative privation",
                "fallacy of relative privation",
                "хуже не отменяет",
                "a worse problem does not cancel this one",
                "not as bad does not mean acceptable",
                "наличие худшего не оправдывает",
                "worse does not make this ok",
                "есть хуже — не аргумент");
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
