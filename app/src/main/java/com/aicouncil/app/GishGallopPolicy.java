package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local Gish gallop / proof-by-verbosity policy.
 * Detects a conclusion treated as proven because many unrelated claims were listed,
 * or because the other side cannot answer every point at once.
 * A long list that still cites a reason, a source or a number is not the error.
 * Naming the error is not the error. Signals remain hypotheses that require context.
 */
public final class GishGallopPolicy {
    private static final int MAX_SIGNALS = 2;
    private static final int PILE = 6;

    private GishGallopPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        boolean hitA = commits(a);
        boolean hitB = commits(b);
        add(conflicts, initial, hitA || hitB,
                "gish gallop: перечень утверждений выдан за доказательство");
        add(conflicts, initial, hitA && hitB,
                "оба эксперта закрывают вопрос потоком несвязанных пунктов");
        add(conflicts, initial, hitA ^ hitB,
                "gish gallop: один эксперт подменяет довод длинным списком");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "gish gallop (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean commits(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        return has(t,
                "и ещё одно доказательство",
                "и еще одно доказательство",
                "столько аргументов что не ответить",
                "вы не сможете ответить на все пункты",
                "раз пунктов много значит верно",
                "этот список доказывает",
                "поток утверждений доказывает",
                "невозможно опровергнуть каждый пункт поэтому тезис верен",
                "and another thing that proves",
                "you can't answer all of these so i'm right",
                "too many points to refute therefore true",
                "this list of claims proves",
                "quantity of arguments proves",
                "proof by verbosity")
                || piled(t);
    }

    /** Six or more bare bullets, no warrant, closed as if the pile were proof. */
    private static boolean piled(String t) {
        int bullets = 0;
        int warrants = 0;
        String[] lines = t.split("\\n");
        for (String raw : lines) {
            String line = raw.trim();
            if (line.matches("^(\\d+[\\).]|[-•*])\\s+.+")) bullets++;
            if (has(line, "потому", "because", "источник", "http", "source",
                    "расчёт", "расчет", "исследован", "base rate", "базовая частота", "%")) {
                warrants++;
            }
        }
        if (bullets < PILE || warrants > 0) return false;
        return has(t,
                "поэтому верно",
                "значит это правда",
                "therefore this is true",
                "so the claim is true",
                "этот перечень доказывает",
                "these points prove");
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "gish gallop",
                "поток утверждений не доказательство",
                "количество тезисов не доказательство",
                "a flood of claims is not proof",
                "verbosity is not evidence",
                "несвязанные пункты не закрывают вопрос",
                "поток несвязанных утверждений не сильнее",
                "a long list is not a stronger argument");
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
