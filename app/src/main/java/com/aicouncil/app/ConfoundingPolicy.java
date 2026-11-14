package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local confounding check.
 * Flags a verdict that treats an association or co-movement as the cause
 * without naming a confounder check, a control, or an explicit
 * "confounding unchecked". Naming the gap is not itself an error.
 * Distinct from post hoc (mere sequence) and single-cause (one factor of many).
 * The coordinator must not close on correlation alone.
 */
public final class ConfoundingPolicy {
    private static final int MAX_SIGNALS = 2;

    private ConfoundingPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "confounding: association treated as cause without a named confounder");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают причину на совместном движении без контроля смешения");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "confounding (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || hasControl(t)) return false;
        return hasAssociation(t) && closesOnCause(t);
    }

    private static boolean hasAssociation(String t) {
        return has(t,
                "корреляц",
                "коррелир",
                "связаны между собой",
                "совместно меняются",
                "движутся вместе",
                "correlation",
                "they are associated",
                "move together",
                "co-movement");
    }

    private static boolean closesOnCause(String t) {
        return has(t,
                "значит причина",
                "доказывает причину",
                "это и есть причина",
                "одно вызывает",
                "therefore causes",
                "proves causation",
                "so it causes",
                "this is the cause");
    }

    private static boolean hasControl(String t) {
        return has(t,
                "мешающий фактор",
                "конфаунд",
                "после контроля",
                "контролируем смешение",
                "controlled for",
                "after adjustment",
                "association is not causation",
                "корреляция не причина",
                "смешение не проверено",
                "unmeasured confounder");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "confounding",
                "смешение факторов",
                "конфаундинг");
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
