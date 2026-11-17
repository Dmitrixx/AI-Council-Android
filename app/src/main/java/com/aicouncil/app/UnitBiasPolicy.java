package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local unit-bias signals.
 * Flags a verdict that treats completing a full unit, round package, whole serving
 * or "one of" as decisive without stating the marginal value of partial amounts,
 * alternative sizes, or that the unit boundary is arbitrary relative to the goal.
 * Naming the gap is not itself an error.
 * The coordinator cannot close on the mere fact of finishing the unit.
 */
public final class UnitBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private UnitBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, unitWithoutMarginal(a) || unitWithoutMarginal(b),
                "unit bias: вердикт закрывает выбор тем, что «надо доесть/доделать единицу» или «круглое число/целая упаковка» без оценки предельной ценности частичной дозы или альтернативного размера");
        add(conflicts, initial, bothUnit(a, b),
                "оба эксперта закрывают на границе единицы без независимого сравнения предельной ценности");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "unit bias (possible signal, requires context): " + core;
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
                "unit bias", "эффект единицы",
                "marginal value", "предельная ценность",
                "partial amount", "частичная доза",
                "unit is arbitrary", "граница единицы произвольна",
                "half would be better", "половина лучше");
    }

    private static boolean unitWithoutMarginal(String t) {
        boolean unit = has(t,
                "доесть до конца", "доделать единицу", "целая упаковка",
                "круглое число", "одно целое", "finish the unit",
                "complete the serving", "whole package", "round number",
                "one full", "до конца порции", "целиком",
                "unit size", "размер единицы");
        boolean decisive = has(t,
                "поэтому выбира", "значит надо", "закрываем",
                "recommend", "therefore we", "so we should",
                "этого достаточно", "потому что единица",
                "because it's one");
        boolean marginal = has(t,
                "предельн", "marginal", "частичн", "partial",
                "половин", "half", "альтернативн размер",
                "alternative size", "не вся единица",
                "меньше единицы");
        return unit && decisive && !marginal && !acknowledges(t);
    }

    private static boolean bothUnit(String a, String b) {
        return unitWithoutMarginal(a) && unitWithoutMarginal(b);
    }
}
