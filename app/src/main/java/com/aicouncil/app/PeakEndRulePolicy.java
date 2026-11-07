package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local peak-end rule and duration-neglect signals.
 * Judging an experience, process, plan or outcome primarily by its most intense moment (peak)
 * and its final moments (end), while neglecting the duration of the experience and the average
 * quality over the whole period, is a hypothesis, not evidence.
 * Distinct from focusing illusion (one factor) and affect heuristic (emotion-driven).
 * Coordinator must not treat "the ending was great so the whole thing was good" as settled
 * without considering duration and intermediate quality.
 */
public final class PeakEndRulePolicy {
    private static final int MAX_SIGNALS = 2;

    private PeakEndRulePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "peak-end rule / duration neglect: вывод, что опыт/план/результат хорош или плох в основном по пиковому моменту и окончанию, без учёта длительности и среднего качества за весь период");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта оценивают по пику и концу, игнорируя длительность и промежуточное качество");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "peak-end rule (possible signal, requires context): " + core;
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
                "peak-end rule", "правило пика и конца", "duration neglect", "пренебрежение длительностью",
                "average quality over time", "среднее качество за период",
                "duration matters", "длительность важна",
                "not just the ending", "не только окончание",
                "intermediate quality", "промежуточное качество");
    }

    private static boolean naivePattern(String t) {
        boolean peakEnd = has(t,
                "окончание было отличным поэтому", "конец был хорошим значит",
                "the ending was great so", "because it ended well",
                "пик был сильным поэтому весь", "the peak moment made it",
                "запомнился финал", "remembered the ending",
                "не важно сколько длилось", "duration doesn't matter",
                "главное как закончилось", "what matters is how it ended",
                "по пику и концу", "judged by peak and end");
        return peakEnd && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
