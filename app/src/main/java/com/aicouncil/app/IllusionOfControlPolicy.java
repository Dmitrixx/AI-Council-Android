package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local illusion-of-control signals.
 * Overestimating one's ability to control or determine outcomes of processes that are largely
 * random, multi-factor or externally driven ("I control the result", "if I do X it will definitely work",
 * "everything depends on me", "I can make it happen") without acknowledging randomness, base rates,
 * external constraints, skill vs luck distinction or alternative explanations, is a hypothesis, not evidence.
 * Distinct from overconfidence (general belief in own accuracy), optimism bias and planning fallacy.
 * Coordinator must not treat personal agency claims as settled without weighing uncontrollable factors.
 */
public final class IllusionOfControlPolicy {
    private static final int MAX_SIGNALS = 2;

    private IllusionOfControlPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "illusion of control: переоценка способности контролировать исход («я контролирую», «если я сделаю — точно сработает», «всё зависит от меня») без учёта случайности, внешних факторов или base rates");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта переоценивают личный контроль над исходом, игнорируя случайность и внешние ограничения");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "illusion of control (possible signal, requires context): " + core;
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
                "illusion of control", "иллюзия контроля",
                "randomness", "случайность",
                "luck vs skill", "удача против навыка",
                "external factors", "внешние факторы",
                "base rate", "базовая вероятность",
                "uncontrollable", "неконтролируемый",
                "stochastic", "стохастический",
                "outside my control", "вне моего контроля");
    }

    private static boolean naivePattern(String t) {
        boolean control = has(t,
                "я контролирую", "всё зависит от меня",
                "если я сделаю", "точно сработает",
                "я могу сделать так", "под моим контролем",
                "i control the outcome", "i can make it happen",
                "everything depends on me", "under my control",
                "if i do this it will definitely",
                "я определяю результат", "полностью в моих руках",
                "nothing can stop me if");
        return control && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
