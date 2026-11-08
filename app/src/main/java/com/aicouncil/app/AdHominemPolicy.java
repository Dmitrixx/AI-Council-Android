package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local ad-hominem signals.
 * Attacking the person (character, motives, credentials in a personal way, "you are X so your argument is invalid")
 * instead of addressing the substance of the claim is a hypothesis, not evidence.
 * Distinct from authority bias (relying on credentials as positive support), reactive devaluation
 * (source of the idea), and straw man (distorting the argument itself).
 * Coordinator must not treat personal attacks as sufficient to dismiss an argument, nor treat
 * the absence of personal attacks as proof the argument is sound.
 */
public final class AdHominemPolicy {
    private static final int MAX_SIGNALS = 2;

    private AdHominemPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "ad hominem: атака на человека (мотив, характер, «ты такой-то, поэтому аргумент неверен») вместо разбора сути утверждения");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта отвергают позицию через личные нападки, а не по качеству аргументов");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "ad hominem (possible signal, requires context): " + core;
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
                "ad hominem",
                "личный выпад",
                "атака на личность",
                "не рассматриваю мотивы как доказательство",
                "character attack is not evidence",
                "addressing the argument, not the person");
    }

    private static boolean naivePattern(String t) {
        boolean personalAttack = has(t,
                "ты просто хочешь",
                "ему нельзя верить потому что он",
                "она предвзята из-за",
                "ты такой-то, поэтому",
                "его мнение не считается, он же",
                "you can't trust him because he is",
                "she's biased because of her",
                "your argument is invalid because you are",
                "he has an agenda so",
                "don't listen to him, he's just",
                "личный интерес делает аргумент ложным",
                "мотив дискредитирует позицию");
        boolean noSubstance = !has(t,
                "разберём сам аргумент",
                "независимо от личности",
                "substance of the claim",
                "addressing the evidence",
                "the logic stands or falls on",
                "игнорирую личность, смотрю на факты");
        return personalAttack && noSubstance && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
