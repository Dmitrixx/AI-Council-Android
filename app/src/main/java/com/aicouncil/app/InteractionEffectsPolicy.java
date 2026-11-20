package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local interaction-effects / non-additivity signals.
 * Flags a verdict that treats combined actions, simultaneous interventions or scaled packages
 * as the simple sum of separate effects without stating interaction terms, synergies, interference,
 * thresholds, or why effects remain approximately additive.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on additive decomposition when the decision packages
 * multiple levers or crosses a threshold where interactions become material.
 * Distinct from pure marginal analysis (intensive margin of one lever) and from causal oversimplification:
 * this focuses on the joint effect of several concurrent changes.
 */
public final class InteractionEffectsPolicy {
    private static final int MAX_SIGNALS = 2;

    private InteractionEffectsPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesOnAdditivePackage(a) || closesOnAdditivePackage(b),
                "interaction effects: вердикт закрывает на пакете действий как на сумме отдельных эффектов без синергии, помехи или порога");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на аддитивной сумме, не назвав взаимодействие или нелинейность совместного эффекта");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "interaction effects (possible signal, requires context): " + core;
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

    private static boolean acknowledgesInteraction(String t) {
        return has(t,
                "interaction", "взаимодействие", "синергия", "synergy",
                "interference", "помеха", "нелинейн", "non-linear", "nonlinear",
                "threshold", "порог", "joint effect", "совместный эффект",
                "не просто сумма", "not just the sum", "multiplicative",
                "мультипликатив", "cross term", "перекрёстный");
    }

    private static boolean closesOnAdditivePackage(String t) {
        boolean packageLang = has(t,
                "вместе", "в комплексе", "пакет мер", "package of", "combined",
                "одновременно", "simultaneously", "и то и другое",
                "оба шага", "both measures", "набор действий",
                "полный пакет", "full package", "все меры сразу");
        boolean additiveClose = has(t,
                "сумма эффектов", "sum of effects", "просто сложить",
                "каждый даёт", "each contributes", "добавляем эффекты",
                "аддитивно", "additively", "без потери",
                "поэтому вместе лучше", "therefore the package",
                "закрываем на пакете", "the combination is best");
        return packageLang && additiveClose && !acknowledgesInteraction(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesOnAdditivePackage(a) && closesOnAdditivePackage(b);
    }
}
