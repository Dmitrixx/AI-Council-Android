package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local wishful-thinking / motivated-reasoning check.
 * Flags a verdict that closes on a preferred or pleasant outcome as if the desire
 * were evidence of likelihood, or that discounts contrary evidence because the
 * conclusion is unwelcome, without independent justification.
 * Naming the gap is not itself an error.
 * Signals remain hypotheses requiring context.
 */
public final class WishfulThinkingPolicy {
    private static final int MAX_SIGNALS = 2;

    private WishfulThinkingPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        boolean hitA = commits(answerA);
        boolean hitB = commits(answerB);
        add(conflicts, initial, hitA || hitB,
                "wishful thinking / motivated reasoning: desired or pleasant outcome treated as evidence, or contrary data discounted because unwelcome");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают выбор на желаемом исходе или отбрасывают неудобные данные");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "wishful thinking / motivated reasoning (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(48, core.length()));
        for (String existing : conflicts) {
            if (existing != null && existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String raw) {
        String t = lower(raw);
        if (t.isEmpty() || acknowledges(t) || hasMitigation(t)) return false;
        return (hasDesireAsEvidence(t) || hasDiscountUnwelcome(t)) && closes(t);
    }

    private static boolean hasDesireAsEvidence(String t) {
        return has(t,
                "хочется верить",
                "было бы хорошо если",
                "надеюсь что это так",
                "должно быть правдой потому что хочу",
                "хочу верить",
                "приятно думать что",
                "желаю чтобы это было так",
                "it would be nice if",
                "i hope this is true",
                "because we want it to be",
                "must be true because we prefer",
                "wish it were",
                "prefer to believe",
                "would be great if",
                "хочется чтобы это работало");
    }

    private static boolean hasDiscountUnwelcome(String t) {
        return has(t,
                "не хочу верить что",
                "это неприятно поэтому отбросим",
                "данные неудобные но",
                "игнорируем то что не нравится",
                "не смотря на данные я верю",
                "неудобная правда",
                "ignore the inconvenient",
                "don't want to believe",
                "unwelcome data",
                "despite the evidence I prefer",
                "set aside the bad news",
                "отбросим то что не нравится");
    }

    private static boolean closes(String t) {
        return has(t,
                "поэтому",
                "значит",
                "следовательно",
                "thus",
                "therefore",
                "so the answer is",
                "вывод:",
                "заключение:",
                "рекомендую",
                "следует",
                "must be",
                "clearly",
                "окончательный выбор");
    }

    private static boolean hasMitigation(String t) {
        return has(t,
                "желаемое не доказательство",
                "хотелось бы не значит что так",
                "независимо от того что хочу",
                "wishful thinking",
                "motivated reasoning",
                "desired outcome is not evidence",
                "preference is not probability",
                "желание не делает вероятным");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "wishful thinking",
                "motivated reasoning",
                "желаемое мышление",
                "мотивированное рассуждение",
                "хотелось бы верить но");
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
