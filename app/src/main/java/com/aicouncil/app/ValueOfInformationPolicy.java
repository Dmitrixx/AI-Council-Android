package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local value-of-information signals.
 * Flags a verdict that treats immediate action (or immediate inaction) as decisive
 * without stating the cost and expected value of gathering additional information,
 * whether that information could change the decision, reversibility, or the opportunity
 * cost of delay. Naming the gap is not itself an error.
 * The coordinator cannot close on "act now" or "wait forever" alone without an information-value check.
 */
public final class ValueOfInformationPolicy {
    private static final int MAX_SIGNALS = 2;

    private ValueOfInformationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, actNowWithoutVoI(a) || actNowWithoutVoI(b),
                "value of information: вердикт закрывает выбор немедленным действием без оценки стоимости и ценности дополнительной информации, возможности изменения решения или обратимости");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на немедленном действии без независимой оценки value of information");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "value of information (possible signal, requires context): " + core;
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
                "value of information", "ценность информации", "voi",
                "expected value of information", "математическое ожидание информации",
                "cost of delay", "стоимость задержки",
                "information could change", "информация может изменить",
                "reversible", "обратим");
    }

    private static boolean actNowWithoutVoI(String t) {
        boolean actNow = has(t,
                "действовать сейчас", "немедленно", "без отлагательств", "срочно решать",
                "act immediately", "do it now", "no time to wait", "decide right away",
                "закрываем сейчас", "выбираем без дополнительных данных",
                "enough information already", "достаточно информации");
        boolean decisive = has(t,
                "поэтому делаем", "значит надо",
                "therefore we should", "so we act",
                "recommend acting now", "закрываем вопрос");
        boolean mentionsVoI = has(t,
                "value of information", "ценность доп информации",
                "cost of more data", "стоимость сбора данных",
                "could change the decision", "может изменить решение",
                "reversible action", "обратимое действие",
                "opportunity cost of delay", "альтернативная стоимость ожидания");
        return actNow && decisive && !mentionsVoI && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return actNowWithoutVoI(a) && actNowWithoutVoI(b);
    }
}
