package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local halo-effect signals.
 * Flags when a conclusion about one attribute (competence, honesty, quality) is driven by an unrelated positive or negative trait
 * (appearance, brand, first impression, status) without separate evidence.
 * Hypotheses requiring context, not automatic verdicts. The coordinator must not treat them as definitive.
 */
public final class HaloEffectPolicy {
    private static final int MAX_SIGNALS = 2;

    private HaloEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, hasHalo(a) || hasHalo(b),
                "эффект ореола: оценка одного качества (компетентность, надёжность) опирается на несвязанный позитивный/негативный признак без отдельных доказательств");
        add(conflicts, initial, bothHaveHalo(a, b),
                "оба ответа используют несвязанный признак (внешность, бренд, статус) как доказательство другого качества");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "halo effect (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (affirmed(text, word)) return true;
        return false;
    }

    private static boolean affirmed(String text, String phrase) {
        int from = 0;
        while (from < text.length()) {
            int i = text.indexOf(phrase, from);
            if (i < 0) return false;
            int start = Math.max(0, i - 4);
            String prefix = text.substring(start, i);
            if (!prefix.endsWith("не ") && !prefix.endsWith("not ")) return true;
            from = i + Math.max(1, phrase.length());
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean hasHalo(String t) {
        boolean trait = has(t, "красивый", "привлекательный", "дорогий бренд", "известный", "статус", "хорошо выглядит", "attractive", "good looking", "premium brand", "famous");
        boolean judgment = has(t, "компетентный", "надёжный", "качественный", "честный", "competent", "reliable", "high quality", "trustworthy");
        boolean separate = has(t, "отдельные доказательства", "независимо", "separate evidence", "regardless of");
        return trait && judgment && !separate;
    }

    private static boolean bothHaveHalo(String a, String b) {
        return hasHalo(a) && hasHalo(b);
    }
}
