package com.aicouncil.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local (no-network) deliberation intelligence for the AI Council.
 * Detects disagreements, arithmetic faults, calibration issues, evidence density,
 * residual risk ranges, polarity, directional conflicts and more to give the
 * coordinator a precise map.
 */
public final class DeliberationIntelligence {
    public static final class ArithmeticFault {
        public final String expression;
        public final String expected;
        public ArithmeticFault(String expression, String expected) {
            this.expression = expression == null ? "" : expression;
            this.expected = expected == null ? "" : expected;
        }
    }

    public static final class Brief {
        public final List<String> conflicts;
        public final List<String> missing;
        public final int scoreA;
        public final int scoreB;
        public final int hedgeRatio;
        public final int agreement;
        public final int evidenceA;
        public final int evidenceB;
        public final List<String> qualityGaps;
        public Brief(List<String> conflicts, List<String> missing, int scoreA, int scoreB,
                     int hedgeRatio, int agreement, int evidenceA, int evidenceB, List<String> qualityGaps) {
            this.conflicts = conflicts == null ? Collections.emptyList() : conflicts;
            this.missing = missing == null ? Collections.emptyList() : missing;
            this.scoreA = scoreA;
            this.scoreB = scoreB;
            this.hedgeRatio = hedgeRatio;
            this.agreement = agreement;
            this.evidenceA = evidenceA;
            this.evidenceB = evidenceB;
            this.qualityGaps = qualityGaps == null ? Collections.emptyList() : qualityGaps;
        }
    }

    private DeliberationIntelligence() {}

    // ---------- Public API ----------

