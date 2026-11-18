package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local path-dependence / lock-in signals.
 * Flags a verdict that treats the current technology, standard or institutional
 * path as the only viable option because it is already dominant, without
 * weighing switching costs, network effects or whether a better alternative
 * is blocked by history rather than merit. Naming the gap is not itself an error.
 * The coordinator cannot close solely because "this is how it already is".
 */
public final class PathDependencePolicy {
    private static final int MAX_SIGNALS = 2;

    private PathDependencePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresLockIn(a) || ignoresLockIn(b),
                "path dependence: вердикт закрывает выбор так, будто доминирующий путь единственно верен из-за истории, а не из-за качества");
        add(conflicts, initial, ignoresLockIn(a) && ignoresLockIn(b),
                "оба эксперта закрывают без учёта зависимости от пройденного пути");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "path dependence (possible signal, requires context): " + core;
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
                "path dependence", "зависимость от пути", "path-dependence",
                "lock-in", "lock in", "запирание", "switching cost", "стоимость переключения",
                "network effect", "сетевой эффект", "qwerty", "историческая случайность",
                "history not merit", "история а не качество");
    }

    private static boolean ignoresLockIn(String t) {
        boolean dominant = has(t,
                "уже стандарт", "already the standard", "все уже на этом",
                "everyone is already on this", "доминирует рынок", "market dominant",
                "переходить поздно", "too late to switch", "установленный путь",
                "established path", "так уже принято везде", "industry lock");
        boolean best = has(t,
                "значит лучший", "therefore the best", "иначе не выжил бы",
                "would not have survived otherwise", "рынок выбрал правильно",
                "market chose correctly", "альтернативы хуже раз уж не победили",
                "alternatives lost so they are worse", "победа доказывает превосходство");
        return dominant && best && !acknowledges(t);
    }
}
