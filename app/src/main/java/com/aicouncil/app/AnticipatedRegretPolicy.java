package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local anticipated-regret signals.
 * Flags a verdict that treats avoiding possible future regret as clearly decisive
 * for preferring a safer, status-quo or "at least I tried" option, without stating
 * the expected value of the alternatives, the probability of the regret-causing
 * outcome, or that regret is an emotional cost to be weighed rather than the sole criterion.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on anticipated regret.
 */
public final class AnticipatedRegretPolicy {
    private static final int MAX_SIGNALS = 2;

    private AnticipatedRegretPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesOnRegret(a) || closesOnRegret(b),
                "anticipated regret: вердикт закрывает выбор только чтобы избежать возможного будущего сожаления — без оценки ожидаемой ценности или вероятности исхода, вызывающего сожаление");
        add(conflicts, initial, closesOnRegret(a) && closesOnRegret(b),
                "оба эксперта закрывают на избегании сожаления без форвард-лукинг ценности");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "anticipated regret (possible signal, requires context): " + core;
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
                "anticipated regret", "ожидаемое сожаление",
                "regret is not a decision criterion", "сожаление — не критерий решения",
                "expected value of regret", "вероятность сожаления",
                "emotional cost to weigh", "эмоциональная стоимость");
    }

    private static boolean closesOnRegret(String t) {
        boolean regretLang = has(t,
                "чтобы не пожалеть", "чтобы не было сожаления", "избежать сожаления",
                "at least I won't regret", "so I won't regret later",
                "better safe than sorry because of regret", "чтобы потом не жалеть");
        boolean decisive = has(t,
                "поэтому выбираем безопасный", "значит лучше не делать",
                "закрываем на избегании сожаления", "therefore we choose the safe option",
                "so we prefer status quo to avoid regret");
        boolean mentionsValue = has(t,
                "ожидаемая ценность", "expected value",
                "вероятность исхода", "probability of the outcome",
                "сожаление как стоимость", "regret as a cost to weigh");
        return regretLang && decisive && !mentionsValue && !acknowledges(t);
    }
}
