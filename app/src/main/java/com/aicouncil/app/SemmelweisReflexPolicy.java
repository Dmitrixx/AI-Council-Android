package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local Semmelweis-reflex signals.
 * Rejecting or dismissing new evidence / a new observation because it conflicts
 * with the established paradigm, orthodoxy or "how things are done" — without
 * independent evaluation of the evidence itself — is a hypothesis, not a verdict.
 * Distinct from confirmation bias (preferring supporting data), reactive
 * devaluation (source of the idea), straw man (caricature) and status-quo bias
 * (preference for the current state): here the trigger is that the claim challenges
 * the accepted model or authority of the field.
 * Coordinator must not treat "this contradicts the standard view" as sufficient
 * reason to discard the observation.
 */
public final class SemmelweisReflexPolicy {
    private static final int MAX_SIGNALS = 2;

    private SemmelweisReflexPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignores(a) || ignores(b),
                "semmelweis-reflex: новые данные / наблюдение отвергаются потому что противоречат устоявшейся парадигме, без независимой оценки");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта отбрасывают disconfirming evidence из-за конфликта с принятой моделью, а не по качеству данных");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "semmelweis-reflex (possible signal, requires context): " + core;
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
                "semmelweis", "семмельвейс",
                "парадигма может быть неполной", "парадигма может ошибаться",
                "устоявшаяся модель не священна", "ортодоксия не доказательство",
                "contradicts the paradigm but the data",
                "evidence against the accepted model",
                "данные против принятой модели",
                "независимо от того, что принято считать");
    }

    private static boolean naivePattern(String t) {
        boolean rejectBecauseOrthodoxy = has(t,
                "это противоречит принятой",
                "это противоречит устоявшейся",
                "это противоречит стандартной",
                "это не соответствует парадигме",
                "так не бывает в нашей области",
                "это опровергает всё, что мы знаем",
                "это идёт против orthodoxy",
                "this contradicts the established",
                "this goes against the accepted",
                "this challenges the standard model",
                "это не может быть, потому что все знают",
                "это невозможно, раз наука говорит иначе",
                "невозможно, потому что противоречит",
                "отвергаем, потому что не вписывается в",
                "discard because it contradicts the paradigm",
                "reject because it conflicts with the orthodoxy",
                "это ересь относительно",
                "это против канона");
        boolean paradigmDefense = has(t,
                "устоявшаяся парадигма",
                "принятая модель",
                "стандартный взгляд",
                "общепринятое мнение в науке",
                "the established paradigm",
                "the accepted model",
                "the orthodoxy of the field",
                "consensus science says",
                "как принято считать в отрасли");
        boolean dismissWithoutEvaluation = has(t,
                "поэтому можно не рассматривать",
                "поэтому отбрасываем",
                "поэтому игнорируем",
                "не стоит принимать всерьёз",
                "можно сразу отвергнуть",
                "therefore we can dismiss",
                "so we can ignore",
                "no need to take seriously");
        boolean combo = paradigmDefense && dismissWithoutEvaluation;
        return (rejectBecauseOrthodoxy || combo) && !acknowledges(t);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }
}
