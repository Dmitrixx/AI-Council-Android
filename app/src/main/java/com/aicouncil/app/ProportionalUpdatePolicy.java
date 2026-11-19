package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local proportional / Bayesian-style belief-update signals.
 * Flags a verdict that treats new evidence as fully confirmatory or fully disconfirmatory
 * ("proves", "completely changes", "settles the question", "now we know") without stating
 * the strength of the evidence relative to the prior, the direction and rough magnitude
 * of the update, likelihood considerations, or that weak or dependent evidence should
 * move beliefs less. Naming the gap is not itself an error.
 * The coordinator cannot close solely on unweighted "decisive" evidence.
 * Distinct from base-rate neglect and point-estimate checks: this focuses on the
 * proportionality of the update itself.
 */
public final class ProportionalUpdatePolicy {
    private static final int MAX_SIGNALS = 2;

    private ProportionalUpdatePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesWithoutProportionalUpdate(a) || closesWithoutProportionalUpdate(b),
                "proportional update: вердикт закрывает на «доказательно/опровергнуто» без оценки силы новых данных относительно априорных, направления и примерной величины обновления убеждений");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на решающих доказательствах без пропорционального обновления вероятностей");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "proportional update (possible signal, requires context): " + core;
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
                "proportional update", "пропорциональное обновление",
                "likelihood ratio", "отношение правдоподобия",
                "strength of evidence", "сила доказательств",
                "moves the prior", "сдвигает априорные",
                "weak evidence", "слабые данные", "dependent evidence",
                "зависимые свидетельства", "update magnitude");
    }

    private static boolean closesWithoutProportionalUpdate(String t) {
        boolean decisiveLang = has(t,
                "доказывает", "proves", "опровергает", "disproves",
                "полностью меняет", "completely changes",
                "теперь точно знаем", "now we know for sure",
                "решает вопрос", "settles the question",
                "окончательно подтверждает", "definitively confirms");
        boolean closing = has(t,
                "поэтому делаем", "therefore we proceed",
                "вердикт ясен", "the conclusion is clear",
                "закрываем на этом", "so we close on this");
        boolean mentionsUpdate = has(t,
                "сила свидетельств", "evidence strength",
                "likelihood", "насколько сдвигает",
                "пропорционально", "prior still relevant",
                "априорные остаются");
        return decisiveLang && closing && !mentionsUpdate && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesWithoutProportionalUpdate(a) && closesWithoutProportionalUpdate(b);
    }
}
