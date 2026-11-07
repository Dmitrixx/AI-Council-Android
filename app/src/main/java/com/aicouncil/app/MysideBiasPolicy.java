package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local myside / motivated-reasoning signals.
 * Keeping a preferred conclusion by discarding contrary evidence, or counting only
 * studies that support "our side", without a rule that would apply to friendly
 * evidence too, is a hypothesis, not a reason the conclusion is true.
 * Distinct from confirmation bias (seeking support) — this one actively voids the other side.
 */
public final class MysideBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private MysideBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "myside bias: вывод держат, отбрасывая данные против своей позиции или засчитывая только подтверждающие источники, без правила, которое применяется и к удобным доказательствам");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта защищают заранее выбранную сторону и не называют наблюдение, которое заставило бы её сменить");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "myside bias (possible signal, requires context): " + core;
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
                "myside", "мотивированное рассуждение", "предвзятость своей стороны",
                "disconfirming evidence", "данные против своей гипотезы",
                "правило применяется и к нашим источникам", "same standard for friendly evidence",
                "что заставило бы сменить сторону");
    }

    private static boolean naivePattern(String t) {
        boolean discard = has(t,
                "игнорируй доказательства против",
                "отбрось неудобные факты",
                "не считай данные которые противоречат",
                "не считай данные, которые противоречат",
                "только то что подтверждает нашу позицию",
                "только то, что подтверждает нашу позицию",
                "факты против нас не в счёт",
                "факты против нас не в счет",
                "ignore evidence that contradicts",
                "dismiss contrary evidence",
                "only evidence that supports our view",
                "only count evidence that supports",
                "sources against us are biased by definition",
                "источники против нас предвзяты по определению");
        return discard && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
