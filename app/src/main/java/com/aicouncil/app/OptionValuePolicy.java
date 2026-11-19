package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local option-value / real-options signals.
 * Flags a verdict that treats committing now to a course of action as clearly superior
 * without stating the value of preserving flexibility, the option to wait for more information,
 * to reverse course, or to stage the decision (real options analysis), when residual uncertainty
 * exists and the cost of irreversibility is material.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on immediate commitment when keeping options open has value.
 * Distinct from pure reversibility (one-way doors) and pure VoI: this focuses on the positive value
 * of flexibility under uncertainty.
 */
public final class OptionValuePolicy {
    private static final int MAX_SIGNALS = 2;

    private OptionValuePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesWithoutOptionValue(a) || closesWithoutOptionValue(b),
                "option value: вердикт закрывает на немедленном коммитменте без оценки ценности сохранения гибкости (опцион подождать, поэтапно решать, оставить путь назад) при остаточной неопределённости");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на коммитменте сейчас, не назвав ценность опциона оставить решение открытым");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "option value (possible signal, requires context): " + core;
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
                "option value", "ценность опциона", "real options",
                "сохранить гибкость", "keep options open",
                "поэтапное решение", "staged decision",
                "можно подождать", "value of waiting",
                "flexibility has value", "гибкость имеет ценность");
    }

    private static boolean closesWithoutOptionValue(String t) {
        boolean commitmentLang = has(t,
                "надо решать сейчас", "commit now", "зафиксировать решение",
                "нет смысла откладывать", "must decide immediately",
                "выбираем один путь сейчас", "lock in now");
        boolean decisive = has(t,
                "поэтому делаем сейчас", "therefore commit",
                "закрываем на коммитменте", "so we proceed with full commitment",
                "нет причин ждать", "no reason to keep options open");
        boolean mentionsOption = has(t,
                "ценность гибкости", "option value", "real option",
                "можно оставить открытым", "value of flexibility",
                "поэтапно", "staged approach");
        return commitmentLang && decisive && !mentionsOption && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesWithoutOptionValue(a) && closesWithoutOptionValue(b);
    }
}
