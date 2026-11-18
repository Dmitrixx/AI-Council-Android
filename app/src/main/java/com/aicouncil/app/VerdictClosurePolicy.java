package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local check that a closing label actually closed the verdict.
 * Bias detectors look at drafts. This looks at the decision label itself:
 * ДЕЛАТЬ / НЕ ДЕЛАТЬ without a falsifier and a reversibility note is not closed,
 * and a forecast stated as one percent is not a range.
 * Naming the gap is not itself an error. Signals are hypotheses, not verdicts.
 */
public final class VerdictClosurePolicy {
    private static final int MAX_SIGNALS = 2;
    private static final int MAX_GAPS = 3;
    private static final Pattern PERCENT = Pattern.compile("(?<!\\d)(\\d{1,3})\\s*%");

    private VerdictClosurePolicy() {}

    public static void detect(String task, String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        List<String> gapsA = gaps(task, answerA);
        List<String> gapsB = gaps(task, answerB);
        if (!gapsA.isEmpty() || !gapsB.isEmpty()) {
            String sample = !gapsA.isEmpty() ? gapsA.get(0) : gapsB.get(0);
            add(conflicts, initial,
                    "verdict closure: closed label is missing a required slot (" + sample + ")");
        }
        if (!gapsA.isEmpty() && !gapsB.isEmpty()) {
            add(conflicts, initial,
                    "verdict closure: both experts use a closed label without the required slot");
        }
    }

    /**
     * Short coordinator reminder. Empty for tiny tasks and for kinds
     * that do not use a closure label.
     */
    public static String reminder(String task) {
        if (task == null || task.trim().length() < 40) return "";
        SynthesisProtocol.Kind kind = SynthesisProtocol.classify(task);
        if (kind == SynthesisProtocol.Kind.DECISION) {
            return "\n\nПРОВЕРКА ЗАКРЫТИЯ (посчитана приложением, не моделью): "
                    + "ровно одна метка — ДЕЛАТЬ, НЕ ДЕЛАТЬ или СНАЧАЛА ПРОВЕРИТЬ. "
                    + "ДЕЛАТЬ и НЕ ДЕЛАТЬ допустимы только вместе с наблюдением, которое перевернёт вывод, "
                    + "и с явной пометкой, обратим ли шаг. Иначе ставь СНАЧАЛА ПРОВЕРИТЬ и назови саму проверку. "
                    + "Назвать пробел — не ошибка.";
        }
        if (kind == SynthesisProtocol.Kind.FORECAST) {
            return "\n\nПРОВЕРКА ЗАКРЫТИЯ (посчитана приложением, не моделью): "
                    + "итоговая вероятность — диапазон, не одна точка, и рядом reference class "
                    + "либо явное «базовой частоты нет». Точка без диапазона не закрывает прогноз.";
        }
        return "";
    }

