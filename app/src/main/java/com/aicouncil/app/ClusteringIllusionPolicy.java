package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local clustering-illusion / pattern-seeking check.
 * Flags a verdict that treats a cluster, streak, or apparent pattern in sparse or random-looking data
 * as meaningful evidence without independent statistical validation, base-rate comparison, or an explicit note that chance is plausible.
 * Naming the gap is not itself an error.
 * Signals remain hypotheses requiring context.
 */
public final class ClusteringIllusionPolicy {
    private static final int MAX_SIGNALS = 2;

    private ClusteringIllusionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "clustering illusion: random cluster/pattern treated as meaningful signal");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают вердикт на скоплении без проверки случайности");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "clustering illusion (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || hasStatsCheck(t)) return false;
        return hasClusterMarker(t) && closes(t);
    }

    private static boolean hasClusterMarker(String t) {
        return has(t,
                "скопление",
                "кластер",
                "серия подряд",
                "паттерн в данных",
                "не может быть случайно",
                "cluster of",
                "streak of",
                "pattern in the data",
                "too many to be chance",
                "clustering of events",
                "hot streak");
    }

    private static boolean closes(String t) {
        return has(t,
                "поэтому",
                "значит",
                "рекомендую",
                "вердикт",
                "выбираем",
                "следует выбрать",
                "therefore",
                "we should",
                "the choice is",
                "enough to decide",
                "clear signal");
    }

    private static boolean hasStatsCheck(String t) {
        return has(t,
                "статистическая проверка",
                "базовая частота",
                "может быть случайно",
                "проверил на случайность",
                "statistical test",
                "base rate comparison",
                "could be chance",
                "random variation plausible",
                "multiple comparisons corrected");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "clustering illusion",
                "иллюзия кластеризации",
                "скопление не доказательство",
                "pattern may be random",
                "cluster does not imply causation or signal");
    }

    private static boolean has(String text, String... phrases) {
        if (text == null) return false;
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
