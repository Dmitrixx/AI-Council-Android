package com.aicouncil.app;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local woozle / citation-laundering policy.
 * Flags a verdict that treats a chain of citations, or a pile of papers that
 * repeat one claim, as independent confirmation. Naming the error is not itself
 * an error. Signals remain hypotheses that require context.
 */
public final class WoozleEffectPolicy {
    private static final int MAX_SIGNALS = 2;
    private static final Pattern COUNT = Pattern.compile(
            "(\\d{1,3})\\s+(источник\\p{L}*|стат\\p{L}*|ссыл\\p{L}*|цитат\\p{L}*|papers|studies|citations|sources|articles)");

    private WoozleEffectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "woozle: цепочка цитат или пачка статей закрыла вопрос без независимого наблюдения");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта считают повторные ссылки независимым подтверждением");
        add(conflicts, initial, hitA ^ hitB,
                "woozle: один эксперт закрыл вывод числом ссылок на одну работу");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "woozle (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        if (has(t,
                "раз все цитируют одно и то же, значит верно",
                "много статей ссылаются друг на друга, значит доказано",
                "литература сходится, поэтому вопрос закрыт",
                "цепочка ссылок достаточна чтобы закрыть вопрос",
                "цитируют все, значит это факт",
                "cited everywhere so it is established",
                "the literature agrees so the claim is settled",
                "dozens of papers cite it therefore it is true",
                "everyone cites the same study so it is replicated",
                "widely cited means independently confirmed",
                "a citation chain is enough to close")) {
            return true;
        }
        return piledCitations(t);
    }

    /** Four or more named sources treated as proof, with no primary observation. */
    private static boolean piledCitations(String t) {
        Matcher matcher = COUNT.matcher(t);
        int best = 0;
        while (matcher.find()) {
            try {
                best = Math.max(best, Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException ignored) {
                // The pattern only matches 1–3 digits.
            }
        }
        if (best < 4) return false;
        return has(t, "подтвержд", "доказан", "вопрос закрыт", "settled", "established", "значит верно", "therefore true");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "woozle",
                "эффект вузла",
                "цепочка цитирования не независим",
                "повторные ссылки на один источник не репликация",
                "citation chain is not independent",
                "citations collapse to one primary",
                "первоисточник не прослежен",
                "сходятся к одному первоисточнику",
                "not independent confirmation");
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
