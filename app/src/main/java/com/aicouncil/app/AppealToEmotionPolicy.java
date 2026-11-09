package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local appeal-to-emotion signals.
 * Using strong emotional language (fear, pity, anger, disgust) as the main reason
 * for a conclusion instead of evidence or logic
 * («если не сделаем — будет ужас», «подумайте о детях», «appeal to emotion», «think of the children»)
 * is a hypothesis, not independent evidence.
 * Naming the fallacy is not itself the error.
 * The coordinator must not close a verdict on pure emotional appeal alone.
 */
public final class AppealToEmotionPolicy {
    private static final int MAX_SIGNALS = 2;

    private AppealToEmotionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, ignores(a) || ignores(b),
                "appeal to emotion: вывод основан главным образом на эмоциональном воздействии вместо доказательств");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта опираются на эмоции вместо логики или данных");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "appeal to emotion (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }

    private static boolean naivePattern(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "если не сделаем — будет катастрофа",
                "подумайте о детях",
                "как можно быть таким жестоким",
                "это вызывает отвращение",
                "страшно даже подумать",
                "каждый нормальный человек будет в ужасе",
                "это оскорбляет память",
                "think of the children",
                "if we don't act it will be a disaster",
                "this is disgusting and therefore wrong",
                "how can anyone be so heartless",
                "it makes me sick to think",
                "any decent person would be horrified",
                "appeal to emotion",
                "purely emotional argument",
                "fear-based reasoning",
                "pity appeal");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "appeal to emotion",
                "emotional appeal",
                "апелляция к эмоциям",
                "эмоциональный аргумент не доказательство",
                "страх не основание",
                "do not rely on emotion alone",
                "emotional language is not evidence");
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
