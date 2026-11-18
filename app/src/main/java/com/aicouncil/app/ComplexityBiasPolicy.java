package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local complexity-bias / Occam signals.
 * Flags when a verdict prefers a complex multi-factor or elaborate explanation
 * over a simpler one solely because it is more detailed, without showing that
 * the added complexity improves prediction, fits data better, or is necessary.
 * Naming the gap is not itself an error. The coordinator cannot close solely on complexity.
 * Hypotheses requiring context, not verdicts.
 */
public final class ComplexityBiasPolicy {
    private static final int MAX_SIGNALS = 3;

    private ComplexityBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, prefersComplexity(a) || prefersComplexity(b),
                "complexity bias: сложное многофакторное объяснение предпочтено простому без демонстрации лучшей предсказательной силы");
        add(conflicts, initial, bothPreferComplexity(a, b),
                "оба предпочитают более сложное объяснение без сравнения с более простым и без оценки прироста точности");
        add(conflicts, initial, complexityAsVirtue(a) || complexityAsVirtue(b),
                "сложность сама по себе трактуется как достоинство объяснения без проверки необходимости факторов");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "complexity bias (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(38, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (affirmed(text, word)) return true;
        return false;
    }

    private static boolean affirmed(String text, String phrase) {
        int from = 0;
        while (from < text.length()) {
            int i = text.indexOf(phrase, from);
            if (i < 0) return false;
            int start = Math.max(0, i - 4);
            String prefix = text.substring(start, i);
            if (!prefix.endsWith("\u043d\u0435 ") && !prefix.endsWith("not ")) return true;
            from = i + Math.max(1, phrase.length());
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean accountsForSimplicity(String t) {
        return has(t,
                "\u0431\u0440\u0438\u0442\u0432\u0430 \u043e\u043a\u043a\u0430\u043c\u0430", "simpler explanation", "parsimony", "\u043f\u0440\u043e\u0449\u0435 \u043e\u0431\u044a\u044f\u0441\u043d\u0435\u043d\u0438\u0435",
                "\u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u043d\u044b\u0435 \u0444\u0430\u043a\u0442\u043e\u0440\u044b \u043d\u0435 \u0443\u043b\u0443\u0447\u0448\u0430\u044e\u0442", "added factors do not improve",
                "predictive power", "\u043f\u0440\u0435\u0434\u0441\u043a\u0430\u0437\u0430\u0442\u0435\u043b\u044c\u043d\u0430\u044f \u0441\u0438\u043b\u0430", "out of sample");
    }

    private static boolean prefersComplexity(String t) {
        boolean complex = has(t,
                "\u0431\u043e\u043b\u0435\u0435 \u0441\u043b\u043e\u0436\u043d\u043e\u0435 \u043e\u0431\u044a\u044f\u0441\u043d\u0435\u043d\u0438\u0435 \u043b\u0443\u0447\u0448\u0435", "complex multi-factor is better",
                "\u0443\u0447\u0438\u0442\u044b\u0432\u0430\u0435\u0442 \u0431\u043e\u043b\u044c\u0448\u0435 \u0444\u0430\u043a\u0442\u043e\u0440\u043e\u0432", "more factors make it better",
                "\u044d\u0442\u043e \u0441\u043b\u0438\u0448\u043a\u043e\u043c \u043f\u0440\u043e\u0441\u0442\u043e", "too simple to be true");
        boolean conclude = has(t, "\u0437\u043d\u0430\u0447\u0438\u0442", "\u0441\u043b\u0435\u0434\u043e\u0432\u0430\u0442\u0435\u043b\u044c\u043d\u043e", "\u0440\u0435\u043a\u043e\u043c\u0435\u043d\u0434\u0443\u044e", "therefore", "i recommend", "\u0441\u0442\u043e\u0438\u0442 \u0432\u044b\u0431\u0440\u0430\u0442\u044c");
        return complex && conclude && !accountsForSimplicity(t);
    }

    private static boolean bothPreferComplexity(String a, String b) {
        return prefersComplexity(a) && prefersComplexity(b);
    }

    private static boolean complexityAsVirtue(String t) {
        boolean virtue = has(t, "\u0441\u043b\u043e\u0436\u043d\u043e\u0441\u0442\u044c \u2014 \u043f\u0440\u0438\u0437\u043d\u0430\u043a \u043a\u0430\u0447\u0435\u0441\u0442\u0432\u0430", "complexity is a virtue", "more elaborate is more correct");
        return virtue && !accountsForSimplicity(t);
    }
}
