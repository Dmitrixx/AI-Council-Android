package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Conservative offline reasoning-risk detectors (v2.36+; false compromise 2.44;
 * texas sharpshooter / relative privation / nirvana in 2.45; continuum/sorites + broader UK markers in 2.47; single-cause and whataboutism in 2.50; appeal-to-probability and complex-question in 2.80).
 * Signals are hypotheses that require context, not definitive fallacy verdicts.
 * Coordinator must rank these highest and must not average them away.
 */
public final class ReasoningFallacyPolicy {
    private static final int MAX_SIGNALS = 8;

    private ReasoningFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, appealToIgnorance(a) || appealToIgnorance(b),
                "апелляция к незнанию: отсутствие найденных доказательств принимается за доказательство ложности");
        // ... truncated for this example, but in real I need full
    }
}