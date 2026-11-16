package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local duration-neglect signals (Kahneman peak-end extension).
 * Flags a verdict that treats the peak or end intensity of an experience as decisive for its overall value while never stating or weighing the duration of the experience.
 * Naming the gap is not itself an error.
 * The coordinator cannot close on peak or end intensity alone.
 */
public final class DurationNeglectPolicy {
    private static final int MAX_SIGNALS = 2;

    private DurationNeglectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, neglectWithoutDuration(a) || neglectWithoutDuration(b),
                "duration neglect: вердикт закрывает оценку опыта на пике или конце, игнорируя длительность — без учёта сколько времени он длится");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на пике/конце без длительности");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "duration neglect (possible signal, requires context): " + core;
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
                "duration neglect", "пренебрежение длительностью",
                "peak-end rule", "правило пика и конца",
                "duration matters", "длительность важна",
                "how long it lasts", "сколько времени длится");
    }

    private static boolean neglectWithoutDuration(String t) {
        boolean peakEndLang = has(t,
                "пик решает", "конец решает",
                "самый яркий момент важнее", "peak intensity decides",
                "how it ends matters most", "как заканчивается важнее",
                "игнорируем сколько длится", "duration doesn't matter",
                "только пик и конец", "only the peak and end");
        boolean decisive = has(t,
                "поэтому выбира", "значит лучше",
                "закрываем", "recommend based on the end",
                "therefore the ending decides", "so we choose the one with better peak",
                "этого достаточно", "решает конец");
        boolean mentionsDuration = has(t,
                "длительность", "duration",
                "сколько длится", "how long",
                "время опыта", "total time",
                "независимо от длины", "regardless of length");
        return peakEndLang && decisive && !mentionsDuration && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return neglectWithoutDuration(a) && neglectWithoutDuration(b);
    }
}
