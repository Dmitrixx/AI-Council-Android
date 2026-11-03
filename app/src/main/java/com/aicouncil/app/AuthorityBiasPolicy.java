package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local authority-bias / appeal-to-authority signals.
 * Flags when a conclusion rests primarily on status, title, "experts say", or source reputation
 * without addressing the underlying evidence, mechanisms, or base rates.
 * Hypotheses requiring context, not automatic verdicts. The coordinator must not treat them as definitive.
 */
public final class AuthorityBiasPolicy {
    private static final int MAX_SIGNALS = 3;

    private AuthorityBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, appealsToStatusWithoutSubstance(a) || appealsToStatusWithoutSubstance(b),
                "апелляция к авторитету: вывод опирается на статус, титул или «эксперты говорят» без разбора сути, механизма или базовых частот");
        add(conflicts, initial, bothRelyOnAuthority(a, b),
                "оба ответа опираются на авторитет без независимой проверки доказательств");
        add(conflicts, initial, dismissesByStatus(a) || dismissesByStatus(b),
                "отклонение аргумента только из-за статуса источника без разбора содержания");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "authority bias (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (affirmed(text, word)) return true;
        return false;
    }

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

    private static boolean addressesSubstance(String t) {
        return has(t,
                "доказательства", "механизм", "базовая частота", "данные показывают",
                "evidence", "mechanism", "base rate", "data show", "независимо от статуса",
                "проверяем суть", "substance");
    }

    private static boolean appealsToStatusWithoutSubstance(String t) {
        boolean authority = has(t,
                "эксперты говорят", "по мнению экспертов", "авторитет говорит",
                "известный специалист", "experts agree", "according to experts",
                "профессор сказал", "нобелевский лауреат", "авторитетный источник");
        boolean conclude = has(t, "следовательно", "значит", "рекомендую", "therefore", "i recommend", "вывод");
        return authority && conclude && !addressesSubstance(t);
    }

    private static boolean bothRelyOnAuthority(String a, String b) {
        return appealsToStatusWithoutSubstance(a) && appealsToStatusWithoutSubstance(b);
    }

    private static boolean dismissesByStatus(String t) {
        boolean dismiss = has(t,
                "не эксперт", "нет авторитета", "любитель", "not an expert",
                "нет образования в этой области", "unqualified source");
        boolean noSubstance = !addressesSubstance(t);
        return dismiss && has(t, "поэтому неверно", "можно игнорировать", "therefore false") && noSubstance;
    }
}
