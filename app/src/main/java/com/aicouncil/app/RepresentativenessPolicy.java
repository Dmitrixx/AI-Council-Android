package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local representativeness heuristic signals.
 * Flags when probability or category judgments appear driven mainly by similarity to a stereotype,
 * prototype, or vivid description without explicit base-rate, reference-class, or frequency correction.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class RepresentativenessPolicy {
    private static final int MAX_SIGNALS = 3;

    private RepresentativenessPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, stereotypeWithoutBaseRate(a) || stereotypeWithoutBaseRate(b),
                "эвристика репрезентативности: суждение опирается на стереотип/сходство с прототипом без base rate или reference class");
        add(conflicts, initial, bothUseSimilarStereotype(a, b),
                "общий стереотип: оба эксперта опираются на похожие описания «типичного случая» без систематических данных");
        add(conflicts, initial, ignoresBaseRateForSimilarity(a) || ignoresBaseRateForSimilarity(b),
                "игнорирование base rate: «похоже на X — значит X» без учёта частоты класса");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "эвристика репрезентативности (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean stereotypeWithoutBaseRate(String t) {
        boolean stereotype = has(t, "типичный", "стереотип", "похож на", "классический пример",
                "такие обычно", "прототип", "typical case", "stereotype", "looks like",
                "похоже на типичного", "все такие");
        boolean base = has(t, "base rate", "базовая частота", "reference class", "статистика",
                "в среднем", "по данным", "частота класса", "prior probability");
        return stereotype && !base;
    }

    private static boolean bothUseSimilarStereotype(String a, String b) {
        boolean aStereo = has(a, "типичный", "стереотип", "похож на", "typical");
        boolean bStereo = has(b, "типичный", "стереотип", "похож на", "typical");
        return aStereo && bStereo;
    }

    private static boolean ignoresBaseRateForSimilarity(String t) {
        boolean similarity = has(t, "похоже на", "схож с", "looks like", "resembles", "похож на типичного");
        boolean conclusion = has(t, "значит", "следовательно", "therefore", "это и есть");
        boolean base = has(t, "base rate", "базовая частота", "reference class", "статистика", "prior");
        return similarity && conclusion && !base;
    }
}
