package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local Lucas-critique signals.
 * Flags a verdict that treats a historical correlation or estimated policy
 * effect as stable after the rule, incentive or regime itself changes, without
 * asking whether agents will reoptimize. Naming the gap is not itself an error.
 * The coordinator cannot close solely on past reduced-form relationships under
 * a new policy.
 */
public final class LucasCritiquePolicy {
    private static final int MAX_SIGNALS = 2;

    private LucasCritiquePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresLucas(a) || ignoresLucas(b),
                "lucas critique: вердикт закрывает политику так, будто прошлое соотношение удержит после смены правил");
        add(conflicts, initial, ignoresLucas(a) && ignoresLucas(b),
                "оба эксперта закрывают без учёта критики Лукаса");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "lucas critique (possible signal, requires context): " + core;
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
                "lucas critique", "критика лукаса", "критика лукаса",
                "reoptimize", "переоптимизируют", "agents will change behavior",
                "поведение изменится при новых правилах", "regime change",
                "reduced form not structural", "не структурная оценка",
                "policy invariance fails");
    }

    private static boolean ignoresLucas(String t) {
        boolean policyChange = has(t,
                "введём правило", "change the rule", "новая политика", "new policy",
                "изменим стимул", "change the incentive", "введём налог", "introduce a tax",
                "установим потолок", "set a cap", "новое регулирование", "new regulation",
                "поменяем режим", "regime shift");
        boolean pastHolds = has(t,
                "как в прошлых данных", "as in past data", "историческая зависимость сохранится",
                "historical relationship will hold", "коэффициент тот же",
                "same coefficient", "эффект останется прежним", "effect will stay the same",
                "по прошлому опыту сработает так же", "past experience guarantees the same response");
        return policyChange && pastHolds && !acknowledges(t);
    }
}
