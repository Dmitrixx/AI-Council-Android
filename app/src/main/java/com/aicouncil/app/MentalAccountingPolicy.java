package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local mental-accounting signals.
 * Flags a verdict that treats money or resources as non-fungible by label
 * (bonus, windfall, house money, sunk budget, "already allocated") and closes
 * the choice on that accounting without an overall portfolio, opportunity-cost,
 * or total-wealth view. Naming the gap is not itself an error.
 * The coordinator cannot close on the labeled pot alone.
 */
public final class MentalAccountingPolicy {
    private static final int MAX_SIGNALS = 2;

    private MentalAccountingPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, labeledPotWithoutPortfolio(a) || labeledPotWithoutPortfolio(b),
                "mental accounting: вердикт закрывает выбор ярлыком денег (бонус, windfall, house money, уже выделенный бюджет) без общего портфеля, альтернативной стоимости или суммарного богатства");
        add(conflicts, initial, bothAccount(a, b),
                "оба эксперта закрывают на учёте по ярлыку без независимой общей оценки ресурсов");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "mental accounting (possible signal, requires context): " + core;
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
                "mental accounting", "ментальный учёт", "ментальный учет",
                "fungible", "взаимозаменяем", "opportunity cost", "альтернативная стоимость",
                "overall portfolio", "общий портфель", "total wealth", "суммарное богатство",
                "money is fungible", "деньги взаимозаменяемы");
    }

    private static boolean labeledPotWithoutPortfolio(String t) {
        boolean labeled = has(t,
                "бонус", "премия", "windfall", "house money", "хаус мани",
                "уже выделен", "already allocated", "sunk budget", "бюджет уже",
                "free money", "бесплатные деньги", "не свои деньги",
                "из бонуса", "from the bonus", "play money");
        boolean decisive = has(t,
                "поэтому можно", "значит берём", "закрываем", "recommend",
                "therefore we can", "so we should take", "этого достаточно",
                "можно потратить", "можно рискнуть");
        boolean portfolio = has(t,
                "общий портфель", "overall wealth", "total resources",
                "альтернативн", "opportunity cost", "взаимозамен",
                "fungible", "весь капитал", "all money is the same");
        return labeled && decisive && !portfolio && !acknowledges(t);
    }

    private static boolean bothAccount(String a, String b) {
        return labeledPotWithoutPortfolio(a) && labeledPotWithoutPortfolio(b);
    }
}
