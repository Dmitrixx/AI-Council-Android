package com.aicouncil.app;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic likelihood-ratio update when the task explicitly supplies
 * a prior and a likelihood ratio. Silent unless both cues sit next to numbers.
 * The note is a calculation, not a diagnosis or a recommendation.
 */
public final class LikelihoodRatioPolicy {
    private static final Pattern NUMBER = Pattern.compile("(\\d+(?:[.,]\\d+)?)");
    private static final Pattern PERCENT = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*%");

    private LikelihoodRatioPolicy() {}

    public static String evaluateExplicitRequest(String task) {
        if (task == null || task.trim().isEmpty()) return "";
        Double priorProb = percentNear(task, "распростран", "априор", "prevalence", "base rate", "базовая частот", "prior");
        Double priorOdds = numberNear(task, "априорные шансы", "prior odds", "odds");
        Double lr = numberNear(task, "likelihood ratio", "lr+", "lr-", "коэффициент правдоподобия", "правдоподобие", "lr ");
        if (lr == null || lr <= 0 || !Double.isFinite(lr)) return "";
        if (priorProb == null && priorOdds == null) return "";
        if (priorProb != null && !unit(priorProb)) return "";

        double odds;
        if (priorOdds != null && priorOdds > 0) {
            odds = priorOdds;
        } else if (priorProb != null) {
            odds = priorProb / (1.0 - priorProb);
        } else {
            return "";
        }
        if (!Double.isFinite(odds) || odds < 0) return "";

        double posteriorOdds = odds * lr;
        double posterior = posteriorOdds / (1.0 + posteriorOdds);
        if (!Double.isFinite(posterior) || posterior < 0 || posterior > 1) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("ДЕТЕРМИНИРОВАННОЕ ОБНОВЛЕНИЕ ПО КОЭФФИЦИЕНТУ ПРАВДОПОДОБИЯ (не ответ ИИ):\n");
        if (priorProb != null) {
            sb.append("Априорная вероятность: ").append(pct(priorProb)).append('\n');
        }
        if (priorOdds != null) {
            sb.append("Априорные шансы: ").append(num(priorOdds)).append('\n');
        }
        sb.append("Коэффициент правдоподобия: ").append(num(lr)).append('\n');
        sb.append("Апостериорные шансы: ").append(num(posteriorOdds)).append('\n');
        sb.append("Апостериорная вероятность: ").append(pct(posterior)).append('\n');
        sb.append("Метод: odds * LR в приложении. Это не диагноз. Если числа означают другое, результат не применять и сказать об этом явно.");
        return sb.toString();
    }

    private static boolean unit(double value) {
        return Double.isFinite(value) && value >= 0.0 && value <= 1.0;
    }

    private static String pct(double value) {
        return String.format(Locale.ROOT, "%.2f%%", value * 100.0);
    }

    private static String num(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }

    private static Double percentNear(String text, String... cues) {
        String lower = text.toLowerCase(Locale.ROOT);
        for (String cue : cues) {
            int at = lower.indexOf(cue);
            if (at < 0) continue;
            Double forward = closestPercent(text, at + cue.length(), Math.min(text.length(), at + cue.length() + 20));
            if (forward != null) return forward;
            Double backward = closestPercent(text, Math.max(0, at - 10), at);
            if (backward != null) return backward;
        }
        return null;
    }

    private static Double numberNear(String text, String... cues) {
        String lower = text.toLowerCase(Locale.ROOT);
        for (String cue : cues) {
            int at = lower.indexOf(cue);
            if (at < 0) continue;
            Double forward = closestNumber(text, at + cue.length(), Math.min(text.length(), at + cue.length() + 16));
            if (forward != null) return forward;
            Double backward = closestNumber(text, Math.max(0, at - 8), at);
            if (backward != null) return backward;
        }
        return null;
    }

    private static Double closestPercent(String text, int from, int to) {
        if (to <= from) return null;
        String slice = text.substring(from, to);
        Matcher matcher = PERCENT.matcher(slice);
        if (!matcher.find()) return null;
        double raw = Double.parseDouble(matcher.group(1).replace(',', '.'));
        if (!Double.isFinite(raw) || raw < 0.0 || raw > 100.0) return null;
        return raw / 100.0;
    }

    private static Double closestNumber(String text, int from, int to) {
        if (to <= from) return null;
        String slice = text.substring(from, to);
        Matcher matcher = NUMBER.matcher(slice);
        if (!matcher.find()) return null;
        double raw = Double.parseDouble(matcher.group(1).replace(',', '.'));
        if (!Double.isFinite(raw) || raw < 0.0) return null;
        return raw;
    }
}
