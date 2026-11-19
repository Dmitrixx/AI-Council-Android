package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local multiple-comparisons / look-elsewhere signals.
 * Flags a verdict that treats a significant finding, p-value or "proven"
 * claim as decisive without mentioning how many tests were run, family-wise
 * error, the look-elsewhere effect or the garden of forking paths.
 * Naming the gap is not itself an error. The coordinator cannot close on an
 * uncorrected significance claim.
 */
public final class MultipleComparisonsPolicy {
    private static final int MAX_SIGNALS = 2;

    private MultipleComparisonsPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, significantWithoutCorrection(a) || significantWithoutCorrection(b),
                "multiple comparisons: вердикт закрывает выбор значимым результатом без поправки на число тестов или look-elsewhere");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на нескорректированной значимости");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "multiple comparisons (possible signal, requires context): " + core;
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
                "multiple comparisons", "множественн",
                "look-elsewhere", "look elsewhere",
                "family-wise", "семейная ошибка",
                "bonferroni", "холм", "holm",
                "fdr", "false discovery",
                "forking paths", "сад расходящихся",
                "число тестов", "how many tests",
                "поправка на множественность", "corrected for multiple");
    }

    private static boolean significantWithoutCorrection(String t) {
        boolean significant = has(t,
                "статистически значим", "statistically significant",
                "p <", "p<", "p-value", "p =",
                "значимый результат", "significant finding",
                "доказано что", "proven that",
                "достиг значимости", "reached significance",
                "отклоняем нулевую", "reject the null");
        boolean decisive = has(t,
                "поэтому выбираем", "значит надо",
                "therefore we should", "so we should",
                "этого достаточно", "this is decisive",
                "закрываем выбор", "вопрос закрыт",
                "решение принято", "we can conclude");
        return significant && decisive && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return significantWithoutCorrection(a) && significantWithoutCorrection(b);
    }
}
