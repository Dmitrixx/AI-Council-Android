package com.aicouncil.app;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local signals that two language models sharing a training corpus are not
 * two independent samples, and that a commitment with an open data gap still
 * needs one discriminating observation.
 * Hypotheses requiring context, not verdicts. The coordinator must not average them away.
 */
public final class ModelCorrelationPolicy {
    private static final int MAX_SIGNALS = 3;
    private static final int MIN_TOKENS = 22;
    static final double OVERLAP_AT = 0.72;
    private static final Pattern TOKEN = Pattern.compile("[\\p{L}]{4,}");
    private static final Set<String> STOP = new HashSet<String>();

    static {
        String[] words = {
                "этот", "эта", "это", "эти", "этого", "этой", "этом", "этому",
                "того", "только", "также", "если", "когда", "потому", "который", "которая",
                "которые", "которое", "будет", "будут", "нужно", "можно", "стоит", "между",
                "после", "перед", "через", "очень", "более", "менее", "есть", "нет",
                "или", "для", "при", "как", "что", "чтобы", "уже", "ещё", "еще", "все",
                "that", "this", "with", "from", "have", "been", "were", "they", "their",
                "which", "would", "could", "should", "about", "into", "than", "then",
                "також", "буде", "потрібно", "можна", "який", "яка", "які", "коли", "якщо"
        };
        for (int i = 0; i < words.length; i++) STOP.add(words[i]);
    }

    private ModelCorrelationPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        int initial = conflicts.size();
        add(conflicts, initial, correlated(answerA, answerB),
                "ответы моделей почти совпадают и оба уверены: общий корпус не является второй независимой выборкой");
        add(conflicts, initial, informationGap(answerA) || informationGap(answerB),
                "решение предлагают при явном пробеле данных и не называют наблюдение, которое его закроет");
    }

    /**
     * Content-token Jaccard in [0, 1]. Returns 0 when either side is too short
     * for a stable comparison, so short echoes do not look like correlation.
     */
    public static double overlap(String left, String right) {
        Set<String> a = tokens(left);
        Set<String> b = tokens(right);
        if (a.size() < MIN_TOKENS || b.size() < MIN_TOKENS) return 0d;
        int inter = 0;
        for (String token : a) {
            if (b.contains(token)) inter++;
        }
        int union = a.size() + b.size() - inter;
        if (union == 0) return 0d;
        return (double) inter / (double) union;
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "корреляция моделей (возможный сигнал, требует контекста): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (int i = 0; i < conflicts.size(); i++) {
            if (conflicts.get(i).contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean correlated(String left, String right) {
        if (overlap(left, right) < OVERLAP_AT) return false;
        if (!confident(left) || !confident(right)) return false;
        return !dissent(lower(left)) && !dissent(lower(right));
    }

    private static boolean informationGap(String raw) {
        String t = disarm(lower(raw));
        boolean commit = has(t,
                "рекомендую", "нужно сделать", "we should", "запускаем",
                "стоит вложить", "стоит запускать", "надо запускать", "следует сделать");
        boolean unknown = has(t,
                "неизвестно", "нет данных", "не хватает данных", "неясно",
                "unclear", "uncertain", "не знаем", "данных нет", "пробел в данных",
                "unknown");
        boolean probe = has(t,
                "пилот", "эксперимент", "a/b", "аб-тест", "измерить",
                "сначала провер", "что изменило бы", "проверить гипотез",
                "дешёвый тест", "собрать данные", "собрать информац", "дискримин");
        return commit && unknown && !probe;
    }

    private static boolean confident(String raw) {
        String t = disarm(lower(raw));
        if (has(t,
                "однозначно", "несомненно", "без сомнен", "нет сомнен", "почти наверняка",
                "definitely", "certainly", "without doubt", "i am sure",
                "я уверен", "я впевнен", "высокая уверенность", "уверен на ")) {
            return true;
        }
        return t.contains("уверенность 8") || t.contains("уверенность 9")
                || t.contains("упевненість 8") || t.contains("упевненість 9");
    }

    private static boolean dissent(String t) {
        return has(t,
                "не согласен", "не згоден", "disagree", "напротив", "расхожусь",
                "другая позиция", "противоположн");
    }

    /** Negation must not count as confidence or as a recommendation to proceed. */
    private static String disarm(String t) {
        return t
                .replace("не уверен", " ")
                .replace("не впевнен", " ")
                .replace("not sure", " ")
                .replace("не рекомендую", " ")
                .replace("не стоит", " ")
                .replace("не следует", " ")
                .replace("не нужно", " ")
                .replace("не треба", " ")
                .replace("do not recommend", " ")
                .replace("don't recommend", " ");
    }

    private static Set<String> tokens(String text) {
        Set<String> out = new HashSet<String>();
        if (text == null) return out;
        Matcher matcher = TOKEN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String token = matcher.group();
            if (!STOP.contains(token)) out.add(token);
        }
        return out;
    }

    private static boolean has(String text, String... words) {
        for (int i = 0; i < words.length; i++) {
            if (text.contains(words[i])) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
