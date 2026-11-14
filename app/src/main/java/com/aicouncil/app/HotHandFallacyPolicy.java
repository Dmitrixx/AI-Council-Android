package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local hot-hand fallacy / streak-as-skill signals.
 * Flags a verdict that treats a recent streak of successes (or failures) in
 * independent or near-independent trials as evidence of elevated (or reduced)
 * probability for the next trial, without noting independence, base rate, or
 * regression to the mean. Naming the gap is not itself an error.
 * The coordinator must not close on the streak alone.
 */
public final class HotHandFallacyPolicy {
    private static final int MAX_SIGNALS = 2;

    private HotHandFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, hotHand(a) || hotHand(b),
                "hot-hand: вердикт закрывает выбор тем, что серия успехов в независимых испытаниях повышает вероятность следующего без оговорки о независимости или регрессии к среднему");
        add(conflicts, initial, bothHotHand(a, b),
                "оба эксперта опираются на серию успехов как на повышенную вероятность без независимой проверки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "hot-hand (possible signal, requires context): " + core;
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
                "hot-hand fallacy", "эффект горячей руки",
                "independence of trials", "независимость испытаний",
                "regression to the mean", "регрессия к среднему",
                "streak does not predict", "серия не предсказывает",
                "random variation", "случайная вариация");
    }

    private static boolean hotHand(String t) {
        boolean streak = has(t,
                "серия успехов", "горячая рука", "на серии",
                "подряд побед", "winning streak", "hot hand",
                "on a roll", "в ударе", "streak of successes",
                "несколько раз подряд");
        boolean predicts = has(t,
                "поэтому следующий тоже успешный",
                "выше шансы на следующий",
                "продолжит побеждать",
                "more likely to succeed next",
                "hot so will continue",
                "therefore next will succeed",
                "серия продолжится");
        return streak && predicts && !acknowledges(t);
    }

    private static boolean bothHotHand(String a, String b) {
        return hotHand(a) && hotHand(b);
    }
}
