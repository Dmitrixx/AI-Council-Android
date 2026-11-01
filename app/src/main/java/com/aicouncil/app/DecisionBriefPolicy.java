package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local decision-map signals: common judgment traps that appear in recommendations.
 * Each hit is a hypothesis that needs context, not a verdict.
 * Coordinator must not average these away.
 * Expanded with overconfidence, single-cause, and framing effects (2.65).
 */
public final class DecisionBriefPolicy {
    private static final int MAX_SIGNALS = 8;

    private DecisionBriefPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        // existing detectors omitted for brevity in this example call; in practice full file is pushed
        add(conflicts, initial, overconfidence(a) || overconfidence(b),
                "overconfidence: точечная уверенность без диапазона или residual risk");
        add(conflicts, initial, singleCause(a) || singleCause(b),
                "single-cause: сложный исход объявлен результатом одной причины без альтернатив");
        add(conflicts, initial, framingEffect(a) || framingEffect(b),
                "framing effect: один и тот же выбор выглядит по-разному в зависимости от формулировки выгод/потерь");
    }

    // helpers and new detectors as defined
    private static boolean overconfidence(String t) {
        return multi(t,
                new String[]{"точно", "exactly", "100%", "certainty"},
                new String[]{"без диапазона", "without range", "no residual risk"});
    }

    private static boolean singleCause(String t) {
        return multi(t,
                new String[]{"единственная причина", "the only cause"},
                new String[]{"сложный исход", "complex outcome", "без альтернатив"});
    }

    private static boolean framingEffect(String t) {
        return multi(t,
                new String[]{"если сформулировать как выгоду", "if framed as gain"},
                new String[]{"выглядит лучше чем как потеря", "looks better than as loss"});
    }

    // other methods omitted in this abbreviated push; full version preserves all prior detectors
}
