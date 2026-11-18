package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local free-rider / commons signals.
 * Flags a verdict that treats a shared resource, public good or collective
 * effort as sustainable when individuals can take the benefit without bearing
 * the cost, without naming contribution, exclusion or enforcement. Naming the
 * gap is not itself an error.
 * The coordinator cannot close solely on voluntary contribution assumptions.
 */
public final class FreeRiderPolicy {
    private static final int MAX_SIGNALS = 2;

    private FreeRiderPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresFreeRide(a) || ignoresFreeRide(b),
                "free rider: вердикт закрывает общее благо так, будто добровольный вклад всех устойчив без исключения или принуждения");
        add(conflicts, initial, ignoresFreeRide(a) && ignoresFreeRide(b),
                "оба эксперта закрывают без учёта проблемы безбилетника");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "free rider (possible signal, requires context): " + core;
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
                "free rider", "безбилетник", "проблема безбилетника", "free-rider",
                "tragedy of the commons", "трагедия общин", "трагедія спільного",
                "contribution required", "нужен вклад", "exclusion", "исключение",
                "enforcement", "принуждение к взносу", "public good underprovided");
    }

    private static boolean ignoresFreeRide(String t) {
        boolean shared = has(t,
                "общее благо", "общего блага", "общим благом",
                "public good", "общий ресурс", "общего ресурса", "shared resource",
                "коллективное усилие", "коллективного усилия", "collective effort",
                "общая инфраструктура", "everyone benefits", "выгода для всех",
                "commons", "общинное", "общины");
        boolean voluntaryOk = has(t,
                "добровольно все внесут", "добровольных взносов хватит", "добровольный вклад",
                "everyone will contribute voluntarily", "voluntary contributions suffice",
                "никто не будет халявить", "no one will free ride",
                "все заплатят сами", "all will pay their share",
                "мотивации достаточно", "motivation is enough without rules");
        return shared && voluntaryOk && !acknowledges(t);
    }
}
