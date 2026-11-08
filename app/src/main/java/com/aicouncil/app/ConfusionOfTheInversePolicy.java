package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local confusion-of-the-inverse / prosecutor's fallacy signals.
 * P(data|hypothesis), test accuracy or a random-match probability is treated as
 * P(hypothesis|data) without a base rate and a false-positive rate.
 * This is a hypothesis, not evidence. Naming the fallacy or supplying a base rate
 * is not itself the error. Coordinator must not copy accuracy into a posterior.
 */
public final class ConfusionOfTheInversePolicy {
    private static final int MAX_SIGNALS = 2;
    private static final Pattern PERCENT = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*%");
    private static final String[] ACCURACY = {
            "точност", "точен", "accuracy", "accurate", "чувствительн", "sensitivity",
            "специфичн", "specificity", "вероятность совпадения", "match probability", "random match"
    };
    private static final String[] POSTERIOR = {
            "вероятность болезни", "вероятность диагноза", "вероятность гипотезы", "вероятность вины",
            "шанс болезни", "шанс что это", "chance you have", "probability you have",
            "probability of disease", "probability of guilt", "chance of guilt", "chance of disease",
            "апостериорн", "posterior", "p(h|e)", "p(гипотеза|данн"
    };

    private ConfusionOfTheInversePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, ignores(a) || ignores(b),
                "ошибка прокурора: точность теста или P(данные|гипотеза) выдаётся за вероятность гипотезы");
        add(conflicts, initial, bothIgnore(a, b),
                "оба эксперта путают условные вероятности: чувствительность выдаётся за апостериорную вероятность");
    }

    /**
     * When the task already contains a full Bayes triple, flag an answer that
     * repeats sensitivity as if it were the positive-test posterior.
     */
    public static void detectAgainstTask(String task, String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        double[] parsed = positivePosterior(task);
        if (parsed == null) return;
        double sensitivityPct = parsed[0];
        double posteriorPct = parsed[1];
        if (Math.abs(sensitivityPct - posteriorPct) < 8.0) return;
        int initial = conflicts.size();
        boolean a = quotesSensitivityAsPosterior(answerA, sensitivityPct, posteriorPct);
        boolean b = quotesSensitivityAsPosterior(answerB, sensitivityPct, posteriorPct);
        add(conflicts, initial, a || b,
                "ошибка прокурора: точность теста или P(данные|гипотеза) выдаётся за вероятность гипотезы");
        add(conflicts, initial, a && b,
                "оба эксперта путают условные вероятности: чувствительность выдаётся за апостериорную вероятность");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "confusion of the inverse (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean ignores(String t) {
        return naivePattern(t);
    }

    private static boolean bothIgnore(String a, String b) {
        return ignores(a) && ignores(b);
    }

    private static boolean naivePattern(String t) {
        if (t == null || t.isEmpty() || acknowledges(t)) return false;
        if (has(t,
                "точность теста и есть вероятность",
                "точность теста равна вероятности",
                "вероятность болезни равна точности",
                "вероятность диагноза равна точности",
                "вероятность вины равна вероятности совпадения",
                "the accuracy is the probability",
                "accuracy of the test is the chance",
                "sensitivity is the probability you have",
                "p(e|h) equals p(h|e)",
                "p(e|h) = p(h|e)",
                "chance of guilt equals the match probability",
                "random match probability is the probability of guilt",
                "99% accurate, so a 99% chance",
                "99% accurate so there is a 99% chance")) {
            return true;
        }
        return pairedPercent(t);
    }

    /** Same percent sits next to an accuracy cue and next to a posterior cue. */
    private static boolean pairedPercent(String t) {
        List<double[]> hits = percents(t);
        for (int i = 0; i < hits.size(); i++) {
            for (int j = 0; j < hits.size(); j++) {
                if (i == j) continue;
                if (Math.abs(hits.get(i)[0] - hits.get(j)[0]) > 0.51) continue;
                String left = window(t, (int) hits.get(i)[1]);
                String right = window(t, (int) hits.get(j)[1]);
                if (has(left, ACCURACY) && has(right, POSTERIOR)) return true;
            }
        }
        return false;
    }

    private static boolean quotesSensitivityAsPosterior(String answer, double sensitivityPct, double posteriorPct) {
        String text = lower(answer);
        if (text.isEmpty() || acknowledges(text)) return false;
        List<double[]> hits = percents(text);
        for (double[] hit : hits) {
            if (Math.abs(hit[0] - sensitivityPct) > 1.5) continue;
            if (Math.abs(hit[0] - posteriorPct) < 8.0) continue;
            String around = window(text, (int) hit[1]);
            if (has(around, POSTERIOR) && !has(around, ACCURACY)) return true;
        }
        return false;
    }

    /** [sensitivity percent, positive posterior percent], or null when Bayes note is absent. */
    private static double[] positivePosterior(String task) {
        String note = BayesianUpdatePolicy.evaluateExplicitRequest(task);
        if (note == null || !note.contains("P(гипотеза | положительный тест):")) return null;
        Double sensitivity = percentAfter(note, "Чувствительность:");
        Double posterior = percentAfter(note, "P(гипотеза | положительный тест):");
        if (sensitivity == null || posterior == null) return null;
        return new double[]{sensitivity, posterior};
    }

    private static Double percentAfter(String text, String label) {
        int at = text.indexOf(label);
        if (at < 0) return null;
        Matcher matcher = PERCENT.matcher(text.substring(at, Math.min(text.length(), at + label.length() + 24)));
        if (!matcher.find()) return null;
        double value = Double.parseDouble(matcher.group(1).replace(',', '.'));
        if (!Double.isFinite(value) || value < 0.0 || value > 100.0) return null;
        return value;
    }

    private static List<double[]> percents(String text) {
        List<double[]> hits = new ArrayList<double[]>();
        Matcher matcher = PERCENT.matcher(text);
        while (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1).replace(',', '.'));
            hits.add(new double[]{value, matcher.start()});
        }
        return hits;
    }

    private static String window(String text, int at) {
        int from = Math.max(0, at - 48);
        int to = Math.min(text.length(), at + 48);
        return text.substring(from, to);
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "base rate", "базовая частот", "базовой частот", "априор", "prior",
                "prevalence", "распростран", "ложноположитель", "false positive",
                "confusion of the inverse", "prosecutor's fallacy", "prosecutor’s fallacy",
                "ошибка прокурора", "ошибке прокурора",
                "не равна вероятности гипотезы", "не путать условн", "не копируй точность");
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
