package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local slippery-slope signals.
 * Claiming that a modest first step will inevitably lead to extreme or disastrous
 * consequences without justifying each intermediate step, probability, or exit
 * ramps ("if we allow this small thing, then soon...", "один шаг — и дальше уже
 * не остановить", "thin end of the wedge") is a hypothesis, not evidence.
 * Distinct from false dilemma (binary choice), normalcy bias (underestimating
 * change), and tail-risk analysis (which requires explicit base rates and
 * mechanisms). Coordinator must not treat an unargued cascade as sufficient
 * reason to reject a limited proposal, nor treat the absence of a cascade
 * argument as proof the proposal is safe.
 */
public final class SlipperySlopePolicy {
    private static final int MAX_SIGNALS = 2;

    private SlipperySlopePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "slippery slope: скромный первый шаг без обоснования промежуточных звеньев и вероятностей объявляется неизбежным путём к экстремуму");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта отвергают ограниченный вариант из-за неуточнённого скользкого склона, а не по качеству самого шага");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "slippery slope (possible signal, requires context): " + core;
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
                "slippery slope",
                "скользкий склон",
                "thin end of the wedge",
                "каждый шаг требует отдельного обоснования",
                "промежуточные звенья не доказаны",
                "probability of the cascade is low",
                "there are exit ramps",
                "не утверждаю неизбежность цепочки");
    }

    private static boolean naivePattern(String t) {
        boolean cascadeClaim = has(t,
                "если разрешим это, то дальше",
                "один маленький шаг, и потом уже",
                "сначала это, потом то, потом катастрофа",
                "нельзя начать, иначе не остановить",
                "тонкий конец клина",
                "if we allow this small thing, then soon",
                "once we start down this path",
                "the thin end of the wedge",
                "first this, then that, then disaster",
                "there is no stopping once we begin",
                "открыть дверь — и пойдёт лавина",
                "разрешить одно — разрешат всё");
        boolean extremeOutcome = has(t,
                "приведёт к катастрофе",
                "закончится диктатурой",
                "уничтожит всё",
                "will lead to total",
                "ends in disaster",
                "inevitably causes collapse",
                "потянет за собой крайности");
        boolean noMechanism = !has(t,
                "каждый промежуточный шаг",
                "вероятность перехода",
                "есть точки выхода",
                "механизм цепочки",
                "intermediate steps are justified",
                "base rate of the cascade",
                "exit ramps exist");
        return cascadeClaim && extremeOutcome && noMechanism && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
