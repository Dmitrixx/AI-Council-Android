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
 * and local arithmetic checks. No network and no Android types.
 */
public final class DeliberationIntelligence {
    private static final int MAX_SUBQUESTIONS = 6;
    private static final int MAX_CLAIMS = 12;
    private static final int MAX_CONFLICTS = 6;
    private static final int MAX_FAULTS = 6;
    private static final Pattern ITEM = Pattern.compile("(?i)^(?:\\d+[\\).]|[-*•])\\s+(.+)$");
    private static final Pattern PERCENT = Pattern.compile(
            "(?i)(\\d+(?:[.,]\\d+)?)\\s*%\\s*(?:от|of)\\s*(\\d+(?:[.,]\\d+)?)\\s*=\\s*(\\d+(?:[.,]\\d+)?)");
    private static final Pattern BINARY = Pattern.compile(
            "(\\d+(?:[.,]\\d+)?)\\s*([+\\-*/×÷])\\s*(\\d+(?:[.,]\\d+)?)\\s*=\\s*(\\d+(?:[.,]\\d+)?)");
    private static final Pattern NUMBER = Pattern.compile(
            "(?i)(\\d+(?:[.,]\\d+)?)(?:\\s*(%|₽|\\$|руб(?:лей|ля|\\.)?|usd|eur|кг|килограмм\\w*|км|километр\\w*|шт\\.?))?");

