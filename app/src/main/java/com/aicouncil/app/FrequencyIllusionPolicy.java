package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local frequency illusion / Baader-Meinhof phenomenon policy.
 * Flags a verdict that treats a recently noticed item, word, or event as suddenly more common
 * or important without independent base-rate or sampling evidence. Naming the error is not itself an error.
 * Signals remain hypotheses that require context.
 */
public final class FrequencyIllusionPolicy {
    private static final int MAX_SIGNALS = 2;

    private FrequencyIllusionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "frequency illusion: недавне замеченное принимается за внезапно частое без независимой проверки базовой частоты");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта опираются на недавнее замечание как на доказательство частоты");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "frequency illusion (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "только что заметил и теперь везде",
                "недавно узнал и сразу часто встречаю",
                "только начал замечать поэтому это повсюду",
                "just noticed it and now it's everywhere therefore common",
                "recently learned the word and see it constantly so it must be frequent",
                "only started seeing this pattern after I looked for it so it is widespread",
                "baader-meinhof means it is actually increasing");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "frequency illusion",
                "baader-meinhof",
                "недавнее замечание не равно частоте",
                "иллюзия частоты не доказательство",
                "noticing something recently does not make it more common",
                "frequency illusion: recent exposure does not update the base rate");
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
