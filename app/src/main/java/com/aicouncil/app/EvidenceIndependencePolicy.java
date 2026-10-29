package com.aicouncil.app;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline checks that agreement is not the same thing as independent evidence.
 * Signals are hypotheses and stay on the decision map so the coordinator cannot average them away.
 */
public final class EvidenceIndependencePolicy {
    private static final int MAX_SIGNALS = 4;
    private static final Pattern URL = Pattern.compile("https?://[^\\s)\\]>\"']+");

    private EvidenceIndependencePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, sharedSingleSource(answerA, answerB, a, b),
                "один источник у обоих: совпадение ответов держится на одной и той же ссылке и не является независимой репликацией");
        add(conflicts, initial, regressionIgnored(a) || regressionIgnored(b),
                "рекорд без возврата к среднему: крайний результат предлагается считать новой нормой");
        add(conflicts, initial, goodhart(a) || goodhart(b),
                "подмена цели метрикой: показатель предлагается максимизировать без оговорки, что его можно накрутить");
        add(conflicts, initial, irreversibleWithoutPremortem(a) || irreversibleWithoutPremortem(b),
                "необратимый шаг без премортема: решение объявлено необратимым, но не названо, что его убило бы");
    }

    private static void add(List<String> out, int initial, boolean condition, String message) {
        String formatted = "карта решения — возможный сигнал, требуется проверка контекста: " + message;
        if (!condition || out.size() - initial >= MAX_SIGNALS || out.contains(formatted)) return;
        out.add(formatted);
    }

    /** Both answers lean on the same URL and treat that overlap as confirmation. */
    private static boolean sharedSingleSource(String rawA, String rawB, String a, String b) {
        String shared = sharedUrl(rawA, rawB);
        if (shared == null) return false;
        boolean treatsAsProof = has(a, "доказан", "подтвержда", "значит верно", "therefore confirmed", "settled")
                || has(b, "доказан", "подтвержда", "значит верно", "therefore confirmed", "settled");
        boolean secondSource = has(a, "друг") && has(a, "исследован")
                || has(b, "друг") && has(b, "исследован")
                || has(a, "независим", "replication", "репликац")
                || has(b, "независим", "replication", "репликац");
        return treatsAsProof && !secondSource;
    }

    private static String sharedUrl(String left, String right) {
        if (left == null || right == null) return null;
        Matcher matcher = URL.matcher(left);
        while (matcher.find()) {
            String url = stripTrail(matcher.group());
            if (url.length() < 12) continue;
            if (right.toLowerCase(Locale.ROOT).contains(url.toLowerCase(Locale.ROOT))) return url;
        }
        return null;
    }

    private static String stripTrail(String url) {
        int end = url.length();
        while (end > 0 && ".,;:".indexOf(url.charAt(end - 1)) >= 0) end--;
        return url.substring(0, end);
    }

    private static boolean regressionIgnored(String t) {
        return has(t, "рекордный", "аномально высок", "best quarter ever", "рекорд так и")
                && has(t, "так и останется", "новой нормой", "will stay this high", "следующий будет таким же")
                && !has(t, "вернётся к среднему", "regression to the mean", "регресс к среднему", "outside view");
    }

    private static boolean goodhart(String t) {
        return has(t, "максимизировать kpi", "максимизируем показатель", "maximize the metric", "цель — метрика")
                && has(t, "любой ценой", "это и есть успех", "the number is the goal")
                && !has(t, "накрутить", "goodhart", "метрика перестанет", "proxy");
    }

    private static boolean irreversibleWithoutPremortem(String t) {
        return has(t, "необратимо", "irreversible", "обратно уже нельзя", "сразу на всех пользователей навсегда")
                && has(t, "рекомендую", "нужно сделать", "we should ship", "запускаем")
                && !has(t, "премортем", "pre-mortem", "что убьёт", "если провалится", "kill criterion", "что опровергнет");
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
