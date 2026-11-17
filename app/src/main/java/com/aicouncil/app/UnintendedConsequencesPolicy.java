package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local unintended-consequences / perverse-incentive signals.
 * Flags a verdict that treats a proposed action or policy as clearly superior
 * based on its intended first-order effect without stating possible second-order
 * adaptations, feedback loops, behavioral responses, or ways the intervention
 * could undermine its own goal (Cobra effect, perverse incentives).
 * Naming that gap is not itself an error.
 * The coordinator cannot close solely on the intended effect without considering
 * unintended consequences.
 */
public final class UnintendedConsequencesPolicy {
    private static final int MAX_SIGNALS = 2;

    private UnintendedConsequencesPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesOnIntendedOnly(a) || closesOnIntendedOnly(b),
                "unintended consequences: вердикт закрывает на намеренном эффекте без учёта адаптаций, обратных связей или перверсных стимулов");
        add(conflicts, initial, closesOnIntendedOnly(a) && closesOnIntendedOnly(b),
                "оба эксперта закрывают без анализа unintended consequences / cobra effect");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "unintended consequences (possible signal, requires context): " + core;
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
                "unintended consequences", "непреднамеренные последствия",
                "cobra effect", "эффект кобры",
                "perverse incentives", "перверсные стимулы",
                "feedback loop", "обратная связь",
                "behavioral adaptation", "адаптация поведения",
                "second-order effect", "второй порядок");
    }

    private static boolean closesOnIntendedOnly(String t) {
        boolean intended = has(t,
                "это решит проблему", "прямое решение", "intended effect",
                "поэтому вводим", "запускаем программу", "the solution is to",
                "this will fix", "мы просто запретим", "просто субсидируем");
        boolean decisive = has(t,
                "поэтому выбираем", "значит внедряем", "закрываем на этом",
                "therefore we implement", "so we proceed with");
        boolean mentionsUnintended = has(t,
                "может вызвать", "побочный эффект", "unintended",
                "люди обойдут", "адаптация", "обратный эффект");
        return intended && decisive && !mentionsUnintended && !acknowledges(t);
    }
}
