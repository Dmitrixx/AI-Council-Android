package com.aicouncil.app;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Local false-precision signals.
 * A probability or chance quoted to hundredths (73.46%) without an interval,
 * a measurement process, or an explicit "approximately" is a hypothesis about
 * calibration, not a more accurate forecast than a range.
 */
public final class FalsePrecisionPolicy {
    private static final int MAX_SIGNALS = 2;
    private static final Pattern PRECISE = Pattern.compile(
            "(?i)(?:вероятност[а-яё]*|шанс[а-яё]*|probability|chance|likelihood|уверенност[а-яё]*)[^\\n%]{0,40}(\\d{1,3}[.,]\\d{2,})\\s*%");

    private FalsePrecisionPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "false precision: вероятность или шанс названы с точностью до сотых без диапазона, процесса измерения или явной приблизительности");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта дают точечную вероятность с лишними знаками и не сверяют интервал");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "false precision (possible signal, requires context): " + core;
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
                "false precision", "ложная точность", "spurious precision",
                "диапазон", "доверительн", "interval", "примерно", "около",
                "confidence interval", "не точнее диапазона");
    }

    private static boolean naivePattern(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return PRECISE.matcher(t).find();
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
