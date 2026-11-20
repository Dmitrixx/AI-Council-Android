package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local frame of the question itself, computed before any model speaks.
 * Extracts the asked question, explicit alternatives and magnitudes, then names
 * the missing ingredients a closable answer would need (options, scale, horizon,
 * who bears the loss, base rate, success metric). Naming a gap is not an error.
 * A verdict that closes while the top gap is still open is a hypothesis, not a choice.
 * Distinct from verdict-closure (label slots) and from key-assumptions (hidden premises
 * inside a recommendation): this starts from the task text.
 */
public final class DecisionFramePolicy {
    private static final int MAX_SIGNALS = 2;
    private static final int MAX_GAPS = 4;
    private static final Pattern OR_SPLIT = Pattern.compile(
            "(?iu)((?:[^\\s?!,.;:]+\\s+){0,5}[^\\s?!,.;:]+)\\s+(?:или|либо|versus|\\bvs\\.?\\b|\\bor\\b)\\s+((?:[^\\s?!,.;:]+\\s+){0,5}[^\\s?!,.;:]+)");
    private static final Pattern BETWEEN = Pattern.compile(
            "(?iu)между\\s+((?:[^\\s?!,.;:]+\\s+){0,4}[^\\s?!,.;:]+)\\s+и\\s+((?:[^\\s?!,.;:]+\\s+){0,4}[^\\s?!,.;:]+)");
    private static final Pattern MAGNITUDE = Pattern.compile(
            "(?iu)(\\d+(?:[.,]\\d+)?)\\s*(%|процент(?:а|ов)?|₽|руб(?:лей|ля|\\.)?|\\$|usd|eur|млн|тыс\\.?|человек|пользовател(?:ей|я)|дн(?:ей|я)|недел(?:ь|и|ю)|месяц(?:ев|а)?|год(?:а|ов)?|лет)");

    private DecisionFramePolicy() {}

    public static final class Gap {
        public final String key;
        public final String text;
        Gap(String key, String text) {
            this.key = key;
            this.text = text;
        }
    }

    public static String block(String task) {
        if (task == null || task.trim().length() < 40) return "";
        List<Gap> gaps = gaps(task);
        StringBuilder sb = new StringBuilder();
        sb.append("КАДР РЕШЕНИЯ (посчитан приложением из текста задачи, не источник фактов):\n");
        sb.append("Вопрос: ").append(question(task)).append("\n");
        sb.append("Режим: ").append(label(SynthesisProtocol.classify(task))).append(".\n");
        List<String> options = options(task);
        if (options.isEmpty()) {
            sb.append("Альтернативы: в тексте не выделены.\n");
        } else {
            sb.append("Альтернативы: ").append(join(options)).append(".\n");
        }
        List<String> numbers = magnitudes(task);
        if (numbers.isEmpty()) {
            sb.append("Величины: не найдены.\n");
        } else {
            sb.append("Величины: ").append(join(numbers)).append(".\n");
        }
        if (gaps.isEmpty()) {
            sb.append("Пробелы кадра: явных дыр в постановке не найдено.\n");
            sb.append("Опровержение: всё равно назови одно наблюдение, которое перевернёт вывод.\n");
        } else {
            sb.append("Пробелы кадра:\n");
            for (Gap gap : gaps) sb.append("- ").append(gap.text).append("\n");
            sb.append("Опровержение: ").append(falsifier(gaps.get(0).key)).append("\n");
            sb.append("Правило: не закрывай меткой ДЕЛАТЬ или НЕ ДЕЛАТЬ, пока верхний пробел не закрыт или явно помечен «неизвестно». Назвать пробел — не ошибка.\n");
        }
        return sb.toString().trim();
    }

    public static void detect(String task, String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        if (task == null || task.trim().length() < 40) return;
        List<Gap> gaps = gaps(task);
        if (gaps.isEmpty()) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        String top = gaps.get(0).text;
        boolean aCloses = closes(a) && !acknowledges(a, gaps);
        boolean bCloses = closes(b) && !acknowledges(b, gaps);
        add(conflicts, initial, aCloses || bCloses,
                "decision frame: вердикт закрывает выбор, не закрыв пробел кадра (" + top + ")");
        add(conflicts, initial, aCloses && bCloses,
                "decision frame: оба эксперта закрывают выбор при открытом пробеле кадра");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "decision frame (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    static List<Gap> gaps(String task) {
        List<Gap> gaps = new ArrayList<Gap>();
        SynthesisProtocol.Kind kind = SynthesisProtocol.classify(task);
        boolean decision = kind == SynthesisProtocol.Kind.DECISION;
        boolean forecast = kind == SynthesisProtocol.Kind.FORECAST;
        String text = lower(task);
        if (decision && options(task).size() < 2) {
            gaps.add(new Gap("options", "явные альтернативы не названы, включая оставить как есть"));
        }
        if ((decision || forecast) && magnitudes(task).isEmpty()) {
            gaps.add(new Gap("scale", "нет ни одной величины — масштаб ставки не задан"));
        }
        if ((decision || forecast) && !has(text,
                "день", "недел", "месяц", "год", "лет", "срок", "квартал", "горизонт",
                "deadline", "quarter", "сегодня", "немедленно", "сейчас")) {
            gaps.add(new Gap("horizon", "горизонт решения не задан"));
        }
        if (decision && !has(text,
                "кто несёт", "кто несет", "убыток", "downside", "потеря", "кто платит",
                "несёт риск", "несет риск", "skin in")) {
            gaps.add(new Gap("downside", "не сказано, кто несёт убыток, если рекомендация ошибочна"));
        }
        if (gaps.size() < MAX_GAPS && (forecast || has(text, "вероят", "шанс", "риск", "probability"))
                && !has(text, "базов", "base rate", "reference class", "похожих случа", "из 100", "историческ")) {
            gaps.add(new Gap("base", "нет базовой частоты похожих случаев"));
        }
        if (gaps.size() < MAX_GAPS && decision && !has(text,
                "критерий успеха", "метрика", "kpi", "чем измеря", "успех —", "success")) {
            gaps.add(new Gap("metric", "критерий успеха не назван"));
        }
        if (gaps.size() > MAX_GAPS) return gaps.subList(0, MAX_GAPS);
        return gaps;
    }

