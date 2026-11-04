package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local Simpson / aggregation-reversal signals.
 * Flags when a conclusion treats an aggregate trend as if it holds inside
 * every subgroup, without checking whether the relationship reverses
 * once the data are stratified.
 * Hypotheses requiring context, not automatic verdicts. Coordinator must not average them away.
 */
public final class SimpsonParadoxPolicy {
    private static final int MAX_SIGNALS = 2;

    private SimpsonParadoxPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, projectsAggregate(a) || projectsAggregate(b),
                "общий тренд не равен подгруппам: вывод «в целом, значит в каждой группе» без проверки, не разворачивается ли связь при стратификации");
        add(conflicts, initial, bothProject(a, b),
                "оба эксперта переносят агрегированный тренд на подгруппы и не называют Симпсона или разбиение");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "simpson paradox (возможный сигнал, требует контекста): " + core;
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

    private static boolean acknowledgesStratification(String t) {
        return has(t,
                "симпсон", "simpson", "стратиф", "при разбиении",
                "внутри подгрупп", "within subgroups", "разворачивается",
                "не переносится на каждую", "does not hold in every");
    }

    private static boolean aggregateClaim(String t) {
        return has(t,
                "в целом", "по всей выборке", "в агрегате", "агрегирован",
                "in aggregate", "overall trend", "суммарный тренд", "в среднем по всем");
    }

    private static boolean appliesToEverySlice(String t) {
        return has(t,
                "в каждой группе", "для каждой подгруппы", "в каждом сегменте",
                "для всех сегментов", "значит для всех групп", "для каждой группы",
                "every subgroup", "each subgroup", "in every group");
    }

    private static boolean projectsAggregate(String t) {
        return aggregateClaim(t) && appliesToEverySlice(t) && !acknowledgesStratification(t);
    }

    private static boolean bothProject(String a, String b) {
        return projectsAggregate(a) && projectsAggregate(b);
    }
}
