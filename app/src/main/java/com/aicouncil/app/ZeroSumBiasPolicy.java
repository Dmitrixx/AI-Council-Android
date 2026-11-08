package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local zero-sum bias signals.
 * Treating a situation as strictly zero-sum ("if they gain, we lose", "only one can win",
 * "pie is fixed", "their success is our failure") when gains can be mutual, expanding or
 * independent, without considering positive-sum outcomes, cooperation surplus or non-rival
 * goods, is a hypothesis, not evidence.
 * Distinct from competition framing, scarcity heuristics and principal-agent conflict:
 * here the structure of payoffs itself is assumed fixed and antagonistic without support.
 * Coordinator must not treat "winner takes all" language as settled without checking for
 * expanding pie, mutual benefit or independent dimensions of value.
 */
public final class ZeroSumBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private ZeroSumBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "zero-sum bias: трактует ситуацию как игру с нулевой суммой («если они выигрывают — мы проигрываем», «пирог фиксирован») без учёта взаимной выгоды или расширяемого пирога");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта предполагают строго антагонистический fixed-pie исход без positive-sum альтернатив");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "zero-sum bias (possible signal, requires context): " + core;
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
                "positive-sum", "positive sum", "non-zero-sum",
                "взаимная выгода", "взаимная польза",
                "расширяемый пирог", "пирог можно увеличить",
                "win-win", "оба выигрывают",
                "не соперничество", "не конкуренция за один ресурс",
                "независимые измерения", "независимые оси выгоды",
                "cooperation surplus", "surplus from cooperation");
    }

    private static boolean naivePattern(String t) {
        boolean zeroSum = has(t,
                "если они выигрывают", "их выигрыш — наш проигрыш",
                "пирог фиксирован", "пирог один",
                "только один может выиграть", "winner takes all",
                "их успех — наша неудача", "их успех наша неудача",
                "zero-sum", "zero sum", "нулевая сумма",
                "игра с нулевой суммой", "строго антагонистич",
                "если они получают — мы теряем",
                "fixed pie", "the pie is fixed",
                "someone has to lose", "кто-то должен проиграть",
                "нельзя выиграть обоим", "нельзя чтобы выиграли оба");
        return zeroSum && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
