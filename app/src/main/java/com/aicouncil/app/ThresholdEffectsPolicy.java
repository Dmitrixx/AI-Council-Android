package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local threshold / tipping-point / phase-transition signals.
 * Flags a verdict that treats effects as continuous and approximately linear
 * across a material threshold (tipping point, regulatory cliff, network critical
 * mass, phase change, discontinuous regime shift) without locating the threshold
 * or stating the discontinuity. Naming the gap is not itself an error.
 * The coordinator cannot close solely on smooth extrapolation when the decision
 * may cross a non-linear boundary.
 * Distinct from interaction-effects (joint effect of several concurrent levers)
 * and from pure marginal analysis (intensive margin under continuous diminishing
 * returns of one lever): this focuses on a qualitative change at a critical level.
 */
public final class ThresholdEffectsPolicy {
    private static final int MAX_SIGNALS = 2;

    private ThresholdEffectsPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesAcrossThresholdWithoutNaming(a) || closesAcrossThresholdWithoutNaming(b),
                "threshold effects: вердикт закрывает на линейной/плавной экстраполяции через порог (tipping point, cliff, critical mass) без указания положения порога и характера разрыва");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на непрерывном эффекте, не назвав порог, фазовый переход или точку качественного изменения");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "threshold effects (possible signal, requires context): " + core;
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
                "threshold", "порог", "tipping point", "точка перелома",
                "critical mass", "критическая масса", "phase transition", "фазовый переход",
                "cliff", "обрыв", "discontinuity", "разрыв", "regime shift",
                "качественный переход", "нелинейный порог", "beyond the threshold",
                "после порога", "до порога эффект другой");
    }

    private static boolean closesAcrossThresholdWithoutNaming(String t) {
        boolean thresholdContext = has(t,
                "по мере роста", "as scale increases", "с увеличением",
                "больше — лучше", "more is better", "линейно растёт",
                "пропорционально масштабу", "scales linearly",
                "критическая масса", "network effect", "сетевой эффект",
                "регуляторный порог", "regulatory threshold",
                "переход через", "crossing into", "после определённого уровня");
        boolean closing = has(t,
                "поэтому делаем больше", "therefore scale up",
                "закрываем на увеличении", "so we increase",
                "полный переход лучше", "full transition is best",
                "нет качественного скачка", "no qualitative change");
        boolean mentionsThreshold = has(t,
                "порог", "threshold", "tipping", "critical mass",
                "фазовый", "cliff", "разрыв", "discontinuity");
        return thresholdContext && closing && !mentionsThreshold && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesAcrossThresholdWithoutNaming(a) && closesAcrossThresholdWithoutNaming(b);
    }
}
