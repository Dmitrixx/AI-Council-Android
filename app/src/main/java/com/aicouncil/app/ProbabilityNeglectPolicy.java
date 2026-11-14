package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local probability neglect signals.
 * Flags a verdict that treats outcome severity, horror, catastrophe or large upside
 * as decisive while ignoring, dismissing or never stating likelihood / base rate of
 * that outcome. Naming the gap is not itself an error.
 * The coordinator cannot close on magnitude alone.
 */
public final class ProbabilityNeglectPolicy {
    private static final int MAX_SIGNALS = 2;

    private ProbabilityNeglectPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, magnitudeWithoutProbability(a) || magnitudeWithoutProbability(b),
                "probability neglect: вердикт закрывает выбор тяжестью/ужасом/масштабом исхода без упоминания вероятности, правдоподобия или базовой частоты этого исхода");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на магнитуде исхода без независимой оценки вероятности");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "probability neglect (possible signal, requires context): " + core;
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
                "probability neglect", "пренебрежение вероятностью",
                "likelihood", "правдоподобие",
                "base rate of the outcome", "базовая частота исхода",
                "even if rare", "даже если редко",
                "probability is low", "вероятность низкая",
                "expected value", "математическое ожидание");
    }

    private static boolean magnitudeWithoutProbability(String t) {
        boolean magnitude = has(t,
                "катастрофа", "ужасный исход", "невообразимый ущерб",
                "catastrophe", "horrific", "unimaginable damage",
                "огромный выигрыш", "огромный upside", "transformative upside",
                "destroy everything", "уничтожит всё", "ruined forever",
                "один раз и всё", "once and it's over");
        boolean decisive = has(t,
                "поэтому нельзя", "значит надо",
                "therefore we must", "so we cannot",
                "recommend", "выбираем", "закрываем",
                "proves we should", "доказывает что надо");
        boolean mentionsProb = has(t,
                "вероятност", "probability", "likelihood", "chance",
                "правдоподоб", "базовая частота", "base rate",
                "редко", "редкостно", "unlikely", "rare",
                "expected value", "математическое ожидание");
        return magnitude && decisive && !mentionsProb && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return magnitudeWithoutProbability(a) && magnitudeWithoutProbability(b);
    }
}
