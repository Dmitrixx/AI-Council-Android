package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local principal–agent signals.
 * A recommendation that pays the advisor is not the same as one that pays the decision-maker.
 * Hypotheses requiring context, not verdicts. The coordinator must not average them away.
 */
public final class PrincipalAgentPolicy {
    private static final int MAX_SIGNALS = 3;

    private PrincipalAgentPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, advisorPayoffDrives(a) || advisorPayoffDrives(b),
                "интерес советчика: рекомендация опирается на комиссию, бонус или план продаж, а выгода того, кто решает, не отделена");
        add(conflicts, initial, bothServeTheAgent(a, b),
                "оба объясняют выбор выгодой продавца или агента и не называют, кто несёт издержки");
        add(conflicts, initial, hidesAsymmetry(a) || hidesAsymmetry(b),
                "асимметрия: предлагают не раскрывать условие, потому что другая сторона не заметит");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "principal-agent (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(42, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (affirmed(text, word)) return true;
        return false;
    }

    /** Phrase counts only when it is not immediately negated. */
    private static boolean affirmed(String text, String phrase) {
        int from = 0;
        while (from < text.length()) {
            int i = text.indexOf(phrase, from);
            if (i < 0) return false;
            int start = Math.max(0, i - 4);
            String prefix = text.substring(start, i);
            if (!prefix.endsWith("не ") && !prefix.endsWith("not ")) return true;
            from = i + Math.max(1, phrase.length());
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean separated(String t) {
        return has(t,
                "конфликт интересов", "conflict of interest", "интерес клиента", "выгода клиента",
                "кто платит", "издержки клиента", "платит заказчик", "выгода заказчика",
                "interest of the client", "client's payoff");
    }

    private static boolean advisorMotive(String t) {
        return has(t,
                "наша комиссия", "моя комиссия", "мой бонус", "наш бонус", "план продаж",
                "нам выгоднее продать", "выгодно продавцу", "выгода агента", "менеджеру выгодно",
                "our commission", "sales quota", "advisor's bonus");
    }

    private static boolean recommends(String t) {
        return has(t,
                "рекомендую", "стоит взять", "берите", "покупайте", "следует купить",
                "нужно купить", "i recommend", "you should buy");
    }

    private static boolean advisorPayoffDrives(String t) {
        return advisorMotive(t) && recommends(t) && !separated(t);
    }

    private static boolean bothServeTheAgent(String a, String b) {
        return advisorMotive(a) && advisorMotive(b) && !separated(a) && !separated(b);
    }

    private static boolean hidesAsymmetry(String t) {
        boolean hidden = has(t,
                "клиент не заметит", "они не узнают", "можно не говорить", "не обязательно раскрывать",
                "they won't notice", "no need to disclose");
        boolean disclose = has(t, "нужно раскрыть", "следует раскрыть", "must disclose", "раскрыть конфликт");
        return hidden && !disclose;
    }
}
