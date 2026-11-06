package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local naive realism signals.
 * Assuming one's own perception of reality is objective and that those who disagree
 * are uninformed, irrational, or biased (without acknowledging that one's own view
 * may also be shaped by perspective, incentives, or incomplete information)
 * is a hypothesis, not evidence. Distinct from false consensus (projection of prevalence),
 * belief bias (desirability), and authority bias.
 * Coordinator must not treat "they just don't see the facts" as settled.
 */
public final class NaiveRealismPolicy {
    private static final int MAX_SIGNALS = 2;

    private NaiveRealismPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "naive realism: своя позиция объективна, несогласные невидящие/иррациональные без признания собственной перспективы");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта считают свою картину объективной, а расхождение — просто непониманием фактов");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "naive realism (возможный сигнал, требует контекста): " + core;
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
                "naive realism", "наивный реализм",
                "моя перспектива тоже", "my perspective is also",
                "возможно, я тоже не вижу всего", "i may also miss",
                "оба взгляда могут быть частичными", "both views may be partial",
                "разные рамки", "different frames of reference",
                "не только они не видят");
    }

    private static boolean naivePattern(String t) {
        boolean selfObjective = has(t,
                "это объективные факты", "это просто факты", "любой видит",
                "очевидно любому", "как можно не видеть",
                "the facts are clear", "anyone can see", "objectively true",
                "они просто не понимают факты", "they just don't see the facts",
                "они игнорируют реальность", "they ignore reality",
                "иррационально не соглашаться", "irrational to disagree",
                "только невидящий может так думать", "only the uninformed think that");
        boolean dismissesOther = has(t,
                "они просто не в теме", "они не понимают ситуацию",
                "they don't understand the situation", "they lack the context",
                "это их предвзятость", "it's their bias",
                "они предвзяты", "they are biased"
        ) && !has(t, "и я тоже", "and i may be");

        return (selfObjective || dismissesOther) && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
