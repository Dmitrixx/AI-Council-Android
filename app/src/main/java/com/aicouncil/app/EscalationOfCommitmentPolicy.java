package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local escalation-of-commitment signals.
 * Flags a verdict that treats continuing a failing project, investment or course of action as clearly superior solely because of prior sunk costs, past effort or previous commitment, without stating the forward-looking expected value, opportunity cost of continuing, or explicit kill criteria.
 * Naming the gap is not itself an error.
 * The coordinator cannot close on past investment alone.
 */
public final class EscalationOfCommitmentPolicy {
    private static final int MAX_SIGNALS = 2;

    private EscalationOfCommitmentPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, escalatesWithoutForwardValue(a) || escalatesWithoutForwardValue(b),
                "escalation of commitment: вердикт закрывает на продолжении провального курса только из-за уже вложенных затрат — без оценки будущей ценности или критериев остановки");
        add(conflicts, initial, bothEscalate(a, b),
                "оба эксперта закрывают на эскалации без форвард-лукинг ценности");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "escalation of commitment (possible signal, requires context): " + core;
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
                "escalation of commitment", "эскалация обязательств",
                "sunk cost is sunk", "потопленные затраты не возвращаются",
                "forward-looking value", "будущая ценность",
                "kill criteria", "критерии остановки",
                "opportunity cost of continuing", "альтернативная стоимость продолжения");
    }

    private static boolean escalatesWithoutForwardValue(String t) {
        boolean pastInvestmentLang = has(t,
                "уже вложили", "уже потратили", "слишком много вложено",
                "нельзя бросать из-за затрат", "продолжаем потому что уже начали",
                "already invested", "too much sunk", "can't quit now because of past costs",
                "continue because we already spent", "эскалация из-за предыдущих вложений");
        boolean decisive = has(t,
                "поэтому продолжаем", "значит надо довести",
                "закрываем на продолжении", "therefore we continue",
                "so we must finish", "этого достаточно чтобы продолжать",
                "решает то что уже вложено");
        boolean mentionsForward = has(t,
                "будущая ценность", "forward-looking",
                "ожидаемая ценность дальше", "expected value going forward",
                "критерии остановки", "kill criteria",
                "альтернативная стоимость", "opportunity cost of continuing",
                "независимо от прошлых затрат", "regardless of past costs");
        return pastInvestmentLang && decisive && !mentionsForward && !acknowledges(t);
    }

    private static boolean bothEscalate(String a, String b) {
        return escalatesWithoutForwardValue(a) && escalatesWithoutForwardValue(b);
    }
}
