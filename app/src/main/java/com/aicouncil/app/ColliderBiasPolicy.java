package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local collider / Berkson signals.
 * Flags a verdict that treats an association observed only inside a selected
 * subgroup (admitted, hospitalized, survivors, respondents) as the association
 * in the full population, without asking whether selection itself created it.
 * Naming the gap is not itself an error.
 * The coordinator cannot close a population claim solely on the selected group.
 */
public final class ColliderBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private ColliderBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesOnCollider(a) || closesOnCollider(b),
                "связь внутри отобранной группы принята за связь в популяции без проверки, не создал ли отбор коллайдер");
        add(conflicts, initial, closesOnCollider(a) && closesOnCollider(b),
                "оба эксперта переносят связь из отобранной группы на популяцию");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "collider bias (possible signal, requires context): возможный сигнал: " + core;
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

    private static boolean namesSelection(String t) {
        return has(t,
                "collider", "коллайдер", "berkson", "берксон",
                "selection induced", "отбор создал", "conditioning on a collider");
    }

    private static boolean closesOnCollider(String t) {
        boolean selected = has(t,
                "среди поступивших", "among those admitted",
                "среди выживших", "among survivors",
                "среди ответивших", "among respondents",
                "среди госпитализированных", "among hospitalized");
        boolean conclude = has(t,
                "значит в популяции", "therefore in the population",
                "связь такая же в целом", "the association holds generally",
                "это доказывает связь");
        return selected && conclude && !namesSelection(t);
    }
}
