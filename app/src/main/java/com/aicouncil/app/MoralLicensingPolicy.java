package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local moral-licensing policy.
 * Flags a verdict that treats past good acts, donations, identity or prior restraint
 * as a license that makes the current questionable choice acceptable.
 * Naming the error is not the error. Signals remain hypotheses that require context.
 */
public final class MoralLicensingPolicy {
    private static final int MAX_SIGNALS = 2;

    private MoralLicensingPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "moral licensing: прошлое доброе дело или идентичность закрыли текущий выбор");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта оправдывают шаг прошлым заслугами или «мы хорошие»");
        add(conflicts, initial, hitA ^ hitB,
                "moral licensing: один эксперт закрывает выбор лицензией от прошлого");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "moral licensing (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "мы уже сделали доброе дело поэтому можно",
                "я пожертвовал значит теперь можно",
                "раз мы хорошие люди значит шаг допустим",
                "мы уже сдержали себя раньше поэтому сейчас можно",
                "прошлые заслуги оправдывают этот выбор",
                "идентичность хороших людей закрывает вопрос",
                "мы достаточно сделали поэтому можно не считать вред",
                "we already did good so this is fine",
                "i donated so now it is okay",
                "because we are good people the choice is acceptable",
                "past restraint licenses the present choice",
                "our prior good acts justify this step",
                "moral license from earlier virtue",
                "we have earned the right to do this");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "moral licensing",
                "моральное лицензирование",
                "прошлое доброе дело не оправдывает текущий вред",
                "заслуги не закрывают оценку шага",
                "past good acts do not license present harm",
                "identity is not a free pass",
                "prior restraint does not justify the next risk");
    }

    private static boolean has(String text, String... phrases) {
        if (text == null) return false;
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
