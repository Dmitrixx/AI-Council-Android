package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local "this time is different" signals.
 * Flags a verdict that treats a specific case as immune to the historical base rate
 * or reference class solely because "this time is different", "the conditions have changed",
 * or "we are special", without naming the reference class, the base rate, or an explicit
 * mechanism why the difference overrides the outside view.
 * Naming the gap is not itself an error.
 * The coordinator cannot close a numeric forecast or timeline solely on the claim that this case is exceptional.
 */
public final class ThisTimeIsDifferentPolicy {
    private static final int MAX_SIGNALS = 2;

    private ThisTimeIsDifferentPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesOnDifference(a) || closesOnDifference(b),
                "this time is different: вердикт закрывает прогноз или срок только утверждением «в этот раз всё иначе» без референс-класса или явного механизма, почему различие отменяет базовую частоту");
        add(conflicts, initial, closesOnDifference(a) && closesOnDifference(b),
                "оба эксперта закрывают на «в этот раз другое» без outside view");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "this time is different (possible signal, requires context): " + core;
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

    private static boolean accountsForReference(String t) {
        return has(t,
                "reference class", "референс-класс", "outside view", "внешний взгляд",
                "базовая частота", "base rate", "похожие случаи",
                "историческая частота", "why this time differs", "почему в этот раз");
    }

    private static boolean closesOnDifference(String t) {
        boolean claim = has(t,
                "в этот раз всё иначе", "this time is different", "условия изменились",
                "мы особенные", "we are special", "это не как обычно",
                "история не повторится", "conditions have changed", "теперь другое");
        boolean conclude = has(t, "следовательно", "рекомендую", "therefore", "i recommend", "стоит выбрать", "делать", "закончим в срок");
        return claim && conclude && !accountsForReference(t);
    }
}
