package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local hyperbolic-discounting signals.
 * Flags a verdict that treats a smaller immediate reward or lower immediate cost
 * as decisively better than a larger delayed alternative solely because it is nearer
 * in time, without stating a consistent discount rate, total undiscounted value,
 * or explicit awareness that near-term preference can be time-inconsistent.
 * Naming the gap is not itself an error.
 * The coordinator cannot close on proximity-in-time alone.
 */
public final class HyperbolicDiscountingPolicy {
    private static final int MAX_SIGNALS = 2;

    private HyperbolicDiscountingPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, nearTermDominates(a) || nearTermDominates(b),
                "hyperbolic discounting: вердикт закрывает выбор тем, что ближний меньший выигрыш или меньшая немедленная цена лучше дальнего большего только из-за близости во времени, без согласованной ставки дисконтирования или полной ценности");
        add(conflicts, initial, bothDiscount(a, b),
                "оба эксперта закрывают на близости во времени без независимой оценки полной ценности или ставки дисконтирования");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "hyperbolic discounting (possible signal, requires context): " + core;
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
                "hyperbolic discount", "гиперболическое дисконтирование",
                "time-inconsistent", "несогласованное во времени",
                "discount rate", "ставка дисконтирования",
                "total value", "полная ценность", "недисконтированная",
                "even with present bias", "даже с present bias");
    }

    private static boolean nearTermDominates(String t) {
        boolean nearSmaller = has(t,
                "лучше взять сейчас даже если меньше", "сейчас лучше даже если меньше",
                "немедленный меньший лучше", "ближний выигрыш важнее дальнего",
                "откладывать не стоит даже если больше", "будущее слишком далеко",
                "immediate smaller is better", "take the smaller now",
                "near-term reward dominates", "future is discounted too heavily",
                "now is worth more despite lower total", "proximity in time decides");
        boolean decisive = has(t,
                "поэтому выбира", "значит надо", "закрываем", "recommend",
                "therefore we", "so we take", "этого достаточно", "решает близость");
        boolean considersFull = has(t,
                "полная ценность", "total value", "undiscounted",
                "ставка дисконтирования", "discount rate",
                "гиперболическ", "hyperbolic",
                "несогласованн", "time-inconsistent");
        return nearSmaller && decisive && !considersFull && !acknowledges(t);
    }

    private static boolean bothDiscount(String a, String b) {
        return nearTermDominates(a) && nearTermDominates(b);
    }
}
