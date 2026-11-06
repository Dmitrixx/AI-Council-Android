package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local just-world hypothesis / just-world fallacy signals.
 * Assuming that people get what they deserve (outcomes prove moral desert, victim blaming,
 * "everything happens for a reason" as fairness) without acknowledging chance, systemic factors,
 * luck or unequal starting conditions is a hypothesis, not evidence.
 * Distinct from fundamental attribution (character over situation) and self-serving bias.
 * Coordinator must not treat "he deserved it" or "the world is fair so success is earned" as settled.
 */
public final class JustWorldBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private JustWorldBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "just-world bias: вывод, что исход доказывает заслуженность (люди получают по заслугам) без учёта случайности, системных факторов или удачи");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта считают, что плохой исход доказывает вину жертвы или что успех всегда заслужен без калибровки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "just-world (possible signal, requires context): " + core;
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
                "just-world", "гипотеза справедливого мира",
                "случайность", "chance", "luck",
                "системные факторы", "systemic factors",
                "не всегда заслуженно", "not always deserved",
                "удача", "неравные стартовые условия", "unequal starting conditions");
    }

    private static boolean naivePattern(String t) {
        boolean justWorld = has(t,
                "получил по заслугам", "сам виноват", "мир справедлив",
                "всё происходит не зря", "everything happens for a reason",
                "you get what you deserve", "people get what they deserve",
                "он это заслужил", "она сама виновата",
                "успех всегда заслужен", "бедные ленивы",
                "what goes around comes around", "reap what you sow");
        return justWorld && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