    private static boolean closes(String t) {
        return has(t,
                "поэтому делаем", "поэтому выбираем", "следует запустить",
                "рекомендую делать", "однозначно стоит", "закрываем выбор",
                "we should proceed", "therefore we should", "the choice is clear");
    }

    private static boolean acknowledges(String t, List<Gap> gaps) {
        if (has(t, "неизвестно", "сначала проверить", "decision frame", "пробел кадра")) return true;
        for (Gap gap : gaps) {
            if ("options".equals(gap.key) && has(t, "альтернатив", "третий вариант", "оставить как есть", "status quo")) return true;
            if ("scale".equals(gap.key) && has(t, "масштаб не задан", "величина неизвест", "ставка не назван")) return true;
            if ("horizon".equals(gap.key) && has(t, "горизонт", "срок не задан", "time horizon")) return true;
            if ("downside".equals(gap.key) && has(t, "кто несёт", "кто несет", "who bears", "downside")) return true;
            if ("base".equals(gap.key) && has(t, "базовая частота", "base rate", "reference class", "похожих случа")) return true;
            if ("metric".equals(gap.key) && has(t, "критерий успеха", "success metric", "чем измеря")) return true;
        }
        return false;
    }

    private static String falsifier(String key) {
        if ("options".equals(key)) return "назови второй живой вариант, включая «оставить как есть», и чем он хуже.";
        if ("scale".equals(key)) return "назови величину ставки (деньги, время или доля) — без неё сравнение бессмысленно.";
        if ("horizon".equals(key)) return "назови срок, после которого рекомендация перестаёт быть верной.";
        if ("downside".equals(key)) return "назови, чей ресурс сгорает, если рекомендация ошибочна.";
        if ("base".equals(key)) return "назови долю похожих случаев, где выбранный путь сработал, или напиши, что её нет.";
        if ("metric".equals(key)) return "назови измерение и порог, после которого рекомендацию следует отменить.";
        return "назови одно наблюдение, которое перевернёт вывод.";
    }

    private static String question(String task) {
        String[] lines = task.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            int q = trimmed.indexOf('?');
            int qFull = trimmed.indexOf('？');
            int cut = q < 0 ? qFull : (qFull < 0 ? q : Math.min(q, qFull));
            if (cut >= 0) return clip(trimmed.substring(0, cut + 1));
        }
        String flat = task.trim().replaceAll("\\s+", " ");
        int cut = -1;
        for (int i = 0; i < flat.length(); i++) {
            char c = flat.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                cut = i;
                break;
            }
        }
        if (cut >= 20) return clip(flat.substring(0, cut + 1));
        return clip(flat);
    }

    private static String clip(String text) {
        if (text.length() <= 220) return text;
        return text.substring(0, 217) + "…";
    }

    static List<String> options(String task) {
        List<String> out = new ArrayList<String>();
        Matcher between = BETWEEN.matcher(task);
        if (between.find()) {
            addOpt(out, between.group(1));
            addOpt(out, between.group(2));
        }
        if (out.size() < 2) {
            Matcher or = OR_SPLIT.matcher(task);
            if (or.find()) {
                out.clear();
                addOpt(out, or.group(1));
                addOpt(out, or.group(2));
            }
        }
        return out;
    }

    private static void addOpt(List<String> out, String raw) {
        if (raw == null) return;
        String cleaned = raw.replaceAll("(?iu)^(стоит ли|нужно ли|should we|should i)\\s+", "").trim();
        cleaned = cleaned.replaceAll("[?!.…,:;]+$", "").trim();
        if (cleaned.length() < 2 || cleaned.length() > 80) return;
        out.add(cleaned);
    }

    static List<String> magnitudes(String task) {
        List<String> out = new ArrayList<String>();
        Matcher m = MAGNITUDE.matcher(task);
        while (m.find() && out.size() < 6) {
            out.add(m.group(1) + " " + m.group(2));
        }
        return out;
    }

    private static String join(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append("; ");
            sb.append(items.get(i));
        }
        return sb.toString();
    }

    private static String label(SynthesisProtocol.Kind kind) {
        if (kind == SynthesisProtocol.Kind.FORECAST) return "прогноз";
        if (kind == SynthesisProtocol.Kind.DECISION) return "решение";
        if (kind == SynthesisProtocol.Kind.FACT) return "проверка факта";
        if (kind == SynthesisProtocol.Kind.DESIGN) return "проектирование";
        if (kind == SynthesisProtocol.Kind.EXPLANATION) return "объяснение";
        return "общий разбор";
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean has(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }
}
