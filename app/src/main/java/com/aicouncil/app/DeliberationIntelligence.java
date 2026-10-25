package com.aicouncil.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic council brain: shared sub-questions, numeric disagreements,
 * qualitative polarity conflicts and local arithmetic checks. No network and no Android types.
 * Enhanced with Jaccard-based agreement scoring for better calibration.
 */
public final class DeliberationIntelligence {
    private static final int MAX_SUBQUESTIONS = 6;
    private static final int MAX_CLAIMS = 12;
    private static final int MAX_CONFLICTS = 6;
    private static final int MAX_FAULTS = 6;
    private static final Pattern ITEM = Pattern.compile("(?i)^(?:\\d+[\\).]|[-*\u2022])\\s+(.+)$");
    private static final Pattern PERCENT = Pattern.compile(
            "(?i)(\\d+(?:[.,]\\d+)?)\\s*%\\s*(?:от|of)\\s*(\\d+(?:[.,]\\d+)?)\\s*=\\s*(\\d+(?:[.,]\\d+)?)");
    private static final Pattern BINARY = Pattern.compile(
            "(\\d+(?:[.,]\\d+)?)\\s*([+\\-*/\u00d7\u00f7])\\s*(\\d+(?:[.,]\\d+)?)\\s*=\\s*(\\d+(?:[.,]\\d+)?)");
    private static final Pattern NUMBER = Pattern.compile(
            "(?i)(\\d+(?:[.,]\\d+)?)(?:\\s*(%|₽|\\$|руб(?:лей|ля|\\.)?|usd|eur|кг|килограмм\\w*|км|километр\\w*|шт\\.?))?");

    private static final Set<String> STOP = new HashSet<String>();
    private static final Set<String> POSITIVE = new HashSet<String>();
    private static final Set<String> NEGATIVE = new HashSet<String>();
    static {
        String[] words = {
                "этот", "этого", "этой", "этим", "которая", "который", "которые", "также", "только",
                "нужно", "будет", "можно", "нельзя", "около", "примерно", "ответ", "задача", "через",
                "после", "перед", "между", "если", "когда", "чтобы", "потому", "стоит", "надо"
        };
        for (String word : words) STOP.add(word);
        String[] pos = {"рост", "увеличение", "положительный", "высокий", "лучше", "рекомендуется", "поддерживаю", "согласен"};
        for (String word : pos) POSITIVE.add(word);
        String[] neg = {"падение", "уменьшение", "отрицательный", "низкий", "хуже", "не рекомендуется", "против", "не согласен"};
        for (String word : neg) NEGATIVE.add(word);
    }

    private DeliberationIntelligence() {}

    public static final class ArithmeticFault {
        public final String expression;
        public final String expected;
        public final String claimed;

        ArithmeticFault(String expression, String expected, String claimed) {
            this.expression = expression;
            this.expected = expected;
            this.claimed = claimed;
        }
    }

    public static final class Brief {
        public final List<String> subquestions;
        public final List<String> conflicts;
        public final List<ArithmeticFault> arithmetic;
        public final List<String> missing;
        public final int scoreA;
        public final int scoreB;
        public final int agreementScore;

        Brief(List<String> subquestions, List<String> conflicts, List<ArithmeticFault> arithmetic,
              List<String> missing, int scoreA, int scoreB, int agreementScore) {
            this.subquestions = subquestions;
            this.conflicts = conflicts;
            this.arithmetic = arithmetic;
            this.missing = missing;
            this.scoreA = scoreA;
            this.scoreB = scoreB;
            this.agreementScore = agreementScore;
        }
    }