    public static List<ArithmeticFault> findArithmetic(String text) {
        List<ArithmeticFault> faults = new ArrayList<>();
        if (text == null || text.isEmpty()) return faults;
        Pattern plus = Pattern.compile("(\\d+)\\s*\\+\\s*(\\d+)\\s*=\\s*(\\d+)");
        Matcher m = plus.matcher(text);
        while (m.find()) {
            long a = Long.parseLong(m.group(1)), b = Long.parseLong(m.group(2)), c = Long.parseLong(m.group(3));
            if (a + b != c) faults.add(new ArithmeticFault(m.group(0), String.valueOf(a + b)));
        }
        Pattern mul = Pattern.compile("(\\d+)\\s*\\*\\s*(\\d+)\\s*=\\s*(\\d+)");
        m = mul.matcher(text);
        while (m.find()) {
            long a = Long.parseLong(m.group(1)), b = Long.parseLong(m.group(2)), c = Long.parseLong(m.group(3));
            if (a * b != c) faults.add(new ArithmeticFault(m.group(0), String.valueOf(a * b)));
        }
        Pattern pct = Pattern.compile("(\\d+)\\s*%\\s*от\\s*(\\d+)\\s*=\\s*(\\d+)");
        m = pct.matcher(text);
        while (m.find()) {
            long p = Long.parseLong(m.group(1)), base = Long.parseLong(m.group(2)), claimed = Long.parseLong(m.group(3));
            long expected = base * p / 100;
            if (expected != claimed) faults.add(new ArithmeticFault(m.group(0), String.valueOf(expected)));
        }
        // subtraction
        Pattern minus = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)\\s*=\\s*(\\d+)");
        m = minus.matcher(text);
        while (m.find()) {
            long a = Long.parseLong(m.group(1)), b = Long.parseLong(m.group(2)), c = Long.parseLong(m.group(3));
            if (a - b != c) faults.add(new ArithmeticFault(m.group(0), String.valueOf(a - b)));
        }
        // division (integer)
        Pattern div = Pattern.compile("(\\d+)\\s*[\\/÷]\\s*(\\d+)\\s*=\\s*(\\d+)");
        m = div.matcher(text);
        while (m.find()) {
            long a = Long.parseLong(m.group(1)), b = Long.parseLong(m.group(2)), c = Long.parseLong(m.group(3));
            if (b != 0 && a / b != c) faults.add(new ArithmeticFault(m.group(0), String.valueOf(a / b)));
        }
        Pattern pctEn = Pattern.compile("(?i)(\\d+)\\s*%\\s*of\\s*(\\d+)\\s*=\\s*(\\d+)");
        m = pctEn.matcher(text);
        while (m.find()) {
            long p = Long.parseLong(m.group(1)), base = Long.parseLong(m.group(2)), claimed = Long.parseLong(m.group(3));
            long expected = base * p / 100;
            if (expected != claimed) faults.add(new ArithmeticFault(m.group(0), String.valueOf(expected)));
        }
        Pattern pctUk = Pattern.compile("(\\d+)\\s*%\\s*від\\s*(\\d+)\\s*=\\s*(\\d+)");
        m = pctUk.matcher(text);
        while (m.find()) {
            long p = Long.parseLong(m.group(1)), base = Long.parseLong(m.group(2)), claimed = Long.parseLong(m.group(3));
            long expected = base * p / 100;
            if (expected != claimed) faults.add(new ArithmeticFault(m.group(0), String.valueOf(expected)));
        }
        Pattern times = Pattern.compile("(\\d+)\\s*[×xX]\\s*(\\d+)\\s*=\\s*(\\d+)");
        m = times.matcher(text);
        while (m.find()) {
            long a = Long.parseLong(m.group(1)), b = Long.parseLong(m.group(2)), c = Long.parseLong(m.group(3));
            if (a * b != c) faults.add(new ArithmeticFault(m.group(0), String.valueOf(a * b)));
        }
        scanWord(text, faults, "(\\d+)\\s+плюс\\s+(\\d+)\\s*(?:=|равно)\\s*(\\d+)", 1);
        scanWord(text, faults, "(\\d+)\\s+минус\\s+(\\d+)\\s*(?:=|равно)\\s*(\\d+)", 2);
        scanWord(text, faults, "(\\d+)\\s+умножить\\s+на\\s+(\\d+)\\s*(?:=|равно)\\s*(\\d+)", 3);
        scanWord(text, faults, "(\\d+)\\s+разделить\\s+на\\s+(\\d+)\\s*(?:=|равно)\\s*(\\d+)", 4);
        return faults;
    }

    /** op: 1 add, 2 sub, 3 mul, 4 div. */
    private static void scanWord(String text, List<ArithmeticFault> faults, String regex, int op) {
        Matcher m = Pattern.compile(regex).matcher(text);
        while (m.find()) {
            long a = Long.parseLong(m.group(1)), b = Long.parseLong(m.group(2)), c = Long.parseLong(m.group(3));
            long expected;
            if (op == 1) expected = a + b;
            else if (op == 2) expected = a - b;
            else if (op == 3) expected = a * b;
            else expected = b == 0 ? c : a / b;
            if (op == 4 && b == 0) continue;
            if (expected != c) faults.add(new ArithmeticFault(m.group(0), String.valueOf(expected)));
        }
    }

    public static String localCheckFooter(String text) {
        List<ArithmeticFault> faults = findArithmetic(text);
        if (faults.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("⚠️ ЛОКАЛЬНАЯ ПРОВЕРКА АРИФМЕТИКИ (приложение):\n");
        for (ArithmeticFault f : faults) {
            sb.append("• ").append(f.expression).append(" → ожидалось ").append(f.expected).append("\n");
        }
        return sb.toString().trim();
    }

    public static List<String> subquestions(String task) {
        List<String> qs = new ArrayList<>();
        if (task == null) return qs;
        String[] lines = task.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.matches("^\\d+\\.\\s+.+") && line.length() > 5) {
                qs.add(line);
            }
        }
        return qs;
    }

    public static String expertAddendum(int slot, String task) {
        List<String> qs = subquestions(task);
        if (qs.isEmpty() || task.length() < 40) return "";
        StringBuilder sb = new StringBuilder("\n\nОБЩИЕ ПОДВОПРОСЫ (для согласованного разбора):\n");
        for (String q : qs) sb.append("- ").append(q).append("\n");
        if (slot == 1) sb.append("\nИспользуй другой метод / другую ветвь рассуждения относительно первого эксперта.");
        return sb.toString();
    }

    public static Brief analyze(String task, String answerA, String answerB) {
        String a = safe(answerA);
        String b = safe(answerB);
        List<String> conflicts = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        int scoreA = substanceScore(a);
        int scoreB = substanceScore(b);
        int evidenceA = evidenceDensity(a);
        int evidenceB = evidenceDensity(b);

        if (isFailure(a)) scoreA = 0;
        if (isFailure(b)) scoreB = 0;

        detectNumericConflicts(a, b, conflicts);
        detectScaleConflicts(a, b, conflicts);
        detectPolarity(a, b, conflicts);
        directionalConflicts(a.toLowerCase(Locale.ROOT), b.toLowerCase(Locale.ROOT), conflicts);
        detectConfidence(a, b, conflicts);
        detectResidualRisk(a, b, conflicts);
        detectCausation(a, b, conflicts);
        detectHorizon(a, b, conflicts);
        detectYears(a, b, conflicts);
        detectReversibility(a, b, task, conflicts);
        detectScope(a, b, conflicts);
        detectOptionChoice(a, b, conflicts);
        detectMissingStakeholders(task, a, b, missing);
        detectUnitClash(a, b, conflicts);
        detectPremiseFork(a, b, conflicts);
        detectExclusivity(a, b, conflicts);
        detectCalibration(a, b, evidenceA, evidenceB, conflicts);
        detectMissingAlternatives(task, a, b, missing);
        detectConditionalFork(a, b, conflicts);
        detectQuantifierClash(a, b, conflicts);
        detectMetricKind(a, b, conflicts);
        rankConflicts(conflicts);

        int hedge = hedgeScore(a + " " + b);
        int agreement = jaccardAgreement(a, b);

        List<String> qs = subquestions(task);
        for (String q : qs) {
            String key = q.toLowerCase(Locale.ROOT);
            int len = Math.min(12, key.length());
            if (len > 3 && !a.toLowerCase(Locale.ROOT).contains(key.substring(0, len)) &&
                !b.toLowerCase(Locale.ROOT).contains(key.substring(0, len))) {
                missing.add(q);
            }
        }

        if (Math.abs(evidenceA - evidenceB) >= 25 && Math.max(evidenceA, evidenceB) >= 40) {
            conflicts.add("доказательства: сильный разрыв в количественной опоре (" + evidenceA + " vs " + evidenceB + ")");
        }

        List<String> gaps = qualityGaps(a, b);
        return new Brief(conflicts, missing, scoreA, scoreB, hedge, agreement, evidenceA, evidenceB, gaps);
    }

    public static String formatForCoordinator(Brief brief) {
        if (brief == null) return "";
        if (brief.conflicts.isEmpty() && brief.missing.isEmpty() && brief.qualityGaps.isEmpty() && brief.agreement > 70) return "";
        StringBuilder sb = new StringBuilder("ЛОКАЛЬНАЯ КАРТА РАЗНОГЛАСИЙ:\n");
        if (!brief.conflicts.isEmpty()) {
            sb.append("РАСХОЖДЕНИЯ:\n");
            for (String c : brief.conflicts) sb.append("• ").append(c).append("\n");
        }
        if (!brief.missing.isEmpty()) {
            sb.append("НЕ ПОКРЫТЫЕ ПОДВОПРОСЫ:\n");
            for (String m : brief.missing) sb.append("• ").append(m).append("\n");
        }
        sb.append("Оценка плотности: A=").append(brief.scoreA).append(", B=").append(brief.scoreB).append("\n");
        sb.append("Evidence density: A=").append(brief.evidenceA).append(", B=").append(brief.evidenceB).append("\n");
        sb.append("Согласованность (Jaccard): ").append(brief.agreement).append("%\n");
        sb.append("Hedge/absolute: ").append(brief.hedgeRatio).append(" (0=категорично, 100=оговорочно)\n");
        if (!brief.qualityGaps.isEmpty()) {
            sb.append("ПРОБЕЛЫ КАЧЕСТВА:\n");
            for (String gap : brief.qualityGaps) sb.append("• ").append(gap).append("\n");
        }
        sb.append(resolutionLine(brief)).append("\n");
        sb.append("Не усредняй метки residual risk, доказательства, масштаб, выбор, единицы, валюту, предпосылки, исключительность и направленные конфликты. ");
        sb.append("Высокую уверенность при слабой опоре не переноси в итог как факт. ");
        sb.append("Если назван пропущенный стейкхолдер — скажи, как вывод его затрагивает или что данных нет. ");
        sb.append("Условную рекомендацию не превращай в безусловную. Квантор «все» не сужай и не расширяй до «некоторые». Проценты и процентные пункты не считай одной метрикой. ");
        sb.append("Предпочитай ответы с более высокой evidence density; при разрыве ≥20 явно укажи предпочтение.\n");
        return sb.toString().trim();
    }

    /** One-line instruction: which crux to resolve before any average. */
    public static String resolutionLine(Brief brief) {
        if (brief == null || brief.conflicts.isEmpty()) {
            return "РЕШЕНИЕ КАРТЫ: явных конфликтов нет; не повышай уверенность из-за похожих формулировок.";
        }
        String top = brief.conflicts.get(0);
        String prefer = "";
        int gap = Math.abs(brief.evidenceA - brief.evidenceB);
        if (gap >= 20) {
            if (brief.evidenceA > brief.evidenceB) {
                prefer = " Количественная опора сильнее у ИИ №1 (" + brief.evidenceA + " против " + brief.evidenceB + ").";
            } else {
                prefer = " Количественная опора сильнее у ИИ №2 (" + brief.evidenceB + " против " + brief.evidenceA + ").";
            }
        }
        return "РЕШЕНИЕ КАРТЫ: не усредняй. Сначала разреши «" + top + "»." + prefer;
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static boolean isFailure(String s) {
        String l = s.toLowerCase(Locale.ROOT);
        return s.length() < 20 || l.contains("ошибка api") || l.contains("timeout") || l.contains("не удалось");
    }

    private static int substanceScore(String text) {
        if (text.length() < 30) return 5;
        int score = Math.min(40, text.length() / 25);
        if (text.matches(".*\\d+.*")) score += 15;
        if (text.toLowerCase(Locale.ROOT).contains("потому") || text.contains("потому что")) score += 10;
        if (text.contains("%") || text.contains("диапазон") || text.contains("–")) score += 10;
        return Math.min(100, score);
    }

    private static int evidenceDensity(String text) {
        int score = 0;
        String l = text.toLowerCase(Locale.ROOT);
        if (l.matches(".*\\d+.*")) score += 20;
        if (l.contains("%")) score += 15;
        if (l.contains("диапазон") || l.contains("–") || l.contains("-")) score += 10;
        if (l.contains("источник") || l.contains("ссылк") || l.contains("http") || l.contains("source")) score += 15;
        if (l.contains("residual") || l.contains("остаточн")) score += 15;
        if (l.contains("pre-mortem") || l.contains("премортем") || l.contains("провал") || l.contains("failure mode")) score += 10;
        if (l.contains("base rate") || l.contains("базов") || l.contains("reference class")) score += 10;
        if (l.contains("верификац") || l.contains("провер") || l.contains("verify")) score += 5;
        if (l.contains("[подтверждено]") || l.contains("[выведено]") || l.contains("подтверждено:") || l.contains("выведено:")) score += 10;
        if (l.contains("=") && l.matches(".*\\d+\\s*[=+\\-*/]\\s*\\d+.*")) score += 10; // formulas
        if (l.contains("исследован") || l.contains("study") || l.contains("данные показывают")) score += 5;
        return Math.min(100, score);
    }

    private static int hedgeScore(String text) {
        String l = text.toLowerCase(Locale.ROOT);
        int hedges = count(l, "возможно", "вероятно", "может", "скорее", "приблизительно", "около", "примерно", "не уверен", "допустим");
        int absolutes = count(l, "точно", "определённо", "гарантированно", "всегда", "никогда", "точно знаю", "100%");
        if (hedges + absolutes == 0) return 50;
        return Math.min(100, (hedges * 100) / (hedges + absolutes));
    }

    private static int count(String text, String... words) {
        int c = 0;
        for (String w : words) if (text.contains(w)) c++;
        return c;
    }

    private static int jaccardAgreement(String a, String b) {
        Set<String> sa = significantWords(a);
        Set<String> sb = significantWords(b);
        if (sa.isEmpty() || sb.isEmpty()) return 0;
        Set<String> inter = new HashSet<>(sa);
        inter.retainAll(sb);
        Set<String> union = new HashSet<>(sa);
        union.addAll(sb);
        return (int) (100.0 * inter.size() / union.size());
    }

    private static Set<String> significantWords(String text) {
        Set<String> set = new HashSet<>();
        if (text == null) return set;
        String[] parts = text.toLowerCase(Locale.ROOT).replaceAll("[^а-яёa-z0-9\\s]", " ").split("\\s+");
        Set<String> stop = new HashSet<>(Arrays.asList("это", "как", "для", "или", "что", "при", "его", "ее", "них", "они", "the", "and", "for", "with"));
        for (String p : parts) if (p.length() > 3 && !stop.contains(p)) set.add(p);
        return set;
    }

    private static void detectNumericConflicts(String a, String b, List<String> conflicts) {
        Pattern num = Pattern.compile("(\\d+[\\s,.]*\\d*)\\s*(руб|рублей|usd|долл|%|процент)");
        List<String> na = extractNumbers(a, num);
        List<String> nb = extractNumbers(b, num);
        if (!na.isEmpty() && !nb.isEmpty() && !na.get(0).equals(nb.get(0))) {
            conflicts.add("число: разные оценки (" + na.get(0) + " vs " + nb.get(0) + ")");
        }
        try {
            double da = firstNumber(a), db = firstNumber(b);
            if (da > 0 && db > 0 && Math.abs(da - db) / Math.max(da, db) > 0.05) {
                if (!conflicts.toString().contains("число")) {
                    conflicts.add("число: относительное расхождение >5% (" + (int)da + " vs " + (int)db + ")");
                }
            }
        } catch (Exception ignored) {}
    }

    private static void detectScaleConflicts(String a, String b, List<String> conflicts) {
        try {
            double da = firstNumber(a), db = firstNumber(b);
            if (da > 0 && db > 0) {
                double ratio = Math.max(da, db) / Math.min(da, db);
                if (ratio >= 10 && Math.abs(da - db) > 50) {
                    conflicts.add("масштаб: разрыв на порядок или больше (" + (int)da + " vs " + (int)db + ", ×" + (int)ratio + ")");
                }
            }
        } catch (Exception ignored) {}
    }

    private static List<String> extractNumbers(String text, Pattern p) {
        List<String> list = new ArrayList<>();
        Matcher m = p.matcher(text.toLowerCase(Locale.ROOT));
        while (m.find()) list.add(m.group(1).replaceAll("\\s", "") + " " + m.group(2));
        return list;
    }

    private static double firstNumber(String text) {
        Matcher m = Pattern.compile("(\\d+[\\.,]?\\d*)").matcher(text);
        if (m.find()) return Double.parseDouble(m.group(1).replace(',', '.'));
        return 0;
    }

    private static void detectPolarity(String a, String b, List<String> conflicts) {
        int affA = polarityAffirm(a), negA = polarityNegate(a);
        int affB = polarityAffirm(b), negB = polarityNegate(b);
        if ((affA > negA && negB > affB) || (negA > affA && affB > negB)) {
            conflicts.add("полярность: противоположные рекомендации (да/нет, рекомендовать/не рекомендовать)");
        }
    }

    /** Affirmative markers after negated phrases are removed, so «не рекомендую» is not a yes. */
    private static int polarityAffirm(String text) {
        String stripped = text == null ? "" : text.toLowerCase(Locale.ROOT);
        String[] negated = {
                "do not recommend", "don't recommend", "should not", "do not",
                "не рекомендую", "не стоит", "не варто", "не согласен", "не згоден",
                "не можна", "нельзя", "не раджу"
        };
        for (String phrase : negated) stripped = stripped.replace(phrase, " ");
        return countTokens(stripped,
                "да", "так", "рекомендую", "стоит", "варто", "нужно", "потрібно",
                "лучше", "краще", "согласен", "згоден", "раджу",
                "yes", "recommend", "should", "better");
    }

    private static int polarityNegate(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        int n = countTokens(lower, "нет", "ні", "нельзя", "no", "avoid", "уникай", "уникати");
        String[] phrases = {
                "не рекомендую", "не стоит", "не варто", "не согласен", "не згоден",
                "не можна", "не раджу", "do not recommend", "should not"
        };
        for (String phrase : phrases) if (lower.contains(phrase)) n++;
        return n;
    }

    private static int countTokens(String text, String... words) {
        if (text == null || text.isEmpty()) return 0;
        String[] parts = text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+");
        Set<String> have = new HashSet<String>();
        for (String part : parts) if (!part.isEmpty()) have.add(part);
        int found = 0;
        for (String word : words) if (have.contains(word)) found++;
        return found;
    }

    /** Marker kept so older CI patches treat this file as already upgraded. */
    private static void directionalConflicts(String la, String lb, List<String> conflicts) {
        boolean growA = la.contains("рост") || la.contains("увеличить") || la.contains("рекомендую запуск") || la.contains("growth") || la.contains("increase") || la.contains("зростан") || la.contains("збільш");
        boolean fallA = la.contains("падение") || la.contains("уменьшить") || la.contains("избеж") || la.contains("decline") || la.contains("decrease") || la.contains("avoid") || la.contains("падін") || la.contains("зменш") || la.contains("уник");
        boolean growB = lb.contains("рост") || lb.contains("увеличить") || lb.contains("рекомендую запуск") || lb.contains("growth") || lb.contains("increase") || lb.contains("зростан") || lb.contains("збільш");
        boolean fallB = lb.contains("падение") || lb.contains("уменьшить") || lb.contains("избеж") || lb.contains("decline") || lb.contains("decrease") || lb.contains("avoid") || lb.contains("падін") || lb.contains("зменш") || lb.contains("уник");
        if ((growA && fallB) || (fallA && growB)) {
            conflicts.add("направление: противоположные векторы (рост/падение, рекомендовать/избегать)");
        }
    }

    private static void detectScope(String a, String b, List<String> conflicts) {
        boolean caseA = isCaseOnly(a), baseA = isBaseOnly(a), caseB = isCaseOnly(b), baseB = isBaseOnly(b);
        if ((caseA && baseB) || (baseA && caseB)) {
            conflicts.add("масштаб выборки: конкретный случай против base rate");
        }
    }

    private static boolean isCaseOnly(String text) {
        return hasAny(text, "в этом случае", "этот проект", "конкретно здесь", "this specific case") && !isBaseRate(text);
    }

    private static boolean isBaseOnly(String text) {
        return isBaseRate(text) && !hasAny(text, "в этом случае", "этот проект", "конкретно здесь", "this specific case");
    }

    private static boolean isBaseRate(String text) {
        return hasAny(text, "base rate", "базов", "reference class", "reference-class");
    }

    private static void detectOptionChoice(String a, String b, List<String> conflicts) {
        String left = recommendationTarget(a), right = recommendationTarget(b);
        if (!left.isEmpty() && !right.isEmpty() && !left.equals(right)) {
            conflicts.add("выбор: разные объекты рекомендации (" + left + " vs " + right + ")");
        }
    }

    private static String recommendationTarget(String text) {
        Matcher matcher = Pattern.compile("(?iu)(?:рекомендую|выбираю|стоит выбрать|лучше выбрать|i recommend|choose)\\s+([\\p{L}\\d][\\p{L}\\d+.#_-]{1,32})").matcher(text == null ? "" : text);
        if (!matcher.find()) return "";
        String token = matcher.group(1).toLowerCase(Locale.ROOT);
        if (token.matches("внедрять|внедрение|запуск|начать|подождать|сейчас|это|the|to|a|an|it")) return "";
        return token;
    }

    private static final String[][] STAKEHOLDERS = {
            {"пользовател", "user", "користувач"},
            {"клиент", "заказчик", "customer", "клієнт"},
            {"пациент", "patient", "пацієнт"},
            {"сотрудник", "персонал", "employee", "співробітник"},
            {"регулятор", "regulator", "регулятор"},
            {"инвестор", "investor", "інвестор"},
            {"поставщик", "supplier", "постачальник"},
            {"партнёр", "партнер", "partner"},
            {"конкурент", "competitor", "конкурент"},
            {"обществ", "society"}
    };

    private static void detectMissingStakeholders(String task, String a, String b, List<String> missing) {
        if (task == null || task.isEmpty()) return;
        String both = a + "\n" + b;
        for (String[] group : STAKEHOLDERS) {
            if (firstHit(task, group) != null && firstHit(both, group) == null) {
                missing.add("стейкхолдер не рассмотрен: " + group[0]);
            }
        }
    }

    private static String firstHit(String text, String[] needles) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        for (String needle : needles) {
            if (needle != null && lower.contains(needle)) return needle;
        }
        return null;
    }

    private static boolean hasAny(String text, String... needles) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        for (String needle : needles) {
            if (needle != null && lower.contains(needle)) return true;
        }
        return false;
    }

    private static int indexOfAny(String text, String... needles) {
        int best = -1;
        if (text == null) return best;
        for (String needle : needles) {
            int at = text.indexOf(needle);
            if (at >= 0 && (best < 0 || at < best)) best = at;
        }
        return best;
    }

    private static List<String> qualityGaps(String a, String b) {
        List<String> gaps = new ArrayList<>();
        noteQuality(gaps, "ИИ №1", a);
        noteQuality(gaps, "ИИ №2", b);
        return gaps;
    }

    private static void noteQuality(List<String> gaps, String who, String text) {
        if (text.length() < 40 || isFailure(text)) return;
        String lower = text.toLowerCase(Locale.ROOT);
        boolean range = lower.matches("(?s).*\\d+\\s*[-–]\\s*\\d+\\s*%.*") || lower.contains("диапазон");
        boolean next = lower.contains("следующ") || lower.contains("проверить") || lower.contains("next step");
        boolean decision = lower.contains("рекоменд") || lower.contains("стоит ") || lower.contains("выбира");
        boolean premortem = lower.contains("pre-mortem") || lower.contains("премортем") || lower.contains("если провал");
        boolean steel = lower.contains("steelman") || lower.contains("оппонент") || lower.contains("контраргумент") || lower.contains("сильный против");
        boolean residual = lower.contains("residual") || lower.contains("остаточн");
        if (!range) gaps.add(who + ": нет диапазона уверенности");
        if (!next) gaps.add(who + ": нет следующего проверяемого шага");
        if (decision && !premortem) gaps.add(who + ": решение без pre-mortem");
        if (decision && !steel) gaps.add(who + ": решение без steelman");
        if (decision && !residual) gaps.add(who + ": решение без residual risk");
        int conf = extractConfidence(text);
        if (conf >= 80 && evidenceDensity(text) < 40) gaps.add(who + ": уверенность не откалибрована по опоре");
    }

    private static void detectConfidence(String a, String b, List<String> conflicts) {
        int ca = extractConfidence(a), cb = extractConfidence(b);
        if (ca > 0 && cb > 0 && Math.abs(ca - cb) >= 30) {
            conflicts.add("уверенность: расхождение заявленных диапазонов (" + ca + "% vs " + cb + "%)");
        }
    }

    private static int extractConfidence(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        int idx = indexOfAny(lower, "уверен", "confidence", "вероятност");
        if (idx < 0) return 0;
        String window = lower.substring(idx, Math.min(lower.length(), idx + 48));
        Matcher m = Pattern.compile("(\\d+)\\s*[-–]\\s*(\\d+)\\s*%|(\\d+)\\s*%").matcher(window);
        if (m.find()) {
            if (m.group(1) != null) return (Integer.parseInt(m.group(1)) + Integer.parseInt(m.group(2))) / 2;
            return Integer.parseInt(m.group(3));
        }
        return 0;
    }

    private static void detectResidualRisk(String a, String b, List<String> conflicts) {
        int ra = extractResidual(a), rb = extractResidual(b);
        if (ra > 0 && rb > 0 && Math.abs(ra - rb) >= 25) {
            conflicts.add("residual risk: середины диапазонов различаются на ≥25% (" + ra + " vs " + rb + ")");
        }
    }

    private static int extractResidual(String text) {
        Matcher m = Pattern.compile("residual[^0-9]*(\\d+)\\s*[-–]\\s*(\\d+)|остаточн[^0-9]*(\\d+)\\s*[-–]\\s*(\\d+)").matcher(text.toLowerCase(Locale.ROOT));
        if (m.find()) {
            if (m.group(1) != null) return (Integer.parseInt(m.group(1)) + Integer.parseInt(m.group(2))) / 2;
            if (m.group(3) != null) return (Integer.parseInt(m.group(3)) + Integer.parseInt(m.group(4))) / 2;
        }
        return 0;
    }

    private static void detectCausation(String a, String b, List<String> conflicts) {
        String la = a.toLowerCase(Locale.ROOT), lb = b.toLowerCase(Locale.ROOT);
        boolean causeA = la.contains("вызывает") || la.contains("потому что") || la.contains("причин");
        boolean corrA = la.contains("корреляц") || la.contains("связан");
        boolean causeB = lb.contains("вызывает") || lb.contains("потому что") || lb.contains("причин");
        boolean corrB = lb.contains("корреляц") || lb.contains("связан");
        if ((causeA && corrB) || (corrA && causeB)) {
            conflicts.add("причина: причинность против корреляции");
        }
    }

    private static void detectHorizon(String a, String b, List<String> conflicts) {
        String la = a.toLowerCase(Locale.ROOT), lb = b.toLowerCase(Locale.ROOT);
        boolean shortA = la.contains("краткосроч") || la.contains("ближайш") || la.contains("месяц");
        boolean longA = la.contains("долгосроч") || la.contains("горизонт") || la.contains("лет");
        boolean shortB = lb.contains("краткосроч") || lb.contains("ближайш") || lb.contains("месяц");
        boolean longB = lb.contains("долгосроч") || lb.contains("горизонт") || lb.contains("лет");
        if ((shortA && longB) || (longA && shortB)) {
            conflicts.add("горизонт: краткосрок против долгосрока");
        }
    }

    private static void detectYears(String a, String b, List<String> conflicts) {
        Pattern year = Pattern.compile("\\b(19|20)\\d{2}\\b");
        Set<String> ya = extractYears(a, year), yb = extractYears(b, year);
        if (ya.size() == 1 && yb.size() == 1 && !ya.equals(yb)) {
            conflicts.add("даты: разные годы рядом с одним предметом (" + ya.iterator().next() + " vs " + yb.iterator().next() + ")");
        }
    }

    private static Set<String> extractYears(String text, Pattern p) {
        Set<String> set = new HashSet<>();
        Matcher m = p.matcher(text);
        while (m.find()) set.add(m.group());
        return set;
    }

    private static void detectReversibility(String a, String b, String task, List<String> conflicts) {
        String combined = (a + " " + b + " " + (task == null ? "" : task)).toLowerCase(Locale.ROOT);
        boolean decision = combined.contains("внедрять") || combined.contains("рекомендую") || combined.contains("стоит ли");
        boolean mentionsRollback = combined.contains("обратим") || combined.contains("откат") || combined.contains("отменить");
        if (decision && !mentionsRollback) {
            conflicts.add("обратимость: решение без оценки отката (one-way / two-way)");
        }
    }

    private static final class Measure {
        final double raw;
        final String label;
        final String family;
        final double canonical;
        Measure(double raw, String label, String family, double canonical) {
            this.raw = raw;
            this.label = label;
            this.family = family;
            this.canonical = canonical;
        }
    }

    private static void detectUnitClash(String a, String b, List<String> conflicts) {
        Measure left = firstMeasure(a), right = firstMeasure(b);
        if (left == null || right == null || !left.family.equals(right.family) || left.label.equals(right.label)) return;
        if ("money".equals(left.family)) {
            conflicts.add("валюта: одно сравнение в разной валюте (" + trimNum(left.raw) + " " + left.label
                    + " vs " + trimNum(right.raw) + " " + right.label + ")");
            return;
        }
        double denom = Math.max(Math.abs(left.canonical), Math.abs(right.canonical));
        double rel = denom == 0 ? 0 : Math.abs(left.canonical - right.canonical) / denom;
        double rawDenom = Math.max(Math.abs(left.raw), Math.abs(right.raw));
        double rawRel = rawDenom == 0 ? 0 : Math.abs(left.raw - right.raw) / rawDenom;
        if (rel <= 0.12 && rawRel > 0.05) return;
        conflicts.add("единицы: одинаковое число в разных единицах (" + trimNum(left.raw) + " " + left.label
                + " vs " + trimNum(right.raw) + " " + right.label + ")");
    }

    private static String trimNum(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.001) return String.valueOf((long) Math.rint(value));
        return String.valueOf(Math.round(value * 100.0) / 100.0);
    }

    private static Measure firstMeasure(String text) {
        if (text == null || text.isEmpty()) return null;
        Matcher matcher = Pattern.compile(
                "(\\d+(?:[\\.,]\\d+)?)\\s*(километр\\p{L}*|км|km|miles?|миль|мили|мил[яи]\\p{L}*|кг|kg|фунт\\p{L}*|lbs?|°\\s*[cf]|celsius|fahrenheit|usd|долл\\p{L}*|руб\\p{L}*|грн|uah|евро|eur|\\$|€|часов|часа|час|hours?|дней|дня|день|days?|минут\\p{L}*|mins?)")
                .matcher(text.toLowerCase(Locale.ROOT));
        if (!matcher.find()) return null;
        double raw = Double.parseDouble(matcher.group(1).replace(',', '.'));
        String unit = matcher.group(2).replace(" ", "");
        if (unit.startsWith("км") || unit.startsWith("km") || unit.startsWith("километр")) return new Measure(raw, "км", "length", raw);
        if (unit.startsWith("мил") || unit.startsWith("mile")) return new Measure(raw, "миль", "length", raw * 1.60934);
        if (unit.equals("кг") || unit.equals("kg")) return new Measure(raw, "кг", "mass", raw);
        if (unit.startsWith("фунт") || unit.equals("lb") || unit.equals("lbs")) return new Measure(raw, "фунт", "mass", raw * 0.453592);
        if (unit.startsWith("°c") || unit.equals("celsius")) return new Measure(raw, "°C", "temp", raw);
        if (unit.startsWith("°f") || unit.equals("fahrenheit")) return new Measure(raw, "°F", "temp", (raw - 32.0) * 5.0 / 9.0);
        if (unit.startsWith("руб")) return new Measure(raw, "руб", "money", raw);
        if (unit.equals("usd") || unit.startsWith("долл") || unit.equals("$")) return new Measure(raw, "usd", "money", raw);
        if (unit.equals("грн") || unit.equals("uah")) return new Measure(raw, "грн", "money", raw);
        if (unit.equals("евро") || unit.equals("eur") || unit.equals("€")) return new Measure(raw, "eur", "money", raw);
        if (unit.startsWith("час") || unit.startsWith("hour")) return new Measure(raw, "ч", "time", raw);
        if (unit.startsWith("д") || unit.startsWith("day")) return new Measure(raw, "дн", "time", raw * 24.0);
        if (unit.startsWith("минут") || unit.startsWith("min")) return new Measure(raw, "мин", "time", raw / 60.0);
        return null;
    }

    private static final String[][] PREMISE_PAIRS = {
            {"неэластич", "эластич"},
            {"нееластич", "еластич"},
            {"inelastic", "elastic"},
            {"необратим", "обратим"},
            {"дефицит", "профицит"},
            {"монопол", "конкурен"}
    };

    private static void detectPremiseFork(String a, String b, List<String> conflicts) {
        for (String[] pair : PREMISE_PAIRS) {
            int left = premisePole(a, pair[0], pair[1]);
            int right = premisePole(b, pair[0], pair[1]);
            if (left * right < 0) {
                conflicts.add("предпосылки: противоположные допущения (" + pair[1] + " / " + pair[0] + ")");
                return;
            }
        }
    }

    private static int premisePole(String text, String negative, String positive) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        if (lower.contains(negative)) return -1;
        if (lower.contains(positive)) return 1;
        return 0;
    }

    private static void detectExclusivity(String a, String b, List<String> conflicts) {
        int left = exclusivityPole(a);
        int right = exclusivityPole(b);
        if (left * right >= 0) return;
        Set<String> shared = significantWords(a);
        shared.retainAll(significantWords(b));
        if (shared.isEmpty()) return;
        conflicts.add("исключительность: запрет против допуска при общем предмете (" + shared.iterator().next() + ")");
    }

    private static int exclusivityPole(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        boolean forbid = lower.contains("запрещ") || lower.contains("недопуст") || lower.contains("нельзя")
                || lower.contains("must not") || lower.contains("forbid") || lower.contains("не обязатель");
        boolean allow = lower.contains("допуска") || lower.contains("разреш") || lower.contains("можно")
                || lower.contains("allow") || lower.contains("permitted")
                || (lower.contains("обязательно") && !lower.contains("не обязатель"));
        if (forbid && allow) return 0;
        if (forbid) return -1;
        if (allow) return 1;
        return 0;
    }

    private static void detectCalibration(String a, String b, int evidenceA, int evidenceB, List<String> conflicts) {
        int ca = extractConfidence(a);
        int cb = extractConfidence(b);
        if (ca >= 80 && evidenceA < 40) {
            conflicts.add("калибровка: высокая уверенность при слабой опоре у ИИ №1 (" + ca + "% / evidence " + evidenceA + ")");
        }
        if (cb >= 80 && evidenceB < 40) {
            conflicts.add("калибровка: высокая уверенность при слабой опоре у ИИ №2 (" + cb + "% / evidence " + evidenceB + ")");
        }
        if (ca > 0 && cb > 0 && Math.abs(ca - cb) >= 30 && Math.abs(evidenceA - evidenceB) <= 15) {
            conflicts.add("калибровка: разрыв уверенности при сходной опоре (" + ca + "% vs " + cb + "%)");
        }
    }

    private static void detectMissingAlternatives(String task, String a, String b, List<String> missing) {
        if (task == null) return;
        String lower = task.toLowerCase(Locale.ROOT);
        boolean asks = lower.contains("сравн") || lower.contains("вариант") || lower.contains("альтернатив")
                || lower.contains("pros and cons") || lower.contains("плюсы и минусы") || lower.contains("что лучше");
        if (!asks || a.length() < 40 || b.length() < 40) return;
        if (!coversAlternatives(a) && !coversAlternatives(b)) {
            missing.add("альтернативы не развёрнуты, хотя задача просит сравнение");
        }
    }

    private static boolean coversAlternatives(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        return lower.contains("альтернатив") || lower.contains("вариант") || lower.contains("другой подход")
                || lower.contains("с одной стороны") || lower.contains("вместо") || lower.contains(" versus ")
                || lower.contains(" vs ");
    }

    /** One expert gates the action ("only if"); the other states it as categorical. */
    private static void detectConditionalFork(String a, String b, List<String> conflicts) {
        if (!isRecommendation(a) || !isRecommendation(b)) return;
        if (isGated(a) == isGated(b)) return;
        conflicts.add("условие: категоричная рекомендация против условной (только если / при условии)");
    }

    private static boolean isRecommendation(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        return lower.contains("рекоменд") || lower.contains("стоит выбрать") || lower.contains("i recommend")
                || lower.contains("варто") || lower.contains("раджу");
    }

    private static boolean isGated(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        return lower.contains("только если") || lower.contains("лишь если") || lower.contains("only if")
                || lower.contains("при условии") || lower.contains("за умови")
                || lower.contains("если и только если") || lower.contains("не раньше чем")
                || lower.contains("пока не ") || lower.contains("until ");
    }

    /** Universal claim versus a partial claim about a shared subject. */
    private static void detectQuantifierClash(String a, String b, List<String> conflicts) {
        int left = quantifierPole(a);
        int right = quantifierPole(b);
        if (left * right >= 0) return;
        Set<String> shared = significantWords(a);
        shared.retainAll(significantWords(b));
        if (shared.isEmpty()) return;
        conflicts.add("квантор: все против части («" + shared.iterator().next() + "»)");
    }

    private static int quantifierPole(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        String stripped = lower.replace("не всем", " ").replace("не всех", " ").replace("не все", " ")
                .replace("не каждый", " ").replace("not all", " ").replace("не усі", " ");
        boolean universal = stripped.contains("все клиент") || stripped.contains("всех клиент")
                || stripped.contains("всем клиент") || stripped.contains("каждый клиент")
                || stripped.contains("все пользовател") || stripped.contains("каждый пользовател")
                || stripped.contains("во всех случаях") || stripped.contains("all users")
                || stripped.contains("every user") || stripped.contains("all customers")
                || stripped.contains("усі клієнт") || stripped.contains("кожен клієнт");
        boolean partial = lower.contains("некотор") || lower.contains("не все") || lower.contains("не всех")
                || lower.contains("не всем") || lower.contains("часть клиент") || lower.contains("часть пользовател")
                || lower.contains("some users") || lower.contains("some customers")
                || lower.contains("a subset") || lower.contains("не каждый") || lower.contains("деякі");
        if (universal && partial) return 0;
        if (universal) return 1;
        if (partial) return -1;
        return 0;
    }

    /** A percent change is not a percentage-point change. */
    private static void detectMetricKind(String a, String b, List<String> conflicts) {
        boolean pointsA = percentagePoints(a), pointsB = percentagePoints(b);
        boolean percentA = plainPercent(a), percentB = plainPercent(b);
        if ((pointsA && percentB) || (percentA && pointsB)) {
            conflicts.add("метрика: проценты против процентных пунктов");
        }
    }

    private static boolean percentagePoints(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        return lower.contains("п.п") || lower.contains("п. п") || lower.contains("процентных пункт")
                || lower.contains("процентних пункт") || lower.contains("percentage point");
    }

    private static boolean plainPercent(String text) {
        return text != null && text.contains("%") && !percentagePoints(text);
    }

    private static void rankConflicts(List<String> conflicts) {
        Collections.sort(conflicts, new java.util.Comparator<String>() {
            public int compare(String left, String right) {
                return cruxRank(left) - cruxRank(right);
            }
        });
    }

    private static int cruxRank(String conflict) {
        if (conflict.startsWith("полярность") || conflict.startsWith("исключительность")) return 0;
        if (conflict.startsWith("выбор") || conflict.startsWith("условие")) return 1;
        if (conflict.startsWith("направление") || conflict.startsWith("квантор")) return 2;
        if (conflict.startsWith("валюта") || conflict.startsWith("единицы")) return 3;
        if (conflict.startsWith("предпосылки") || conflict.startsWith("метрика")) return 4;
        if (conflict.startsWith("причина")) return 5;
        if (conflict.startsWith("масштаб выборки")) return 6;
        if (conflict.startsWith("калибровка")) return 7;
        if (conflict.startsWith("уверенность")) return 8;
        if (conflict.startsWith("residual")) return 9;
        if (conflict.startsWith("даты")) return 9;
        if (conflict.startsWith("горизонт")) return 10;
        if (conflict.startsWith("масштаб")) return 11;
        if (conflict.startsWith("число")) return 12;
        if (conflict.startsWith("доказательства")) return 13;
        return 14;
    }
}
