package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local marginal-analysis / diminishing-returns signals.
 * Flags a verdict that treats a large-scale, full-intensity or all-or-nothing action as clearly superior
 * without stating the incremental (marginal) costs and benefits of the next unit, the point of diminishing returns,
 * or why the last unit is still worthwhile, when the decision is about intensity, quantity, dosage or scale
 * rather than a pure binary go/no-go.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on total or average effects when the relevant question is the margin.
 * Distinct from unit-bias (boundary of one unit) and pure opportunity-cost: this focuses on optimization
 * at the intensive margin under diminishing returns.
 */
public final class MarginalAnalysisPolicy {
    private static final int MAX_SIGNALS = 2;

    private MarginalAnalysisPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesWithoutMarginal(a) || closesWithoutMarginal(b),
                "marginal analysis: вердикт закрывает на полном масштабе / всё или ничего без оценки предельных (маржинальных) затрат и выгод следующей единицы и точки убывающей отдачи");
        add(conflicts, initial, bothNeglect(a, b),
                "оба эксперта закрывают на среднем/полном эффекте, не назвав маржинальную ценность и убывающую отдачу");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "marginal analysis (possible signal, requires context): " + core;
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
                "marginal", "маржинальный", "предельный",
                "diminishing returns", "убывающая отдача",
                "next unit", "следующая единица",
                "intensive margin", "интенсивная граница",
                "optimal dosage", "оптимальная доза", "на пределе");
    }

    private static boolean closesWithoutMarginal(String t) {
        boolean scaleLang = has(t,
                "делаем в полном объёме", "full scale", "всё или ничего",
                "all or nothing", "максимальный охват", "большая доза",
                "полный масштаб лучше", "maximum intensity is best");
        boolean closing = has(t,
                "поэтому делаем полностью", "therefore full scale",
                "закрываем на максимуме", "so we go all-in",
                "нет смысла частично", "partial is not enough");
        boolean mentionsMarginal = has(t,
                "маржинальный", "marginal cost", "предельная выгода",
                "убывающая отдача", "diminishing returns",
                "следующая единица уже не", "next unit not worth");
        return scaleLang && closing && !mentionsMarginal && !acknowledges(t);
    }

    private static boolean bothNeglect(String a, String b) {
        return closesWithoutMarginal(a) && closesWithoutMarginal(b);
    }
}
