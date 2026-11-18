package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local adverse-selection signals.
 * Flags a verdict that treats people who opt into an offer, insurance, loan,
 * job or program as typical of the wider pool, without asking whether hidden
 * information makes the self-selected side systematically worse for the
 * counterparty. This is hidden type before the contract, not behavior change
 * after protection (moral hazard).
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely because volunteers look like the average.
 */
public final class AdverseSelectionPolicy {
    private static final int MAX_SIGNALS = 2;

    private AdverseSelectionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresPool(a) || ignoresPool(b),
                "adverse selection: вердикт закрывает выбор так, будто вошедшие в сделку сами — типичный пул, без скрытой информации");
        add(conflicts, initial, ignoresPool(a) && ignoresPool(b),
                "оба эксперта закрывают без учёта неблагоприятного отбора до сделки");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "adverse selection (possible signal, requires context): " + core;
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
                "adverse selection", "неблагоприятный отбор", "скрытая информация",
                "hidden information", "hidden type", "самоотбор в сделку",
                "lemons", "рынок лимонов", "пул смещён", "pool is selected",
                "who selects in", "кто входит сам не случаен");
    }

    private static boolean ignoresPool(String t) {
        boolean contract = has(t,
                "принимают предложение", "кто купит полис", "those who take the offer",
                "who opts in", "заёмщики которые придут", "applicants who apply",
                "добровольно войдут в сделку", "people who buy the guarantee",
                "кто оформит страховку");
        boolean typical = has(t,
                "такие же как средний", "same as the average", "качество пула не ухудшится",
                "pool will not worsen", "не хуже среднего рынка", "typical of everyone",
                "пул останется средним", "pool stays average");
        return contract && typical && !acknowledges(t);
    }
}
