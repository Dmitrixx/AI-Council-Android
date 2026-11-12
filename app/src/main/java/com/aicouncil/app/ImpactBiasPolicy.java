package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local impact bias / affective forecasting error policy.
 * Flags a verdict that treats a predicted strong emotional reaction (joy, regret, fear of missing out)
 * as sufficient reason to choose or avoid an option, without independent justification that the feeling
 * will actually occur at that intensity or duration. Naming the error is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class ImpactBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private ImpactBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "impact bias: предсказанная сильная эмоция закрыла выбор без независимой проверки интенсивности и длительности");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта опираются на ожидаемое сильное чувство как на достаточное основание");
        add(conflicts, initial, hitA ^ hitB,
                "impact bias: один эксперт закрыл вывод предсказанной эмоцией");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "impact bias (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "будет очень жаль поэтому не стоит",
                "сильно пожалею если не сделаю поэтому делаем",
                "предвкушаю радость поэтому выбираем",
                "страх упустить заставляет взять",
                "буду счастлив от этого следовательно верно",
                "the regret would be unbearable so we must",
                "I will feel terrible if we don't so do it",
                "anticipating great joy therefore this is the choice",
                "fear of missing out decides the outcome",
                "predicted strong emotion is enough to close");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "impact bias",
                "affective forecasting",
                "предсказанная эмоция не равна реальной",
                "интенсивность чувства обычно переоценивается",
                "длительность эмоции короче чем кажется",
                "predicted feelings are often overestimated",
                "impact bias means we overpredict emotional intensity and duration",
                "do not close on anticipated regret or joy alone");
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
