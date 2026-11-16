package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local transportability / external-validity signals.
 * Flags a verdict that treats a result from another population, laboratory,
 * country, market or historical regime as decisive for this case without
 * saying why the mechanism transfers, or explicitly that transfer is unproven.
 * Naming the gap is not itself an error.
 * The coordinator cannot close by copying a foreign result unchanged.
 */
public final class ExternalValidityPolicy {
    private static final int MAX_SIGNALS = 2;

    private ExternalValidityPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesWithoutTransfer(a) || closesWithoutTransfer(b),
                "external validity: вердикт переносит результат другой популяции, лаборатории, страны или режима и закрывает этот случай без причины, почему механизм переносится");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают переносом чужого результата без проверки переносимости");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "external validity (possible signal, requires context): " + core;
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
                "external validity", "внешняя валидность", "transportability",
                "переносимость не проверена", "механизм может не перенестись",
                "механізм може не перенестися", "не переносится на этот случай",
                "другая популяция не та", "інша популяція");
    }

    private static boolean closesWithoutTransfer(String t) {
        boolean foreign = has(t,
                "результат лаборатории переносим", "результат лабораторії переносим",
                "в другой стране сработало", "в іншій країні спрацювало",
                "чужой кейс переносим как есть", "чужий кейс переносимо як є",
                "worked in their market so", "lab result transfers",
                "student sample decides for everyone", "исследование на студентах закрывает",
                "пилотный город решает для всей страны", "пілотне місто вирішує для всієї країни");
        boolean decisive = has(t,
                "поэтому внедряем как есть", "значит масштабируем без проверки",
                "therefore adopt unchanged", "закрываем перенос",
                "so we roll it out unchanged", "рекомендую копировать без адаптации",
                "тому впроваджуємо як є");
        return foreign && decisive && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesWithoutTransfer(a) && closesWithoutTransfer(b);
    }
}
