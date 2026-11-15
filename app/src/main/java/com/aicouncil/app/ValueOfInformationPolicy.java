package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local value-of-information signals.
 * Flags a verdict that closes the decision under residual uncertainty
 * (or claims "enough information") when additional cheap/decision-changing
 * information is available, without stating the expected value of information,
 * the cost of obtaining it, the potential for new data to reverse the choice,
 * or that further information would not change the decision.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely because more data is unavailable or
 * "we already know enough" without addressing VoI.
 */
public final class ValueOfInformationPolicy {
    private static final int MAX_SIGNALS = 2;

    private ValueOfInformationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesWithoutVoI(a) || closesWithoutVoI(b),
                "value of information: вердикт закрывает выбор при остаточной неопределённости или «достаточно информации» без оценки ожидаемой ценности дополнительной информации, стоимости её получения или того, что новые данные могли бы изменить решение");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают без учёта ценности информации при высокой неопределённости");
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
                "value of information", "ценность информации", "voi", "evpi",
                "expected value of information", "ожидаемая ценность информации",
                "cost of information", "стоимость информации",
                "more data would not change", "новые данные не изменят",
                "information is not decision-relevant", "информация не повлияет на решение");
    }

    private static boolean closesWithoutVoI(String t) {
        boolean uncertaintyOrEnough = has(t,
                "достаточно информации", "enough information", "no need for more data",
                "нет смысла ждать", "не нужно больше данных", "уже всё ясно",
                "we know enough", "further research not needed", "достаточно данных",
                "uncertainty remains but", "неопределённость есть, но решаем");
        boolean decisive = has(t,
                "поэтому выбираем", "значит решаем сейчас", "therefore we decide",
                "so we proceed", "recommend now", "закрываем вопрос",
                "нет времени на дополнительную информацию", "act now");
        boolean mentionsVoI = has(t,
                "ценность информации", "value of information", "voi",
                "стоимость получения", "cost of waiting", "could reverse",
                "могло бы изменить решение", "не изменит выбор");
        return uncertaintyOrEnough && decisive && !mentionsVoI && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesWithoutVoI(a) && closesWithoutVoI(b);
    }
}
