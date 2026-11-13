package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local availability-cascade / information-cascade policy.
 * Flags a verdict that treats the fact that many models, sources or people
 * now say the same thing (or that the claim is "everywhere") as sufficient
 * independent evidence, without an explicit check of source independence,
 * base rate, or original evidence.
 * Naming that error is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class AvailabilityCascadePolicy {
    private static final int MAX_SIGNALS = 2;

    private AvailabilityCascadePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "availability cascade: widespread repetition taken as independent confirmation");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают выбор тем, что «все так говорят» без проверки независимости источников");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "availability cascade (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || hasIndependenceCheck(t)) return false;
        return hasCascadeMarker(t) && closes(t);
    }

    private static boolean hasCascadeMarker(String t) {
        return has(t,
                "все так говорят",
                "все согласны",
                "везде пишут",
                "везде говорят",
                "многие источники",
                "много моделей",
                "все модели",
                "консенсус моделей",
                "everyone says",
                "everybody agrees",
                "widely reported",
                "circulating widely",
                "many sources confirm",
                "all models agree",
                "multiple models say",
                "information cascade",
                "availability cascade");
    }

    private static boolean closes(String t) {
        return has(t,
                "поэтому",
                "значит",
                "рекомендую",
                "вердикт",
                "вопрос закрыт",
                "выбираем",
                "следует выбрать",
                "therefore",
                "we should",
                "the choice is",
                "so we",
                "enough to decide",
                "достаточно, чтобы");
    }

    private static boolean hasIndependenceCheck(String t) {
        return has(t,
                "независимость источников",
                "проверил независимость",
                "оригинальные наблюдения",
                "базовая частота",
                "source independence",
                "independent observations",
                "not independent",
                "cascade does not confirm",
                "repetition is not evidence",
                "повторение не доказательство");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "availability cascade",
                "information cascade",
                "каскад доступности",
                "информационный каскад",
                "повторение не делает истинным",
                "agreement among models is not independent");
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
