package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local motivated-reasoning signals.
 * Flags a verdict that appears to select, interpret, or weight evidence primarily
 * to protect a preferred conclusion, identity, or prior commitment, rather than
 * updating on the full balance of evidence (including disconfirming data).
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on a conclusion-preserving reading of the evidence.
 */
public final class MotivatedReasoningPolicy {
    private static final int MAX_SIGNALS = 2;

    private MotivatedReasoningPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesOnMotivated(a) || closesOnMotivated(b),
                "вердикт защищает предпочтительный вывод, отбирая или переинтерпретируя доказательства вместо обновления на полном балансе");
        add(conflicts, initial, closesOnMotivated(a) && closesOnMotivated(b),
                "оба эксперта закрывают на conclusion-preserving чтении доказательств");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "motivated reasoning (possible signal, requires context): " + core;
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
                "motivated reasoning", "мотивированное рассуждение",
                "identity-protective", "защитное для идентичности",
                "we update on the full balance", "обновляемся на полном балансе",
                "disconfirming evidence still counts", "опровергающие данные тоже считаются");
    }

    private static boolean closesOnMotivated(String t) {
        boolean protective = has(t,
                "это подтверждает нашу позицию", "this confirms our view",
                "как мы и ожидали", "as we expected all along",
                "доказательства в нашу пользу", "the evidence supports what we already believed",
                "неудобные данные можно объяснить", "the inconvenient data can be explained away");
        boolean closes = has(t,
                "поэтому вердикт", "therefore the conclusion stands",
                "закрываем на исходной позиции", "we stick with the original conclusion",
                "значит наша сторона права", "so our side is right");
        boolean balanced = has(t,
                "полный баланс доказательств", "full balance of evidence",
                "включая опровергающие", "including disconfirming",
                "обновление priors", "updating the prior");
        return protective && closes && !balanced && !acknowledges(t);
    }
}
