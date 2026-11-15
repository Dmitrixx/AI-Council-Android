package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local illusion-of-validity signals.
 * Flags a verdict that treats a coherent, confident, internally consistent story
 * or model as decisive evidence of correctness or predictive power without
 * stating out-of-sample checks, base rates, alternative narratives, or
 * calibration against outcomes. Naming the gap is not itself an error.
 * The coordinator cannot close on narrative coherence or confidence alone.
 */
public final class IllusionOfValidityPolicy {
    private static final int MAX_SIGNALS = 2;

    private IllusionOfValidityPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, coherentWithoutValidation(a) || coherentWithoutValidation(b),
                "illusion of validity: вердикт закрывает выбор связной/уверенной историей или моделью без упоминания вневыборочной проверки, базовых частот, альтернативных нарративов или калибровки по исходам");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на когерентности нарратива без независимой валидации");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "illusion of validity (possible signal, requires context): " + core;
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
                "illusion of validity", "иллюзия валидности",
                "out of sample", "вне выборки", "вневыборочн",
                "base rate", "базовая частота",
                "alternative narrative", "альтернативный нарратив",
                "calibration", "калибровка",
                "predictive accuracy", "предсказательная точность");
    }

    private static boolean coherentWithoutValidation(String t) {
        boolean coherent = has(t,
                "связная история", "логичная картина", "всё сходится",
                "coherent story", "makes sense", "internally consistent",
                "модель объясняет", "the model fits", "confident prediction",
                "уверенный прогноз", "ясно что");
        boolean decisive = has(t,
                "поэтому выбираем", "значит верно",
                "therefore we should", "so this is correct",
                "recommend", "закрываем", "proves",
                "доказывает");
        boolean mentionsValidation = has(t,
                "вневыборочн", "out-of-sample", "base rate", "базовая",
                "альтернатив", "alternative", "калибр", "calibration",
                "точность прогноз", "predictive");
        return coherent && decisive && !mentionsValidation && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return coherentWithoutValidation(a) && coherentWithoutValidation(b);
    }
}
