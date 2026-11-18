package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local winner's curse signals.
 * Flags a verdict that treats winning a competitive bid, auction, hiring race
 * or exclusive offer as proof the price or terms are fair, without asking
 * whether the win itself is evidence of overpayment relative to the common
 * value. Naming the gap is not itself an error.
 * The coordinator cannot close solely because the side that bid highest won.
 */
public final class WinnersCursePolicy {
    private static final int MAX_SIGNALS = 2;

    private WinnersCursePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresCurse(a) || ignoresCurse(b),
                "winner's curse: вердикт закрывает выбор так, будто победа в конкурсе/аукционе сама доказывает справедливую цену");
        add(conflicts, initial, ignoresCurse(a) && ignoresCurse(b),
                "оба эксперта закрывают без учёта проклятия победителя");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "winner's curse (possible signal, requires context): " + core;
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
                "winner's curse", "проклятие победителя", "прокляття переможця",
                "overbid", "overbidding", "common value", "общая ценность",
                "win implies overpayment", "победа значит переплата",
                "conditional on winning", "условно на победу",
                "с учётом проклятия", "accounting for the curse");
    }

    private static boolean ignoresCurse(String t) {
        boolean competitive = has(t,
                "выиграл аукцион", "выиграли аукцион", "won the auction", "highest bid", "наивысшая ставка",
                "перебил всех", "outbid everyone", "выиграл тендер", "выиграли тендер", "won the tender",
                "выбрали наше предложение", "our bid won", "выиграл конкурс", "выиграли конкурс",
                "won the hiring race", "предложили больше всех", "победа в аукционе", "победа в тендере");
        boolean fair = has(t,
                "значит цена справедлива", "цена справедлива", "price is fair", "не переплатили",
                "fair value", "рыночная цена подтверждена", "bid reflects true value",
                "победа доказывает", "winning proves the value", "winning proves the",
                "ставка соответствует ценности", "цена верная", "качество цены", "value is correct");
        return competitive && fair && !acknowledges(t);
    }
}
