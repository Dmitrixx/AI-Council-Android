package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local confirmation-bias and selective-evidence signals.
 * Flags when an answer or both answers primarily cite supporting evidence,
 * ignore disconfirming data, or frame the task in a way that only seeks confirmation.
 * Hypotheses requiring context, not automatic verdicts.
 */
public final class ConfirmationBiasPolicy {
    private static final int MAX_SIGNALS = 5;

    private ConfirmationBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, onlySupportingEvidence(a) || onlySupportingEvidence(b),
                "подтверждающее смещение: приводятся только поддерживающие факты, опровергающие не упомянуты");
        add(conflicts, initial, seeksOnlyConfirmation(a) || seeksOnlyConfirmation(b),
                "поиск только подтверждения: формулировка или анализ направлены на подтверждение гипотезы, а не её проверку");
        add(conflicts, initial, bothIgnoreDisconfirming(a, b),
                "оба игнорируют опровержения: ни один эксперт не рассматривает данные против основной гипотезы");
        add(conflicts, initial, asymmetricEvidence(a, b),
                "асимметрия доказательств: один опирается на подтверждающее, другой не балансирует или тоже");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "confirmation bias (possible signal, requires context): " + core;
        for (String existing : conflicts) {
            if (existing.contains(core.substring(0, Math.min(40, core.length())))) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static boolean multi(String text, String[] groupA, String[] groupB) {
        return has(text, groupA) && has(text, groupB);
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean onlySupportingEvidence(String t) {
        boolean support = has(t, "подтверждает", "поддерживает", "evidence for", "supports", "в пользу", "за", "confirms", "подтверджує");
        boolean against = has(t, "опровергает", "против", "against", "disconfirms", "опровергает", "не подтверждает", "contradicts", "суперечить");
        return support && !against && t.length() > 120;
    }

    private static boolean seeksOnlyConfirmation(String t) {
        return multi(t,
                new String[]{"проверить", "проверим", "verify", "test the hypothesis", "подтвердить гипотезу"},
                new String[]{"только", "only", "лишь", "исключительно", "solely"})
                || has(t, "ищем подтверждение", "looking for confirmation", "шукаємо підтвердження");
    }

    private static boolean bothIgnoreDisconfirming(String a, String b) {
        return (onlySupportingEvidence(a) || seeksOnlyConfirmation(a))
                && (onlySupportingEvidence(b) || seeksOnlyConfirmation(b));
    }

    private static boolean asymmetricEvidence(String a, String b) {
        boolean aOnly = onlySupportingEvidence(a);
        boolean bOnly = onlySupportingEvidence(b);
        return aOnly != bOnly;
    }
}