    public static List<String> subquestions(String task) {
        List<String> found = new ArrayList<String>();
        if (task == null) return found;
        String[] lines = task.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line == null ? "" : line.trim();
            if (trimmed.length() < 8) continue;
            Matcher item = ITEM.matcher(trimmed);
            if (item.matches()) addUnique(found, clean(item.group(1)));
            else if (trimmed.endsWith("?")) addUnique(found, clean(trimmed));
            if (found.size() >= MAX_SUBQUESTIONS) return found;
        }
        if (found.size() >= 2) return found;
        if (task.trim().length() > 180) {
            String[] sentences = task.split("(?<=[\\.!\\?])\\s+");
            for (String sentence : sentences) {
                String trimmed = clean(sentence);
                if (trimmed.length() < 40) continue;
                addUnique(found, trimmed.length() > 180 ? trimmed.substring(0, 180).trim() : trimmed);
                if (found.size() >= 4) break;
            }
        }
        return found;
    }

    public static String expertAddendum(int slot, String task) {
        List<String> questions = subquestions(task);
        if (questions.size() < 2) return "";
        StringBuilder out = new StringBuilder();
        out.append("\n\nОБЩИЕ ПОДВОПРОСЫ (ответь на каждый отдельно, одним и тем же порядком; это карта сравнения, а не новые факты):\n");
        for (int i = 0; i < questions.size(); i++) {
            out.append(i + 1).append(". ").append(questions.get(i)).append('\n');
        }
        if (slot == 1) {
            out.append("Используй другой метод, чем самый очевидный, и явно отметь, где твой вывод расходится с напрашивающимся ответом.\n");
        } else if (slot == 0) {
            out.append("Сначала прямой ответ на каждый подвопрос, затем общая рекомендация.\n");
        }
        return out.toString();
    }

    public static Brief analyze(String task, String a, String b) {
        List<String> questions = subquestions(task);
        List<ArithmeticFault> faults = new ArrayList<ArithmeticFault>();
        faults.addAll(findArithmetic(a));
        faults.addAll(findArithmetic(b));
        if (faults.size() > MAX_FAULTS) faults = new ArrayList<ArithmeticFault>(faults.subList(0, MAX_FAULTS));
        List<String> conflicts = numericConflicts(a, b);
        conflicts.addAll(qualitativeConflicts(a, b));
        if (conflicts.size() > MAX_CONFLICTS) conflicts = new ArrayList<String>(conflicts.subList(0, MAX_CONFLICTS));
        List<String> missing = missingCoverage(questions, a, b);
        int agreement = computeAgreementScore(a, b, conflicts.size());
        return new Brief(questions, conflicts, faults, missing, substance(a), substance(b), agreement);
    }

    public static String formatForCoordinator(Brief brief) {
        if (brief == null) return "";
        if (brief.conflicts.isEmpty() && brief.arithmetic.isEmpty() && brief.missing.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        out.append("Плотность ответа: ИИ №1 ").append(brief.scoreA).append("/100, ИИ №2 ").append(brief.scoreB).append("/100. ");
        out.append("Оценка согласованности (Jaccard + конфликты): ").append(brief.agreementScore).append("/100.\n");
        if (!brief.conflicts.isEmpty()) {
            out.append("РАСХОЖДЕНИЯ (числовые и качественные полярные; проверь по тексту, не сглаживай):\n");
            for (String conflict : brief.conflicts) out.append("- ").append(conflict).append('\n');
        }
        if (!brief.arithmetic.isEmpty()) {
            out.append("ЛОКАЛЬНО ПРОВЕРЕННАЯ АРИФМЕТИКА (посчитано приложением):\n");
            for (ArithmeticFault fault : brief.arithmetic) {
                out.append("- ").append(fault.expression).append(" заявлено ").append(fault.claimed)
                        .append(", ожидается ").append(fault.expected).append('\n');
            }
        }
        if (!brief.missing.isEmpty()) {
            out.append("ПОДВОПРОСЫ БЕЗ ЯВНОГО ПОКРЫТИЯ:\n");
            for (String miss : brief.missing) out.append("- ").append(miss).append('\n');
        }
        out.append("Не цитируй эту карту как внешний источник. Если расхождение ложное, так и напиши. Используй оценку согласованности для калибровки итога.\n");
        String text = out.toString().trim();
        return text.length() > 2800 ? text.substring(0, 2800).trim() : text;
    }

    public static String localCheckFooter(String text) {
        List<ArithmeticFault> faults = findArithmetic(text);
        if (faults.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        out.append("🧮 ЛОКАЛЬНАЯ ПРОВЕРКА АРИФМЕТИКИ\n");
        out.append("Приложение пересчитало выражения в тексте. Это не мнение модели.\n");
        int limit = Math.min(MAX_FAULTS, faults.size());
        for (int i = 0; i < limit; i++) {
            ArithmeticFault fault = faults.get(i);
            out.append("• ").append(fault.expression).append(" — заявлено ").append(fault.claimed)
                    .append(", получается ").append(fault.expected).append('\n');
        }
        return out.toString().trim();
    }

    public static List<ArithmeticFault> findArithmetic(String text) {
        List<ArithmeticFault> faults = new ArrayList<ArithmeticFault>();
        if (text == null || text.isEmpty()) return faults;
        Matcher m = PERCENT.matcher(text);
        while (m.find() && faults.size() < MAX_FAULTS) {
            double pct = parse(m.group(1));
            double base = parse(m.group(2));
            double claimed = parse(m.group(3));
            double expected = base * pct / 100.0;
            if (Math.abs(expected - claimed) > 0.01 * Math.max(1.0, Math.abs(expected))) {
                faults.add(new ArithmeticFault(m.group(0).trim(), formatNum(expected), formatNum(claimed)));
            }
        }
        m = BINARY.matcher(text);
        while (m.find() && faults.size() < MAX_FAULTS) {
            double left = parse(m.group(1));
            String op = m.group(2);
            double right = parse(m.group(3));
            double claimed = parse(m.group(4));
            double expected;
            if ("+".equals(op)) expected = left + right;
            else if ("-".equals(op)) expected = left - right;
            else if ("*".equals(op) || "\u00d7".equals(op)) expected = left * right;
            else if ("/".equals(op) || "\u00f7".equals(op)) expected = right == 0 ? Double.NaN : left / right;
            else continue;
            if (Double.isNaN(expected)) continue;
            if (Math.abs(expected - claimed) > 0.01 * Math.max(1.0, Math.abs(expected))) {
                faults.add(new ArithmeticFault(m.group(0).trim(), formatNum(expected), formatNum(claimed)));
            }
        }
        return faults;
    }

    private static int substance(String text) {
        if (text == null) return 0;
        String trimmed = text.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("Ошибка") || trimmed.startsWith("Не удалось")) return 0;
        int words = trimmed.split("\\s+").length;
        return Math.min(100, words * 2);
    }

    private static int computeAgreementScore(String a, String b, int conflictCount) {
        if (a == null) a = "";
        if (b == null) b = "";
        if (a.isEmpty() || b.isEmpty()) return 0;
        Set<String> wordsA = significantWords(a);
        Set<String> wordsB = significantWords(b);
        if (wordsA.isEmpty() || wordsB.isEmpty()) return 40;
        int intersection = 0;
        for (String w : wordsA) if (wordsB.contains(w)) intersection++;
        int union = wordsA.size() + wordsB.size() - intersection;
        double jaccard = union == 0 ? 0 : (double) intersection / union;
        int base = (int) (jaccard * 100);
        base -= conflictCount * 12;
        return Math.max(0, Math.min(100, base));
    }

    private static Set<String> significantWords(String text) {
        Set<String> set = new HashSet<String>();
        if (text == null) return set;
        String[] parts = text.toLowerCase(Locale.ROOT).split("[^\\p{L}]+");
        for (String part : parts) {
            if (part.length() >= 5 && !STOP.contains(part)) set.add(part);
        }
        return set;
    }

    private static List<String> qualitativeConflicts(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        String la = a == null ? "" : a.toLowerCase(Locale.ROOT);
        String lb = b == null ? "" : b.toLowerCase(Locale.ROOT);
        boolean posA = hasAny(la, POSITIVE);
        boolean negA = hasAny(la, NEGATIVE);
        boolean posB = hasAny(lb, POSITIVE);
        boolean negB = hasAny(lb, NEGATIVE);
        if ((posA && negB) || (negA && posB)) {
            conflicts.add("Качественная полярность: один ответ более положительный/рост, другой отрицательный/падение по ключевым темам");
        }
        return conflicts;
    }

    private static boolean hasAny(String text, Set<String> set) {
        for (String w : set) if (text.contains(w)) return true;
        return false;
    }

    private static List<String> numericConflicts(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        List<NumClaim> claimsA = extractClaims(a);
        List<NumClaim> claimsB = extractClaims(b);
        for (NumClaim x : claimsA) {
            for (NumClaim y : claimsB) {
                if (conflicts.size() >= MAX_CONFLICTS) return conflicts;
                if (Math.abs(x.value - y.value) < 0.01 * Math.max(1.0, Math.abs(x.value))) continue;
                int need = x.unit.isEmpty() && y.unit.isEmpty() ? 2 : 1;
                int overlap = overlapCount(x.keywords, y.keywords);
                if (overlap < need) continue;
                if (!x.unit.isEmpty() && !y.unit.isEmpty() && !x.unit.equals(y.unit)) continue;
                conflicts.add("около «" + preview(x.keywords, y.keywords) + "»: "
                        + formatNum(x.value) + x.unit + " vs " + formatNum(y.value) + y.unit);
            }
        }
        return conflicts;
    }

    private static List<String> missingCoverage(List<String> questions, String a, String b) {
        List<String> missing = new ArrayList<String>();
        if (questions == null) return missing;
        String joined = ((a == null ? "" : a) + " " + (b == null ? "" : b)).toLowerCase(Locale.ROOT);
        for (String question : questions) {
            String q = question.toLowerCase(Locale.ROOT);
            String[] tokens = q.split("\\s+");
            int hits = 0;
            for (String t : tokens) {
                if (t.length() < 4 || STOP.contains(t)) continue;
                if (joined.contains(t)) hits++;
            }
            if (hits < 2) {
                missing.add(question.length() > 140 ? question.substring(0, 140).trim() : question);
            }
            if (missing.size() >= 4) break;
        }
        return missing;
    }

    private static final class NumClaim {
        final double value;
        final String unit;
        final List<String> keywords;
        NumClaim(double value, String unit, List<String> keywords) {
            this.value = value;
            this.unit = unit;
            this.keywords = keywords;
        }
    }

    private static List<NumClaim> extractClaims(String text) {
        List<NumClaim> out = new ArrayList<NumClaim>();
        if (text == null || text.isEmpty()) return out;
        Matcher m = NUMBER.matcher(text);
        while (m.find() && out.size() < MAX_CLAIMS) {
            double value = parse(m.group(1));
            String unit = normalizeUnit(m.group(2));
            int start = Math.max(0, m.start() - 40);
            int end = Math.min(text.length(), m.end() + 20);
            String ctx = text.substring(start, end).toLowerCase(Locale.ROOT);
            List<String> keys = new ArrayList<String>();
            for (String w : ctx.split("[^\\p{L}\\p{N}]+")) {
                if (w.length() >= 3 && !STOP.contains(w)) keys.add(w);
            }
            if (keys.isEmpty()) continue;
            out.add(new NumClaim(value, unit, keys));
        }
        return out;
    }

    private static int overlapCount(List<String> a, List<String> b) {
        Set<String> set = new HashSet<String>(a);
        int n = 0;
        for (String x : b) if (set.contains(x)) n++;
        return n;
    }

    private static String preview(List<String> a, List<String> b) {
        Set<String> both = new HashSet<String>(a);
        both.retainAll(b);
        if (both.isEmpty()) return "число";
        List<String> list = new ArrayList<String>(both);
        Collections.sort(list, Comparator.comparingInt(String::length).reversed());
        return list.get(0);
    }

    private static double parse(String s) {
        if (s == null) return 0;
        return Double.parseDouble(s.replace(',', '.'));
    }

    private static String formatNum(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-6) return String.valueOf((long) Math.rint(v));
        return String.format(Locale.US, "%.2f", v);
    }

    private static String normalizeUnit(String unit) {
        if (unit == null || unit.isEmpty()) return "";
        String u = unit.toLowerCase(Locale.ROOT);
        if (u.startsWith("руб")) return " руб";
        if (u.equals("%")) return "%";
        if (u.equals("$") || u.equals("usd")) return " $";
        if (u.equals("eur")) return " \u20ac";
        return " " + u;
    }

    private static void addUnique(List<String> list, String value) {
        if (value == null || value.isEmpty()) return;
        for (String existing : list) if (existing.equalsIgnoreCase(value)) return;
        list.add(value);
    }

    private static String clean(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", " ").trim();
    }
}
