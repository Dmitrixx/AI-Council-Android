package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local strategic anticipation / theory-of-mind check.
 * Flags a verdict that treats other agents' behavior as fixed or non-strategic
 * (they will not change their actions, information, or incentives in response to our decision)
 * without naming likely adaptive responses, the incentives they face, or higher-order beliefs.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on a static reaction function when strategic interaction is material.
 */
public final class StrategicAnticipationPolicy {
    private static final int MAX_SIGNALS = 2;

    private StrategicAnticipationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesWithoutAnticipation(a) || closesWithoutAnticipation(b),
                "strategic anticipation: вердикт закрывает на фиксированном поведении других агентов без упоминания их адаптивных ответов, инцентивов или стратегического взаимодействия");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на статической реакции других, игнорируя стратегическое антиципирование");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "strategic anticipation (possible signal, requires context): " + core;
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
                "strategic response", "стратегический ответ", "agents will adapt",
                "агенты адаптируются", "theory of mind", "теория разума",
                "higher-order beliefs", "высшие уровни убеждений",
                "incentive response", "реакция на инцентивы",
                "they will change", "они изменят поведение",
                "game theoretic", "игровое взаимодействие");
    }

    private static boolean closesWithoutAnticipation(String t) {
        boolean staticLang = has(t,
                "другие не изменятся", "others will stay the same", "fixed behavior",
                "фиксированное поведение", "they won't respond",
                "нет реакции", "no strategic response", "агенты не адаптируются");
        boolean closing = has(t,
                "поэтому закрываем", "therefore we close", "решение однозначно",
                "the choice is settled", "делаем так");
        return staticLang && closing && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesWithoutAnticipation(a) && closesWithoutAnticipation(b);
    }
}
