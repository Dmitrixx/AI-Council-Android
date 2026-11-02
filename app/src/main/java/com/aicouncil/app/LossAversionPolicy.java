package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local loss-aversion signals (prospect theory).
 * Flags when a comparable loss is treated as decisive without a stated
 * loss-aversion coefficient, an expected-value comparison, or a ruin constraint.
 * Hypotheses requiring context, not verdicts. The coordinator must not average them.
 */
public final class LossAversionPolicy {
    private static final int MAX_SIGNALS = 3;

    private LossAversionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, rejectsBecauseLossLooms(a) || rejectsBecauseLossLooms(b),
                "неприятие потерь: отказ от сопоставимой ставки, потому что потеря «страшнее» выигрыша, без коэффициента полезности или предела разорения");
        add(conflicts, initial, bothPreferSureThing(a, b),
                "оба выбирают гарантированный малый выигрыш и не сравнивают его с матожиданием рискованного варианта");
        add(conflicts, initial, weighsLossWithoutCoefficient(a) || weighsLossWithoutCoefficient(b),
                "потери названы тяжелее выгод, но коэффициент неприятия потерь и ожидаемая ценность не посчитаны");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "loss aversion (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (affirmed(text, word)) return true;
        return false;
    }

    /** True when phrase occurs and is not immediately preceded by «не ». */
    private static boolean affirmed(String text, String phrase) {
        int from = 0;
        while (from < text.length()) {
            int i = text.indexOf(phrase, from);
            if (i < 0) return false;
            int start = Math.max(0, i - 4);
            String prefix = text.substring(start, i);
            if (!prefix.endsWith("не ") && !prefix.endsWith("not ")) return true;
            from = i + Math.max(1, phrase.length());
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean rejectsBecauseLossLooms(String t) {
        boolean loom = has(t, "потеря страшнее", "потерять страшнее", "losses loom", "loss aversion",
                "неприятие потерь", "боль от потери", "проигрыш больнее", "терять больнее");
        boolean reject = has(t, "отказыва", "не берём", "не берем", "лучше не рисковать",
                "не стоит рисковать", "decline the bet", "reject the bet");
        boolean justified = has(t, "коэффициент", "lambda", "келли", "kelly", "доля банка",
                "критерий выживания", "разорен", "эргодич", "матожидан", "expected value",
                "ожидаемая ценность");
        return loom && reject && !justified;
    }

    private static boolean bothPreferSureThing(String a, String b) {
        return prefersSure(a) && prefersSure(b)
                && !has(a + " " + b, "матожидан", "expected value", "ожидаемая ценность", "ожидаемая доходность");
    }

    private static boolean prefersSure(String t) {
        boolean sure = has(t, "гарантированн", "синица в руках", "sure thing", "верный выигрыш");
        boolean prefer = has(t, "лучше взять", "предпоч", "выбираем гарант", "лучше синица");
        return sure && prefer;
    }

    private static boolean weighsLossWithoutCoefficient(String t) {
        boolean asym = has(t, "потери весят больше", "loss weighs more", "проигрыш ощущается сильнее",
                "выигрыш ощущается слабее");
        boolean number = has(t, "коэффициент", "lambda", "в два раза", "в 2 раза", "1.5", "2.0", "2x");
        return asym && !number;
    }
}
