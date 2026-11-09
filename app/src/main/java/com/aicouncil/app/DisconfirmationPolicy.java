package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local disconfirmation contract.
 * A confident recommendation that names no observation which would change the conclusion
 * («однозначно рекомендую», «definitely, nothing would change my mind», «невозможно опровергнуть»)
 * stays a hypothesis, not a closed verdict.
 * Naming the criterion («критерий опровержения», «what would change my mind») is not the error.
 * The coordinator must not close a verdict that cannot be falsified.
 */
public final class DisconfirmationPolicy {
    private static final int MAX_SIGNALS = 2;

    private DisconfirmationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, sealed(a) || sealed(b),
                "disconfirmation: вывод объявлен неопровержимым");
        boolean missA = missing(a);
        boolean missB = missing(b);
        add(conflicts, initial, missA && missB,
                "оба эксперта закрывают решение без критерия опровержения");
        add(conflicts, initial, missA ^ missB,
                "disconfirmation: уверенное решение без наблюдения, которое его изменило бы");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "disconfirmation (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    /** Claim is declared immune to any future observation. */
    private static boolean sealed(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "ничто не изменит вывод",
                "невозможно опровергнуть",
                "нельзя опровергнуть",
                "что бы ни произошло, вывод верен",
                "unfalsifiable",
                "nothing would change my mind",
                "whatever the evidence, the conclusion stands");
    }

    /** Confident recommendation with no stated disconfirming observation. */
    private static boolean missing(String t) {
        if (t == null || t.isEmpty() || acknowledges(t) || sealed(t)) return false;
        if (hasDisconfirm(t)) return false;
        return confidentDecision(t);
    }

    private static boolean confidentDecision(String t) {
        boolean decision = has(t,
                "рекомендую",
                "следует выбрать",
                "стоит запустить",
                "надо внедр",
                "нужно выбрать",
                "i recommend",
                "we should");
        boolean certain = has(t,
                "однозначно",
                "без сомнений",
                "без вариантов",
                "гарантированно",
                "определённо",
                "определенно",
                "certainly",
                "definitely");
        return decision && certain;
    }

    private static boolean hasDisconfirm(String t) {
        return has(t,
                "изменило бы вывод",
                "изменит вывод",
                "что опровергнет",
                "критерий опровержения",
                "what would change",
                "would change my mind",
                "would falsify",
                "disconfirm",
                "если данные покажут обратное",
                "наблюдение, которое");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "критерий опровержения",
                "what would change my mind",
                "disconfirmation criterion",
                "не закрывай без опровержения",
                "вывод остаётся гипотезой, пока нет наблюдения");
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
