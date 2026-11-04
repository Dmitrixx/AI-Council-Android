package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local ecological-fallacy signals.
 * Group-level averages, rates or correlations do not automatically apply to a specific individual
 * without individual-level data. Hypotheses, not verdicts.
 */
public final class EcologicalFallacyPolicy {
    private static final int MAX_SIGNALS = 2;

    private EcologicalFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ecologicalApplication(a) || ecologicalApplication(b),
                "групповая статистика или среднее применяется к конкретному индивиду без индивидуальных данных");
        add(conflicts, initial, bothEcological(a, b),
                "оба эксперта переносят групповой уровень на индивида без оговорок");
        add(conflicts, initial, fork(a, b),
                "развилка: вывод из групповой статистики против явного разделения группового и индивидуального уровня");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "ecological fallacy (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(36, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean ecologicalApplication(String text) {
        return hasGroupToIndividual(text) && !hasSeparation(text);
    }

    private static boolean bothEcological(String a, String b) {
        return ecologicalApplication(a) && ecologicalApplication(b);
    }

    private static boolean fork(String a, String b) {
        return (ecologicalApplication(a) && hasSeparation(b))
                || (ecologicalApplication(b) && hasSeparation(a));
    }

    private static boolean hasGroupToIndividual(String text) {
        boolean group = contains(text,
                "в среднем по группе", "group average", "среднее по популяции",
                "групповая статистика", "population rate", "среднее по классу",
                "в этой группе в среднем", "on average for the group",
                "корреляция на уровне групп", "aggregate correlation");
        boolean individual = contains(text,
                "этот человек", "this individual", "конкретный человек",
                "для него", "for him", "for her", "этот пациент",
                "this person", "данный индивид", "конкретный случай",
                "применяется к нему", "applies to this person");
        return group && individual;
    }

    private static boolean hasSeparation(String text) {
        return contains(text,
                "групповое среднее не применимо к индивиду",
                "ecological fallacy", "экологическая ошибка",
                "нужны индивидуальные данные", "group rate does not apply to individual",
                "не переносить групповой уровень на человека",
                "individual-level data required", "не из среднего по группе следует для него");
    }

    private static boolean contains(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
