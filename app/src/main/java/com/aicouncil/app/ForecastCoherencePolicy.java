package com.aicouncil.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local arithmetic on forecasts. Signals are computed from numbers the answers
 * actually state. They are hypotheses about coherence, not verdicts, and the
 * coordinator must not average them away.
 */
public final class ForecastCoherencePolicy {
    private static final int MAX_SIGNALS = 4;
    private static final Pattern SUCCESS = Pattern.compile(
            "(?:вероятность|шанс|ймовірність|probability)\\s+(?:успеха|успіху|success)\\s*[:\\-–]?\\s*(\\d{1,3}(?:[.,]\\d+)?)\\s*%");
    private static final Pattern FAILURE = Pattern.compile(
            "(?:вероятность|шанс|ймовірність|probability)\\s+(?:провала|неудачи|провалу|невдачі|failure)\\s*[:\\-–]?\\s*(\\d{1,3}(?:[.,]\\d+)?)\\s*%");
    private static final Pattern NAMED = Pattern.compile(
            "(?:вероятность|шанс|ймовірність)\\s+([\\p{L}]{4,24})\\s*[:\\-–]?\\s*(\\d{1,3}(?:[.,]\\d+)?)\\s*%");
    private static final Pattern JOINT = Pattern.compile(
            "(?:и то и другое|оба события|совместная вероятность|й те й інше|both happen)\\s*[:\\-–]?\\s*(\\d{1,3}(?:[.,]\\d+)?)\\s*%");
    private static final Pattern EACH = Pattern.compile(
            "(?:по отдельности|каждое по|each one|each is|окремо)\\s*[:\\-–]?\\s*(\\d{1,3}(?:[.,]\\d+)?)\\s*%");
    private static final Pattern PERCENT = Pattern.compile("(\\d{1,3}(?:[.,]\\d+)?)\\s*%");
    private static final Pattern GAIN = Pattern.compile(
            "(?:выигрыш|прибыль|gain|payoff|виграш)\\s*[:\\-–]?\\s*(\\d+(?:[.,]\\d+)?)");
    private static final Pattern LOSS = Pattern.compile(
            "(?:потеря|убыток|loss|втрата|збиток)\\s*[:\\-–]?\\s*(\\d+(?:[.,]\\d+)?)");

    private ForecastCoherencePolicy() {}

    public static boolean wantsNumericContract(String task) {
        String t = lower(task);
        return t.contains("прогноз") || t.contains("forecast") || t.contains("вероятност")
                || t.contains("шанс успеха") || t.contains("шанс успіху") || t.contains("ожидаем")
                || t.contains("стоит ли") || t.contains("чи варто") || t.contains("инвестир")
                || t.contains("інвест") || t.contains("запускать") || t.contains("запускати");
    }

    public static void detect(String task, String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        String a = lower(answerA);
        String b = lower(answerB);
        add(conflicts, initial, complementMessage(a, "ИИ №1"));
        add(conflicts, initial, complementMessage(b, "ИИ №2"));
        add(conflicts, initial, conjunction(a) || conjunction(b)
                ? "вероятности: совместный исход заявлен вероятнее отдельного события"
                : null);
        add(conflicts, initial, exclusiveMessage(a, b));
        add(conflicts, initial, namedClash(a, b));
        add(conflicts, initial, valueClash(a, b));
        add(conflicts, initial, outsideViewMissing(task, a, b)
                ? "прогноз: точечная оценка без reference class и без базовой частоты похожих случаев"
                : null);
    }

    private static void add(List<String> out, int initial, String message) {
        if (message == null || message.isEmpty()) return;
        if (out.size() - initial >= MAX_SIGNALS || out.contains(message)) return;
        out.add(message);
    }

    private static String complementMessage(String text, String who) {
        Double success = first(SUCCESS, text);
        Double failure = first(FAILURE, text);
        if (success == null || failure == null) return null;
        if (success < 0 || failure < 0 || success > 100 || failure > 100) return null;
        if (Math.abs(success + failure - 100.0) <= 8.0) return null;
        return "вероятности: у " + who + " успех " + fmt(success) + "% и провал " + fmt(failure)
                + "% не дополняют друг друга до 100";
    }

    private static boolean conjunction(String text) {
        Double joint = first(JOINT, text);
        Double each = first(EACH, text);
        if (joint == null || each == null) return false;
        return joint > each + 0.5 && joint <= 100 && each >= 0;
    }

