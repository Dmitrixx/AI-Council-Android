package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local belief-bias and motivated-reasoning signals.
 * A conclusion that merely sounds plausible, or that the speaker wants to be true,
 * is not evidence that the argument is valid. An unwelcome conclusion is not
 * evidence that the argument is invalid. Hypotheses, not verdicts.
 */
public final class BeliefBiasPolicy {
    private static final int MAX_SIGNALS = 2;

    private BeliefBiasPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "belief bias: правдоподобный, желанный или неприятный вывод принят за доказательство валидности аргумента");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта закрывают вывод правдоподобием или желанием, а не отдельной проверкой аргумента");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "belief bias (возможный сигнал, требует контекста): " + core;
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
                "belief bias", "мотивированное рассуждение", "желание не доказательство",
                "правдоподобие не доказательство", "приятный вывод не делает",
                "wanting it true is not evidence", "believability is not validity",
                "правдоподобность не валидность", "не потому что хочется",
                "не потому что звучит правдоподобно");
    }

    private static boolean plausibilityAsProof(String t) {
        return has(t,
                "звучит правдоподобно, значит", "раз правдоподобно, значит",
                "правдоподобно — значит доказано", "правдоподобно, значит аргумент",
                "sounds believable so", "sounds plausible, so the argument",
                "believable, therefore true", "sounds right so the logic",
                "звучит правильно, значит логика верна");
    }

    private static boolean implausibilityKillsLogic(String t) {
        return has(t,
                "неправдоподобно, значит рассуждение", "не может быть правдой, значит аргумент",
                "раз не верится, значит ложно", "unbelievable, so the argument",
                "too implausible, therefore the reasoning", "не верится, значит логика ложная");
    }

    private static boolean desireAsProof(String t) {
        return has(t,
                "хочу, чтобы это было правдой, значит", "мне так выгоднее, значит это верно",
                "приятный вывод, значит он верный", "i want it to be true, so it is",
                "because i need this to be true", "удобный вывод принимаю за доказанный",
                "иначе придётся признать ошибку, значит тезис",
                "the conclusion is welcome, so it is", "fits what we want, therefore true",
                "так лучше для нас, значит это правда");
    }

    private static boolean triggered(String t) {
        return plausibilityAsProof(t) || implausibilityKillsLogic(t) || desireAsProof(t);
    }

    private static boolean ignores(String t) {
        return triggered(t) && !acknowledges(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
