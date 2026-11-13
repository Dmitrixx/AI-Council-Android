package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local belief-perseverance / belief-perseverance signals.
 * Clinging to an original belief or conclusion despite explicit contradictory evidence,
 * disconfirming data, or failed predictions, without acknowledging the new evidence
 * or updating the prior, is a hypothesis, not evidence.
 * Distinct from confirmation bias (selective search) and sunk cost (past investment).
 * Coordinator must not treat the original stance as settled once contrary observations are named.
 */
public final class BeliefPerseverancePolicy {
    private static final int MAX_SIGNALS = 2;

    private BeliefPerseverancePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "belief perseverance: упорство в исходном убеждении вопреки противоречащим данным («всё равно я прав», «данные не меняют мое мнение») без явного учёта новых доказательств");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта цепляются за исходным выводом вопреки названным противоречащим наблюдениям");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "belief perseverance (possible signal, requires context): " + core;
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
                "belief perseverance", "упорство в убеждениях",
                "update the prior", "обновить априорное",
                "contrary evidence changes", "противоречащие данные меняют",
                "reconsider in light of", "пересмотреть в свете",
                "new data overrides", "новые данные перевешивают");
    }

    private static boolean naivePattern(String t) {
        boolean cling = has(t,
                "всё равно я прав", "данные не меняют моё мнение",
                "я всё равно считаю так", "это не меняет вывода",
                "despite the evidence I still", "вопреки данным всё равно",
                "still believe the original", "остаюсь при исходном",
                "evidence doesn't change my mind", "доказательства не влияют",
                "исходная позиция остаётся", "несмотря на противоречия считаю по-прежнему");
        return cling && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