    private static String exclusiveMessage(String a, String b) {
        Double left = exclusiveSum(a);
        Double right = exclusiveSum(b);
        if (left == null && right == null) return null;
        String who = left != null ? "одном ответе " + fmt(left) + "%" : "одном ответе " + fmt(right) + "%";
        return "вероятности: взаимоисключающие доли суммируются в " + who + ", это больше 100";
    }

    private static Double exclusiveSum(String text) {
        if (!contains(text, "взаимоисключ", "только один вариант", "mutually exclusive",
                "взаємовиключ", "лише один варіант")) {
            return null;
        }
        Matcher matcher = PERCENT.matcher(text);
        double sum = 0;
        int count = 0;
        while (matcher.find()) {
            double value = parse(matcher.group(1));
            if (value < 0 || value > 100) continue;
            sum += value;
            count++;
        }
        if (count < 2 || sum <= 105.0) return null;
        return sum;
    }

    private static String namedClash(String a, String b) {
        Map<String, Double> left = named(a);
        Map<String, Double> right = named(b);
        String best = null;
        double bestGap = 0;
        for (Map.Entry<String, Double> entry : left.entrySet()) {
            Double other = right.get(entry.getKey());
            if (other == null) continue;
            double gap = Math.abs(entry.getValue() - other);
            if (gap >= 20.0 && gap > bestGap) {
                bestGap = gap;
                best = "вероятности: «" + entry.getKey() + "» оценено по-разному ("
                        + fmt(entry.getValue()) + "% vs " + fmt(other) + "%), не усредняй";
            }
        }
        return best;
    }

    private static Map<String, Double> named(String text) {
        Map<String, Double> found = new LinkedHashMap<String, Double>();
        Matcher matcher = NAMED.matcher(text);
        while (matcher.find()) {
            String label = stem(matcher.group(1));
            double value = parse(matcher.group(2));
            if (label.length() < 4 || value < 0 || value > 100) continue;
            if (!found.containsKey(label)) found.put(label, value);
        }
        return found;
    }

    /** "успеха" and "успех" should meet. Keep a short stable stem. */
    private static String stem(String word) {
        String value = word.toLowerCase(Locale.ROOT);
        if (value.length() > 6) return value.substring(0, 6);
        if (value.length() > 4) return value.substring(0, value.length() - 1);
        return value;
    }

    private static String valueClash(String a, String b) {
        Double left = expectedValue(a);
        Double right = expectedValue(b);
        if (left == null && right == null) return null;
        if (left == null || right == null) {
            return "ожидаемая ценность: расклад вероятность/выигрыш/потеря есть только у одного ответа, второй нельзя уравнивать по гладкости текста";
        }
        if (left * right < 0 && Math.abs(left) >= 1 && Math.abs(right) >= 1) {
            return "ожидаемая ценность: знаки расходятся (" + fmt(left) + " vs " + fmt(right) + "), не усредняй нарратив";
        }
        double gap = Math.abs(left - right);
        double scale = Math.max(Math.abs(left), Math.abs(right));
        if (scale >= 1 && gap / scale > 0.5 && gap >= 10) {
            return "ожидаемая ценность: расхождение больше половины (" + fmt(left) + " vs " + fmt(right) + ")";
        }
        return null;
    }

    static Double expectedValue(String text) {
        Double chance = first(SUCCESS, text);
        Double gain = first(GAIN, text);
        Double loss = first(LOSS, text);
        if (chance == null || gain == null || loss == null) return null;
        if (chance < 0 || chance > 100 || gain < 0 || loss < 0) return null;
        double p = chance / 100.0;
        return p * gain - (1.0 - p) * loss;
    }

    private static boolean outsideViewMissing(String task, String a, String b) {
        if (!wantsNumericContract(task)) return false;
        String both = a + "\n" + b;
        if (contains(both, "reference class", "base rate", "базов", "похожих случа", "історичн", "историческ")) {
            return false;
        }
        return pointEstimate(a) || pointEstimate(b);
    }

    private static boolean pointEstimate(String text) {
        boolean percent = text.matches("(?s).*\\d+\\s*%.*");
        if (!percent) return false;
        return contains(text, "ровно ", "точно ", "exactly ", "рівно ");
    }

    private static Double first(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) return null;
        return parse(matcher.group(1));
    }

    private static double parse(String raw) {
        return Double.parseDouble(raw.replace(',', '.'));
    }

    private static boolean contains(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String fmt(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.05) return String.valueOf((long) Math.rint(value));
        return String.format(Locale.US, "%.1f", value);
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    /** Exposed for tests that want the parsed triple without going through the council. */
    public static List<String> explain(String task, String answerA, String answerB) {
        List<String> out = new ArrayList<String>();
        detect(task, answerA, answerB, out);
        return out;
    }
}