    /**
     * Audits the coordinator section when the report has one, otherwise the whole text.
     * Empty when the verdict is already open, already names its gaps, or makes no closing label.
     */
    public static String footer(String task, String fullResult) {
        String slice = coordinatorSlice(fullResult);
        if (slice.isEmpty()) slice = fullResult == null ? "" : fullResult;
        List<String> found = gaps(task, slice);
        if (found.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("⚠️ СОВЕТ НЕ ЗАКРЫТ (локальная проверка приложения, не вердикт модели):\n");
        int shown = 0;
        for (String gap : found) {
            if (shown >= MAX_GAPS) break;
            sb.append("• ").append(gap).append("\n");
            shown++;
        }
        sb.append("Назвать пробел достаточно, чтобы проверка замолчала. Это не запрет рекомендации.");
        return sb.toString().trim();
    }

    private static String coordinatorSlice(String text) {
        if (text == null) return "";
        int marker = text.lastIndexOf("ИТОГ КООРДИНАТОРА");
        if (marker < 0) return "";
        return text.substring(marker);
    }

    static List<String> gaps(String task, String verdict) {
        List<String> out = new ArrayList<String>();
        if (task == null || verdict == null) return out;
        if (task.trim().length() < 40 || verdict.trim().length() < 40) return out;
        String raw = verdict;
        if (acknowledges(raw.toLowerCase(Locale.ROOT))) return out;
        SynthesisProtocol.Kind kind = SynthesisProtocol.classify(task);
        if (kind == SynthesisProtocol.Kind.DECISION) {
            addDecisionGaps(raw, out);
        } else if (kind == SynthesisProtocol.Kind.FORECAST) {
            addForecastGaps(raw, out);
        }
        return out;
    }

    private static void addDecisionGaps(String raw, List<String> out) {
        String upper = raw.toUpperCase(Locale.ROOT);
        boolean no = hasLabel(upper, "НЕ ДЕЛАТЬ");
        boolean probe = hasLabel(upper, "СНАЧАЛА ПРОВЕРИТЬ");
        String withoutNo = upper.replace("НЕ ДЕЛАТЬ", "         ");
        boolean yes = hasLabel(withoutNo, "ДЕЛАТЬ");
        int labels = (yes ? 1 : 0) + (no ? 1 : 0) + (probe ? 1 : 0);
        if (labels == 0) return;
        if (labels > 1) {
            out.add("несколько меток сразу: оставь ровно одну — ДЕЛАТЬ, НЕ ДЕЛАТЬ или СНАЧАЛА ПРОВЕРИТЬ");
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        if (probe && !yes && !no) {
            if (!concreteProbe(lower)) {
                out.add("метка СНАЧАЛА ПРОВЕРИТЬ без конкретной проверки");
            }
            return;
        }
        if (!falsifier(lower)) {
            out.add("метка ДЕЛАТЬ или НЕ ДЕЛАТЬ без наблюдения, которое перевернёт вывод");
        }
        if (!reversibility(lower)) {
            out.add("не сказано, обратим шаг или нет");
        }
    }

    private static void addForecastGaps(String raw, List<String> out) {
        String lower = raw.toLowerCase(Locale.ROOT);
        if (!has(lower, "вероятност", "шанс", "probability", "прогноз", "forecast")) return;
        int percents = countPercents(raw);
        boolean ranged = percents >= 2
                || has(lower, "диапазон", "интервал", "range", "between ")
                || raw.matches("(?s).*\\d\\s*[-–—]\\s*\\d+\\s*%.*");
        boolean reference = has(lower,
                "reference class", "reference-class", "outside view",
                "базов", "похожих случа", "частота класса", "базовой частоты нет",
                "частоты нет", "нет базовой");
        if (percents >= 1 && !ranged) {
            out.add("точечная вероятность без диапазона");
        }
        if ((percents >= 1 || has(lower, "вероятность успеха", "итоговая вероятность")) && !reference) {
            out.add("прогноз без reference class или явного «базовой частоты нет»");
        }
    }

    private static boolean concreteProbe(String lower) {
        return has(lower,
                "если ", "тест ", "измер", "метрик", "пилот",
                "эксперимент", "опрос ", "опроса", "a/b", "конверс");
    }

    private static boolean falsifier(String lower) {
        return has(lower,
                "переверн", "переключ", "crux", "если увидим", "если окажется",
                "что изменило бы", "what would change", "change my mind",
                "критерий опровержения", "kill criterion",
                "наблюден", "не назван");
    }

    private static boolean reversibility(String lower) {
        return has(lower,
                "обратим", "необратим", "откатить", "откат",
                "reversib", "undo", "обратимость неизвестна", "не оценена обратимость");
    }

    private static boolean acknowledges(String lower) {
        return has(lower,
                "совет не закрыт", "вердикт не закрыт", "verdict closure",
                "проверку закрытия", "не закрываю метк");
    }

    private static int countPercents(String text) {
        int n = 0;
        Matcher matcher = PERCENT.matcher(text);
        while (matcher.find()) n++;
        return n;
    }

    private static boolean hasLabel(String upper, String label) {
        int from = 0;
        while (from < upper.length()) {
            int i = upper.indexOf(label, from);
            if (i < 0) return false;
            boolean left = i == 0 || !Character.isLetter(upper.charAt(i - 1));
            int end = i + label.length();
            boolean right = end >= upper.length() || !Character.isLetter(upper.charAt(end));
            if (left && right) return true;
            from = i + 1;
        }
        return false;
    }

    private static void add(List<String> conflicts, int initial, String core) {
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "verdict closure (possible signal, requires context): " + core;
        for (String existing : conflicts) {
            if (existing != null && existing.contains(core.substring(0, Math.min(36, core.length())))) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... phrases) {
        if (text == null) return false;
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }
}
