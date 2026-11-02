package com.aicouncil.app;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local anchoring bias signals.
 * Flags when a numerical estimate appears heavily influenced by an initial arbitrary number
 * without sufficient independent justification, base-rate adjustment, or sensitivity to the anchor.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class AnchoringPolicy {
    private static final int MAX_SIGNALS = 3;
    private static final Pattern NUMBER = Pattern.compile("(\\d+(?:[.,]\\d+)?)");

    private AnchoringPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, anchoredWithoutAdjustment(a) || anchoredWithoutAdjustment(b),
                "якорение: числовая оценка опирается на начальное или произвольное значение без явной коррекции на base rate или независимые данные");
        add(conflicts, initial, bothShareAnchor(a, b),
                "общий якорь: оба эксперта используют близкие числа без обоснования независимости от начального значения");
        add(conflicts, initial, missingAnchorSensitivity(a) || missingAnchorSensitivity(b),
                "нет чувствительности к якорю: оценка дана, но не сказано, как она изменится при другом начальном числе");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "якорение (возможный сигнал, требует контекста): " + core;
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

    private static boolean anchoredWithoutAdjustment(String t) {
        boolean hasNumber = NUMBER.matcher(t).find();
        boolean baseRateMentioned = has(t, "base rate", "базовая частота", "базовой частоты", "базовую частоту");
        boolean baseRateDenied = has(t, "без базовой частоты", "без учёта базовой частоты",
                "без учета базовой частоты", "не учитывает базовую частоту", "without base rate",
                "base rate was not considered");
        boolean adjustment = (baseRateMentioned && !baseRateDenied)
                || has(t, "reference class", "независимо от", "скорректировал", "adjusted from",
                "не опираюсь на", "якорь не влияет", "учитывая базовую частоту");
        boolean arbitrary = has(t, "примерно", "около", "порядка", "guess", "примерно столько",
                "взял за основу", "начальное значение");
        return hasNumber && arbitrary && !adjustment;
    }

    private static boolean bothShareAnchor(String a, String b) {
        // Agreement on an ordinary result is not evidence of anchoring.
        // Require an explicit reference to an initial/arbitrary anchor before comparing values.
        String combined = a + " " + b;
        boolean explicitAnchor = has(combined, "якор", "anchor", "начальное значение",
                "начальная оценка", "первое названное число", "initial value",
                "starting point", "first number");
        if (!explicitAnchor || has(combined, "независимо", "разные методы", "independent",
                "base rate", "базовая частота", "reference class")) return false;
        Matcher ma = NUMBER.matcher(a);
        Matcher mb = NUMBER.matcher(b);
        if (!ma.find() || !mb.find()) return false;
        try {
            double na = Double.parseDouble(ma.group(1).replace(',', '.'));
            double nb = Double.parseDouble(mb.group(1).replace(',', '.'));
            double denominator = Math.max(Math.abs(na), Math.abs(nb));
            return denominator > 0 && Math.abs(na - nb) / denominator < 0.15;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean missingAnchorSensitivity(String t) {
        boolean estimate = has(t, "оценка", "estimate", "вероятность", "шанс", "прогноз");
        boolean anchorContext = has(t, "якор", "anchor", "начальное число", "initial number", "первое число");
        boolean sensitivity = has(t, "если бы было", "при другом якоре", "чувствительность к",
                "чувствительности к", "анализ чувствительности", "anchor sensitivity", "sensitivity analysis");
        return estimate && anchorContext && !sensitivity && NUMBER.matcher(t).find();
    }
}
