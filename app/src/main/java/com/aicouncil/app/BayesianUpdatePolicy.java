package com.aicouncil.app;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic Bayes update for explicit positive- or negative-test wording.
 * The app does not infer that nearby percentages are medical facts;
 * all three cues must sit next to a percent, or the note is withheld.
 * When the three numbers are present both positive and negative posteriors are reported.
 */
public final class BayesianUpdatePolicy {
    private static final Pattern PERCENT = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*%");

    private BayesianUpdatePolicy() {}

    public static String evaluateExplicitRequest(String task) {
        if (task == null || task.trim().isEmpty()) return "";
        Double prior = percentNear(task, "распростран", "априор", "prevalence", "base rate", "базовая частот", "базовой частот");
        Double sensitivity = percentNear(task, "чувствительн", "sensitivity", "true positive");
        Double specificity = percentNear(task, "специфичн", "specificity");
        Double falsePositive = percentNear(task, "ложноположитель", "false positive");
        if (specificity == null && falsePositive != null) specificity = 1.0 - falsePositive;
        if (prior == null || sensitivity == null || specificity == null) return "";
        if (!unit(prior) || !unit(sensitivity) || !unit(specificity)) return "";

        boolean wantsNegative = mentionsNegativeTest(task);
        boolean wantsPositive = mentionsPositiveTest(task);

        StringBuilder notes = new StringBuilder();
        String pos = positiveNote(prior, sensitivity, specificity);
        String neg = negativeNote(prior, sensitivity, specificity);
        if (!pos.isEmpty()) notes.append(pos);
        if (!neg.isEmpty()) {
            if (notes.length() > 0) notes.append("\n\n");
            notes.append(neg);
        }
        if (notes.length() == 0) return "";
        if (wantsNegative && !wantsPositive) {
            notes.append("\nЗапрос явно упоминал отрицательный тест — в первую очередь смотри блок для отрицательного.");
        } else if (wantsPositive && !wantsNegative) {
            notes.append("\nЗапрос явно упоминал положительный тест — в первую очередь смотри блок для положительного.");
        } else {
            notes.append("\nПолярность теста не указана явно или указаны обе — приведены оба расчёта.");
        }
        return notes.toString();
    }

    private static String positiveNote(double prior, double sensitivity, double specificity) {
        double miss = 1.0 - specificity;
        double evidence = sensitivity * prior + miss * (1.0 - prior);
        if (!(evidence > 1.0e-15) || !Double.isFinite(evidence)) return "";
        double posterior = (sensitivity * prior) / evidence;
        if (!Double.isFinite(posterior) || posterior < 0.0 || posterior > 1.0) return "";
        return "ДЕТЕРМИНИРОВАННОЕ БАЙЕСОВСКОЕ ОБНОВЛЕНИЕ (не ответ ИИ, только для положительного теста):\n"
                + "Априорная вероятность: " + pct(prior) + "\n"
                + "Чувствительность: " + pct(sensitivity) + "\n"
                + "Специфичность: " + pct(specificity) + "\n"
                + "P(гипотеза | положительный тест): " + pct(posterior) + "\n"
                + "Метод: теорема Байеса в приложении. Это не диагноз. Если проценты означают другое, число не применять и сказать об этом явно.";
    }

    private static String negativeNote(double prior, double sensitivity, double specificity) {
        // P(H|neg) = (1 - sens) * prior / [(1 - sens)*prior + specificity*(1 - prior)]
        double falseNeg = 1.0 - sensitivity;
        double evidence = falseNeg * prior + specificity * (1.0 - prior);
        if (!(evidence > 1.0e-15) || !Double.isFinite(evidence)) return "";
        double posterior = (falseNeg * prior) / evidence;
        if (!Double.isFinite(posterior) || posterior < 0.0 || posterior > 1.0) return "";
        return "ДЕТЕРМИНИРОВАННОЕ БАЙЕСОВСКОЕ ОБНОВЛЕНИЕ (не ответ ИИ, только для отрицательного теста):\n"
                + "Априорная вероятность: " + pct(prior) + "\n"
                + "Чувствительность: " + pct(sensitivity) + "\n"
                + "Специфичность: " + pct(specificity) + "\n"
                + "P(гипотеза | отрицательный тест): " + pct(posterior) + "\n"
                + "Метод: теорема Байеса в приложении. Это не диагноз. Если проценты означают другое, число не применять и сказать об этом явно.";
    }

    private static boolean mentionsPositiveTest(String task) {
        String lower = task.toLowerCase(Locale.ROOT);
        return lower.contains("положительн") || lower.contains("positive test")
                || lower.contains("тест положителен") || lower.contains("test is positive")
                || lower.contains("позитивний тест") || lower.contains("тест позитивний");
    }

    private static boolean mentionsNegativeTest(String task) {
        String lower = task.toLowerCase(Locale.ROOT);
        return lower.contains("отрицательн") || lower.contains("negative test")
                || lower.contains("тест отрицателен") || lower.contains("test is negative")
                || lower.contains("негативний тест") || lower.contains("тест негативний");
    }

    private static boolean unit(double value) {
        return Double.isFinite(value) && value >= 0.0 && value <= 1.0;
    }

    private static String pct(double value) {
        return String.format(Locale.ROOT, "%.2f%%", value * 100.0);
    }

    private static Double percentNear(String text, String... cues) {
        String lower = text.toLowerCase(Locale.ROOT);
        for (int i = 0; i < cues.length; i++) {
            int at = lower.indexOf(cues[i]);
            if (at < 0) continue;
            Double forward = closestPercent(text, at + cues[i].length(), Math.min(text.length(), at + cues[i].length() + 16), true);
            if (forward != null) return forward;
            Double backward = closestPercent(text, Math.max(0, at - 6), at, false);
            if (backward != null) return backward;
        }
        return null;
    }

    private static Double closestPercent(String text, int from, int to, boolean stopAtClause) {
        if (to <= from) return null;
        String slice = text.substring(from, to);
        if (stopAtClause) {
            int cut = indexOfClauseComma(slice);
            if (cut >= 0) slice = slice.substring(0, cut);
        }
        Matcher matcher = PERCENT.matcher(slice);
        if (!matcher.find()) return null;
        double raw = Double.parseDouble(matcher.group(1).replace(',', '.'));
        if (!Double.isFinite(raw) || raw < 0.0 || raw > 100.0) return null;
        return raw / 100.0;
    }

    /** Comma that separates a clause, not a decimal comma inside 1,5%. */
    private static int indexOfClauseComma(String slice) {
        for (int i = 0; i < slice.length(); i++) {
            if (slice.charAt(i) != ',') continue;
            char next = i + 1 < slice.length() ? slice.charAt(i + 1) : ' ';
            if (next >= '0' && next <= '9') continue;
            return i;
        }
        return -1;
    }
}
