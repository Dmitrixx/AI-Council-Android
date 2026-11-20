package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local alternative models / model pluralism check.
 * Flags a verdict that treats one preferred model, mechanism, or explanatory framework
 * as sufficient to close the decision without naming alternative models, checking
 * whether the recommendation is robust across plausible models, or noting material
 * model disagreement.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on a single model when model uncertainty is material.
 */
public final class AlternativeModelsPolicy {
    private static final int MAX_SIGNALS = 2;

    private AlternativeModelsPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesOnSingleModel(a) || closesOnSingleModel(b),
                "alternative models: вердикт закрывает на одном предпочтительном модели/механизме без упоминания альтернативных моделей, устойчивости вывода к выбору модели или разногласий между правдоподобными моделями");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на единственной модели, игнорируя модельную неопределённость и альтернативы");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "alternative models (possible signal, requires context): " + core;
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
                "alternative models", "альтернативные модели", "model uncertainty", "модельная неопределённость",
                "robust across models", "устойчиво к выбору модели", "models disagree", "модели расходятся",
                "multiple mechanisms", "несколько механизмов", "model averaging", "усреднение моделей",
                "if the true model is different", "если модель другая");
    }

    private static boolean closesOnSingleModel(String t) {
        boolean singleModelLang = has(t,
                "наша модель показывает", "the model clearly shows", "этот механизм объясняет",
                "единственная модель", "the correct model", "правильная модель",
                "no other explanation", "других объяснений нет", "model is settled");
        boolean closing = has(t,
                "поэтому закрываем", "therefore we close", "решение однозначно",
                "the choice is settled", "делаем так", "recommend closing");
        return singleModelLang && closing && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesOnSingleModel(a) && closesOnSingleModel(b);
    }
}
