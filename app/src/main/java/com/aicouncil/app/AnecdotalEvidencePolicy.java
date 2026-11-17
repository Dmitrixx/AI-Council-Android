package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local anecdotal-evidence signals.
 * Flags a verdict that treats one personal or second-hand story as decisive
 * for a rate, a risk, or a general choice without a base rate, a sample,
 * or an explicit note that the story is only an illustration.
 * Naming that gap is not itself an error.
 * The coordinator cannot close on a single story.
 */
public final class AnecdotalEvidencePolicy {
    private static final int MAX_SIGNALS = 2;

    private AnecdotalEvidencePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, storyWithoutRate(a) || storyWithoutRate(b),
                "anecdotal evidence: вердикт закрывает частоту или выбор одной личной историей без базовой частоты и без пометки, что рассказ только иллюстрация");
        add(conflicts, initial, storyWithoutRate(a) && storyWithoutRate(b),
                "оба эксперта закрывают на одном рассказе вместо частоты или выборки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "anecdotal evidence (possible signal, requires context): " + core;
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
                "anecdotal evidence", "анекдотическое свидетельство",
                "анекдот не доказательство", "история не выборка",
                "один рассказ не", "anecdote is not a rate",
                "story is not a sample", "illustration not evidence",
                "только иллюстрация", "не частота");
    }

    private static boolean storyWithoutRate(String t) {
        boolean story = has(t,
                "у меня был случай", "со мной было", "мой знакомый", "моя знакомая",
                "я знаю человека", "один пациент", "моя тётя", "мой дядя",
                "i know a guy", "a friend of mine", "my uncle", "my aunt",
                "личная история", "реальная история", "one person's story",
                "heard of someone who");
        boolean decisive = has(t,
                "поэтому выбира", "значит надо", "закрываем", "это доказывает",
                "therefore", "so we should", "that's why", "we should choose",
                "рекомендую", "recommend");
        boolean rate = has(t,
                "базовая частота", "base rate", "выборка", "sample size",
                "на n=", " n =", "из 100", "из 1000");
        return story && decisive && !rate && !acknowledges(t);
    }
}