    private static final Set<String> STOP = new HashSet<String>();
    static {
        String[] words = {
                "этот", "этого", "этой", "этим", "которая", "который", "которые", "также", "только",
                "нужно", "будет", "можно", "нельзя", "около", "примерно", "ответ", "задача", "через",
                "после", "перед", "между", "если", "когда", "чтобы", "потому", "стоит", "надо"
        };
        for (String word : words) STOP.add(word);
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

        Brief(List<String> subquestions, List<String> conflicts, List<ArithmeticFault> arithmetic,
              List<String> missing, int scoreA, int scoreB) {
            this.subquestions = subquestions;
            this.conflicts = conflicts;
            this.arithmetic = arithmetic;
            this.missing = missing;
            this.scoreA = scoreA;
            this.scoreB = scoreB;
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

    public static Brief analyze(String task, String answerA, String answerB) {
        String a = answerA == null ? "" : answerA;
        String b = answerB == null ? "" : answerB;
        List<String> questions = subquestions(task);
        List<ArithmeticFault> faults = new ArrayList<ArithmeticFault>();
        faults.addAll(findArithmetic(a));
        faults.addAll(findArithmetic(b));
        if (faults.size() > MAX_FAULTS) faults = new ArrayList<ArithmeticFault>(faults.subList(0, MAX_FAULTS));
        List<String> conflicts = numericConflicts(a, b);
        List<String> missing = missingCoverage(questions, a, b);
        return new Brief(questions, conflicts, faults, missing, substance(a), substance(b));
    }

    public static String formatForCoordinator(Brief brief) {
        if (brief == null) return "";
        if (brief.conflicts.isEmpty() && brief.arithmetic.isEmpty() && brief.missing.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        out.append("Плотность ответа: ИИ №1 ").append(brief.scoreA).append("/100, ИИ №2 ").append(brief.scoreB).append("/100.\n");
        if (!brief.conflicts.isEmpty()) {
            out.append("ЧИСЛОВЫЕ РАСХОЖДЕНИЯ (проверь по тексту, не сглаживай):\n");
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
        out.append("Не цитируй эту карту как внешний источник. Если расхождение ложное, так и напиши.\n");
        String text = out.toString().trim();
        return text.length() > 2400 ? text.substring(0, 2400).trim() : text;
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
        boolean[] used = new boolean[text.length()];
        Matcher percent = PERCENT.matcher(text);
        while (percent.find() && faults.size() < MAX_FAULTS) {
            mark(used, percent.start(), percent.end());
            Double base = parse(percent.group(2));
            Double rate = parse(percent.group(1));
            Double claimed = parse(percent.group(3));
            if (base == null || rate == null || claimed == null) continue;
            double expected = base * rate / 100.0;
            if (!close(expected, claimed)) {
                faults.add(new ArithmeticFault(
                        trimExpr(percent.group()),
                        format(expected),
                        format(claimed)));
            }
        }
        Matcher binary = BINARY.matcher(text);
        while (binary.find() && faults.size() < MAX_FAULTS) {
            if (overlaps(used, binary.start(), binary.end())) continue;
            Double left = parse(binary.group(1));
            Double right = parse(binary.group(3));
            Double claimed = parse(binary.group(4));
            if (left == null || right == null || claimed == null) continue;
            String op = binary.group(2);
            if ((" /".trim().equals(op) || "÷".equals(op)) && Math.abs(right) < 1e-12) continue;
            Double expected = apply(left, op, right);
            if (expected == null || Double.isNaN(expected) || Double.isInfinite(expected)) continue;
            if (!close(expected, claimed)) {
                faults.add(new ArithmeticFault(trimExpr(binary.group()), format(expected), format(claimed)));
            }
        }
        return faults;
    }

    public static int substance(String answer) {
        if (answer == null) return 0;
        String trimmed = answer.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("Ошибка") || trimmed.startsWith("Не удалось")) return 0;
        int score = Math.min(40, trimmed.length() / 20);
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.contains("вывод") || lower.contains("\n-") || lower.contains("1.") || lower.contains("1)")) score += 20;
        if (lower.contains("уверен") || lower.contains("допущ") || lower.contains("неопредел") || lower.contains("%")) score += 15;
        if (lower.contains("факт") || lower.contains("источник") || lower.contains("расч") || lower.contains("формул")) score += 15;
        if (trimmed.length() > 400) score += 10;
        return Math.max(0, Math.min(100, score));
    }

    private static List<String> numericConflicts(String a, String b) {
        List<Claim> left = claims(a);
        List<Claim> right = claims(b);
        List<String> conflicts = new ArrayList<String>();
        for (Claim x : left) {
            for (Claim y : right) {
                if (conflicts.size() >= MAX_CONFLICTS) return conflicts;
                if (!sameUnit(x.unit, y.unit)) continue;
                int shared = shared(x.keywords, y.keywords);
                int need = x.unit.isEmpty() && y.unit.isEmpty() ? 2 : 1;
                if (shared < need) continue;
                if (close(x.value, y.value)) continue;
                conflicts.add("около «" + preview(x.keywords, y.keywords) + "»: "
                        + format(x.value) + unitSuffix(x.unit) + " против " + format(y.value) + unitSuffix(y.unit));
            }
        }
        return conflicts;
    }

    private static List<String> missingCoverage(List<String> questions, String a, String b) {
        List<String> missing = new ArrayList<String>();
        if (questions == null) return missing;
        String blob = (a + "\n" + b).toLowerCase(Locale.ROOT);
        for (String question : questions) {
            List<String> keys = keywords(question);
            int hits = 0;
            for (String key : keys) if (blob.contains(key)) hits++;
            if (keys.size() >= 2 && hits == 0) {
                missing.add(question.length() > 140 ? question.substring(0, 140).trim() : question);
            }
            if (missing.size() >= 4) break;
        }
        return missing;
    }

    private static final class Claim {
        final double value;
        final String unit;
        final List<String> keywords;
        Claim(double value, String unit, List<String> keywords) {
            this.value = value;
            this.unit = unit;
            this.keywords = keywords;
        }
    }

    private static List<Claim> claims(String text) {
        List<Claim> out = new ArrayList<Claim>();
        if (text == null || text.isEmpty()) return out;
        Matcher matcher = NUMBER.matcher(text);
        while (matcher.find() && out.size() < MAX_CLAIMS) {
            if (matcher.start() > 0) {
                char prev = text.charAt(matcher.start() - 1);
                if (Character.isLetterOrDigit(prev) || prev == '.' || prev == ',' || prev == '№' || prev == '#') continue;
            }
            Double value = parse(matcher.group(1));
            if (value == null) continue;
            String unit = matcher.group(2) == null ? "" : normalizeUnit(matcher.group(2));
            int from = Math.max(0, matcher.start() - 42);
            List<String> keys = keywords(text.substring(from, matcher.start()));
            if (keys.isEmpty()) continue;
            out.add(new Claim(value, unit, keys));
        }
        return out;
    }

    private static List<String> keywords(String text) {
        List<String> keys = new ArrayList<String>();
        if (text == null) return keys;
        String[] parts = text.toLowerCase(Locale.ROOT).split("[^\\p{L}]++");
        for (String part : parts) {
            if (part.length() < 5 || STOP.contains(part)) continue;
            if (!keys.contains(part)) keys.add(part);
            if (keys.size() >= 4) break;
        }
        return keys;
    }

    private static int shared(List<String> a, List<String> b) {
        int count = 0;
        for (String key : a) if (b.contains(key)) count++;
        return count;
    }

    private static String preview(List<String> a, List<String> b) {
        List<String> both = new ArrayList<String>();
        for (String key : a) if (b.contains(key)) both.add(key);
        Collections.sort(both, new Comparator<String>() {
            @Override public int compare(String left, String right) { return right.length() - left.length(); }
        });
        if (both.isEmpty()) return "число";
        return both.get(0);
    }

    private static boolean sameUnit(String a, String b) {
        return a.equals(b);
    }

    private static String normalizeUnit(String raw) {
        String unit = raw.toLowerCase(Locale.ROOT);
        if (unit.startsWith("руб") || "₽".equals(unit)) return "rub";
        if ("$".equals(unit) || "usd".equals(unit)) return "usd";
        if ("eur".equals(unit)) return "eur";
        if ("%".equals(unit)) return "%";
        if (unit.startsWith("кг") || unit.startsWith("килограмм")) return "kg";
        if (unit.startsWith("км") || unit.startsWith("километр")) return "km";
        if (unit.startsWith("шт")) return "pcs";
        return unit;
    }

    private static String unitSuffix(String unit) {
        if (unit == null || unit.isEmpty()) return "";
        if ("rub".equals(unit)) return " руб.";
        if ("usd".equals(unit)) return " USD";
        if ("eur".equals(unit)) return " EUR";
        if ("%".equals(unit)) return "%";
        if ("kg".equals(unit)) return " кг";
        if ("km".equals(unit)) return " км";
        if ("pcs".equals(unit)) return " шт.";
        return " " + unit;
    }

    private static Double apply(double left, String op, double right) {
        if ("+".equals(op)) return left + right;
        if ("-".equals(op) || "−".equals(op)) return left - right;
        if ("*".equals(op) || "×".equals(op)) return left * right;
        if ("/".equals(op) || "÷".equals(op)) return left / right;
        return null;
    }

    private static boolean close(double expected, double claimed) {
        double tol = Math.max(0.011, Math.abs(expected) * 0.001);
        return Math.abs(expected - claimed) <= tol;
    }

    private static Double parse(String raw) {
        if (raw == null) return null;
        try {
            return Double.valueOf(raw.replace(',', '.'));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.001) return String.valueOf((long) Math.rint(value));
        return String.format(Locale.US, "%.2f", value);
    }

    private static String trimExpr(String raw) {
        String value = raw == null ? "" : raw.replace('\n', ' ').trim();
        return value.length() > 80 ? value.substring(0, 80).trim() : value;
    }

    private static void mark(boolean[] used, int start, int end) {
        int to = Math.min(used.length, end);
        for (int i = Math.max(0, start); i < to; i++) used[i] = true;
    }

    private static boolean overlaps(boolean[] used, int start, int end) {
        int to = Math.min(used.length, end);
        for (int i = Math.max(0, start); i < to; i++) if (used[i]) return true;
        return false;
    }

    private static void addUnique(List<String> found, String value) {
        if (value == null) return;
        String cleaned = clean(value);
        if (cleaned.length() < 8 || found.contains(cleaned) || found.size() >= MAX_SUBQUESTIONS) return;
        found.add(cleaned);
    }

    private static String clean(String value) {
        return value == null ? "" : value.replace(String.valueOf((char) 0), "").trim();
    }
}
