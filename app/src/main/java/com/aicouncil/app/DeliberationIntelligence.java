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
 * polarity and directional conflicts, confidence mismatch, causal vs correlation,
 * time-horizon and scope splits, year clashes, premise forks, reversibility gaps,
 * hedge/absolute calibration, and local arithmetic checks.
 * No network and no Android types.
 */
public final class DeliberationIntelligence {
    private static final int MAX_SUBQUESTIONS = 6;
    private static final int MAX_CLAIMS = 12;
    private static final int MAX_CONFLICTS = 8;
    private static final int MAX_FAULTS = 6;
    private static final Pattern ITEM = Pattern.compile("(?i)^(?:\\d+[\\).]|[-*•])\\s+(.+)$");
    private static final Pattern PERCENT = Pattern.compile(
            "(?i)(\\d+(?:[.,]\\d+)?)\\s*%\\s*(?:от|of)\\s*(\\d+(?:[.,]\\d+)?)\\s*=\\s*(\\d+(?:[.,]\\d+)?)");
    private static final Pattern BINARY = Pattern.compile(
            "(\\d+(?:[.,]\\d+)?)\\s*([+\\-*/×÷])\\s*(\\d+(?:[.,]\\d+)?)\\s*=\\s*(\\d+(?:[.,]\\d+)?)");
    private static final Pattern NUMBER = Pattern.compile(
            "(?i)(\\d+(?:[.,]\\d+)?)(?:\\s*(%|₽|\\$|руб(?:лей|ля|\\.)?|usd|eur|кг|килограмм\\w*|км|километр\\w*|шт\\.?))?");

    private static final Set<String> STOP = new HashSet<String>();
    private static final String[] AFFIRM = {
            "да", "верно", "правильно", "согласен", "рекомендую", "следует", "нужно", "лучше",
            "yes", "correct", "agree", "recommend", "should", "must", "prefer", "лучше выбрать"
    };
    private static final String[] DENY = {
            "нет", "неверно", "неправильно", "не согласен", "не рекомендую", "не следует", "нельзя",
            "no", "incorrect", "disagree", "not recommend", "should not", "must not", "avoid", "не стоит"
    };
    private static final String[] HEDGE = {
            "возможно", "вероятно", "скорее", "примерно", "около", "допущ", "неопредел", "может быть",
            "perhaps", "maybe", "likely", "approximately", "uncertain", "estimate", "range", "диапазон",
            "кажется", "похоже", "условно", "hypothetically"
    };
    private static final String[] ABSOLUTE = {
            "точно", "всегда", "никогда", "гарантированно", "бесспорно", "однозначно", "100%",
            "always", "never", "guaranteed", "certainly", "definitely", "undeniable", "абсолютно"
    };
    /** Directional antonym pairs (RU/EN). A hit counts only when the two experts take opposite sides. */
    private static final String[][] DIRECTIONAL = {
            {"рост", "падение"}, {"увеличи", "уменьш"}, {"повыс", "пониж"}, {"рост", "снижен"},
            {"рекоменд", "избег"}, {"принят", "отклон"}, {"запуск", "отлож"},
            {"высокий риск", "низкий риск"}, {"increase", "decrease"}, {"grow", "shrink"},
            {"recommend", "avoid"}, {"approve", "reject"}, {"bullish", "bearish"},
            {"optimistic", "pessimistic"}, {"upside", "downside"}
    };
    private static final Pattern CONF_RANGE = Pattern.compile(
            "(?iu)(?:уверенн(?:ость|ости)?|confidence|вероятност[ьи])[^0-9%]{0,28}(\\d{1,3})\\s*[-–—]\\s*(\\d{1,3})\\s*%?");
    private static final Pattern CONF_SINGLE = Pattern.compile(
            "(?iu)(?:уверенн(?:ость|ости)?|confidence)[^0-9%]{0,24}(\\d{1,3})\\s*%");
    private static final String[] CAUSE = {
            "вызывает", "приводит к", "является причиной", "causes", "leads to", "results in", "из-за этого"
    };
    private static final String[] CAUSE_DENY = {
            "не вызывает", "не является причиной", "не причин", "корреляц", "correlation",
            "associated with", "связан с", "ассоциац", "does not cause"
    };
    private static final String[] SHORT_H = {
            "краткосроч", "в ближайш", "short-term", "short term", "near-term", "near term",
            "в этом квартал", "на этой неделе"
    };
    private static final String[] LONG_H = {
            "долгосроч", "в перспективе", "long-term", "long term", "через несколько лет", "на горизонте лет"
    };
    private static final String[] INDIV = {
            "в этом случае", "конкретно этот", "данный пациент", "this specific", "этот клиент", "именно этот"
    };
    private static final String[] POP = {
            "в среднем", "base rate", "по популяции", "статистически", "reference class", "типичных случаях"
    };
    private static final String[] REVERSE = {
            "обратим", "необратим", "откат", "можно отменить", "reversib", "one-way", "two-way",
            "rollback", "отменить решение"
    };
    private static final String[] DECISION_TASK = {
            "стоит ли", "выбрать", "внедр", "запускать", "approve", "launch", "купить", "инвестир", "отказаться от"
    };
    private static final Pattern YEAR = Pattern.compile("(?<!\\d)((?:19|20)\\d{2})(?!\\d)");
    private static final Pattern PREMISE = Pattern.compile(
            "(?iu)(потому что|потому, что|because|assuming|если допустить|при условии|исходя из)([^.]{8,180})");

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
        public final int agreementScore;
        /** 0–100: higher means more hedging language relative to absolutes (avg of both answers). */
        public final int hedgeRatio;

        Brief(List<String> subquestions, List<String> conflicts, List<ArithmeticFault> arithmetic,
              List<String> missing, int scoreA, int scoreB, int agreementScore, int hedgeRatio) {
            this.subquestions = subquestions;
            this.conflicts = conflicts;
            this.arithmetic = arithmetic;
            this.missing = missing;
            this.scoreA = scoreA;
            this.scoreB = scoreB;
            this.agreementScore = agreementScore;
            this.hedgeRatio = hedgeRatio;
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
        polarityConflicts(a, b, conflicts);
        String la = a.toLowerCase(Locale.ROOT);
        String lb = b.toLowerCase(Locale.ROOT);
        directionalConflicts(la, lb, conflicts);
        confidenceMismatch(a, b, conflicts);
        causalMismatch(la, lb, conflicts);
        horizonSplit(la, lb, conflicts);
        scopeSplit(la, lb, conflicts);
        yearConflicts(a, b, conflicts);
        premiseFork(a, b, conflicts);
        reversibilityGap(task, a, b, conflicts);
        List<String> missing = missingCoverage(questions, a, b);
        int polarityHits = 0;
        for (String c : conflicts) if (c.startsWith("полярность")) polarityHits++;
        int agreement = computeAgreementScore(a, b, conflicts.size(), polarityHits);
        int hedge = (hedgeScore(a) + hedgeScore(b)) / 2;
        return new Brief(questions, conflicts, faults, missing, substance(a), substance(b), agreement, hedge);
    }

    public static String formatForCoordinator(Brief brief) {
        if (brief == null) return "";
        if (brief.conflicts.isEmpty() && brief.arithmetic.isEmpty() && brief.missing.isEmpty()
                && brief.agreementScore > 80 && brief.hedgeRatio >= 40 && brief.hedgeRatio <= 75) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        out.append("Плотность ответа: ИИ №1 ").append(brief.scoreA).append("/100, ИИ №2 ").append(brief.scoreB).append("/100. ");
        out.append("Оценка согласованности (локальная): ").append(brief.agreementScore).append("/100. ");
        out.append("Калибровка языка (hedge vs absolute, 0–100, выше = больше оговорок): ").append(brief.hedgeRatio).append(".\n");
        if (!brief.conflicts.isEmpty()) {
            out.append("РАСХОЖДЕНИЯ (числа, полярность, направление, уверенность, причина, горизонт, масштаб, даты; проверь по тексту, не сглаживай):\n");
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
        if (brief.hedgeRatio < 25) {
            out.append("Сигнал: оба ответа звучат слишком категорично — потребуй диапазоны уверенности и residual risk.\n");
        } else if (brief.hedgeRatio > 85) {
            out.append("Сигнал: много оговорок — уточни, где неопределённость эпистемическая, а где алеаторная.\n");
        }
        out.append("Не цитируй эту карту как внешний источник. Если расхождение ложное, так и напиши. "
                + "Метки «горизонт», «причина», «даты», «масштаб», «предпосылки» и «обратимость» не усредняй в один вывод.\n");
        String text = out.toString().trim();
        return text.length() > 3200 ? text.substring(0, 3200).trim() : text;
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
            if (("/ ".trim().equals(op) || "÷".equals(op)) && Math.abs(right) < 1e-12) continue;
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

    /** 0 = very absolute, 100 = heavily hedged. */
    public static int hedgeScore(String answer) {
        if (answer == null || answer.trim().isEmpty()) return 50;
        String lower = answer.toLowerCase(Locale.ROOT);
        int h = countHits(lower, HEDGE);
        int a = countHits(lower, ABSOLUTE);
        if (h + a == 0) return 50;
        return Math.max(0, Math.min(100, (int) (100.0 * h / (h + a + 0.5))));
    }

    private static int countHits(String lower, String[] needles) {
        int n = 0;
        for (String s : needles) if (lower.contains(s)) n++;
        return n;
    }

    private static void polarityConflicts(String a, String b, List<String> conflicts) {
        if (conflicts.size() >= MAX_CONFLICTS) return;
        String la = a.toLowerCase(Locale.ROOT);
        String lb = b.toLowerCase(Locale.ROOT);
        int affA = countHits(la, AFFIRM);
        int denA = countHits(la, DENY);
        int affB = countHits(lb, AFFIRM);
        int denB = countHits(lb, DENY);
        // Strong opposite polarity: one side affirms more than denies, the other does the reverse
        boolean aPositive = affA > denA && affA >= 2;
        boolean aNegative = denA > affA && denA >= 2;
        boolean bPositive = affB > denB && affB >= 2;
        boolean bNegative = denB > affB && denB >= 2;
        if ((aPositive && bNegative) || (aNegative && bPositive)) {
            conflicts.add("полярность: один эксперт склоняется к «да/рекомендую», другой — к «нет/не рекомендую» "
                    + "(аффирмации/отрицания: ИИ1 " + affA + "/" + denA + ", ИИ2 " + affB + "/" + denB + ")");
        }
        // Opening yes/no clash in first 120 chars (common for direct answers)
        String headA = la.length() > 120 ? la.substring(0, 120) : la;
        String headB = lb.length() > 120 ? lb.substring(0, 120) : lb;
        boolean yesA = headA.contains("да,") || headA.startsWith("да ") || headA.startsWith("yes") || headA.contains("рекомендую");
        boolean noA = headA.contains("нет,") || headA.startsWith("нет ") || headA.startsWith("no ") || headA.contains("не рекомендую");
        boolean yesB = headB.contains("да,") || headB.startsWith("да ") || headB.startsWith("yes") || headB.contains("рекомендую");
        boolean noB = headB.contains("нет,") || headB.startsWith("нет ") || headB.startsWith("no ") || headB.contains("не рекомендую");
        if (((yesA && noB) || (noA && yesB)) && conflicts.size() < MAX_CONFLICTS) {
            boolean already = false;
            for (String c : conflicts) if (c.startsWith("полярность")) { already = true; break; }
            if (!already) {
                conflicts.add("полярность: прямые ответы в начале текста расходятся (да/нет или рекомендовать/не рекомендовать)");
            }
        }
    }

    private static void directionalConflicts(String la, String lb, List<String> conflicts) {
        if (conflicts.size() >= MAX_CONFLICTS) return;
        for (String[] pair : DIRECTIONAL) {
            String left = pair[0];
            String right = pair[1];
            boolean aLeft = la.contains(left);
            boolean aRight = la.contains(right);
            boolean bLeft = lb.contains(left);
            boolean bRight = lb.contains(right);
            if ((aLeft && !aRight && bRight && !bLeft) || (aRight && !aLeft && bLeft && !bRight)) {
                conflicts.add("направление: эксперты расходятся по оси «" + left + "» ↔ «" + right + "»");
                if (conflicts.size() >= MAX_CONFLICTS) return;
            }
        }
    }

    private static void confidenceMismatch(String a, String b, List<String> conflicts) {
        if (conflicts.size() >= MAX_CONFLICTS) return;
        int[] ca = extractConfidence(a);
        int[] cb = extractConfidence(b);
        if (ca == null || cb == null) return;
        int midA = (ca[0] + ca[1]) / 2;
        int midB = (cb[0] + cb[1]) / 2;
        if (Math.abs(midA - midB) >= 25) {
            conflicts.add("уверенность: заявленные уровни сильно расходятся (ИИ1 ~"
                    + midA + "%, ИИ2 ~" + midB + "%) — сверь калибровку и base rates");
        }
    }

    private static int[] extractConfidence(String text) {
        if (text == null || text.isEmpty()) return null;
        Matcher m = CONF_RANGE.matcher(text);
        if (m.find()) {
            Integer lo = parseIntSafe(m.group(1));
            Integer hi = parseIntSafe(m.group(2));
            if (lo != null && hi != null && lo <= hi && hi <= 100) return new int[]{lo, hi};
        }
        Matcher s = CONF_SINGLE.matcher(text);
        if (s.find()) {
            Integer v = parseIntSafe(s.group(1));
            if (v != null && v <= 100) return new int[]{Math.max(0, v - 5), Math.min(100, v + 5)};
        }
        return null;
    }

    private static Integer parseIntSafe(String raw) {
        if (raw == null) return null;
        try {
            return Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void causalMismatch(String la, String lb, List<String> conflicts) {
        if (conflicts.size() >= MAX_CONFLICTS) return;
        boolean causeA = hasCause(la);
        boolean denyA = countHits(la, CAUSE_DENY) > 0;
        boolean causeB = hasCause(lb);
        boolean denyB = countHits(lb, CAUSE_DENY) > 0;
        if ((causeA && denyB && !denyA) || (causeB && denyA && !denyB)) {
            conflicts.add("причина: один эксперт утверждает причинную связь, другой говорит лишь о корреляции или отрицает причину — не сглаживай в «влияние»");
        }
    }

    private static boolean hasCause(String lower) {
        String scrub = lower;
        for (String deny : CAUSE_DENY) scrub = scrub.replace(deny, " ");
        return countHits(scrub, CAUSE) > 0;
    }

    private static void horizonSplit(String la, String lb, List<String> conflicts) {
        if (conflicts.size() >= MAX_CONFLICTS) return;
        int shortA = countHits(la, SHORT_H);
        int longA = countHits(la, LONG_H);
        int shortB = countHits(lb, SHORT_H);
        int longB = countHits(lb, LONG_H);
        if ((shortA > 0 && longA == 0 && longB > 0 && shortB == 0)
                || (shortB > 0 && longB == 0 && longA > 0 && shortA == 0)) {
            conflicts.add("горизонт: один эксперт смотрит на краткосрочный эффект, другой — на долгосрочный; раздели итог по горизонтам, не усредняй");
        }
    }

    private static void scopeSplit(String la, String lb, List<String> conflicts) {
        if (conflicts.size() >= MAX_CONFLICTS) return;
        int indA = countHits(la, INDIV);
        int popA = countHits(la, POP);
        int indB = countHits(lb, INDIV);
        int popB = countHits(lb, POP);
        if ((indA > 0 && popA == 0 && popB > 0 && indB == 0)
                || (indB > 0 && popB == 0 && popA > 0 && indA == 0)) {
            conflicts.add("масштаб: один отвечает про конкретный случай, другой — про базовый класс; не усредняй их в одну вероятность");
        }
    }

    private static void yearConflicts(String a, String b, List<String> conflicts) {
        if (conflicts.size() >= MAX_CONFLICTS || a == null || b == null) return;
        List<int[]> yearsA = yearHits(a);
        List<int[]> yearsB = yearHits(b);
        if (yearsA.isEmpty() || yearsB.isEmpty()) return;
        Set<Integer> setA = new HashSet<Integer>();
        Set<Integer> setB = new HashSet<Integer>();
        for (int[] hit : yearsA) setA.add(Integer.valueOf(hit[0]));
        for (int[] hit : yearsB) setB.add(Integer.valueOf(hit[0]));
        if (setA.equals(setB)) return;
        for (int[] left : yearsA) {
            for (int[] right : yearsB) {
                if (Math.abs(left[0] - right[0]) < 2) continue;
                List<String> keysA = keywords(window(a, left[1]));
                List<String> keysB = keywords(window(b, right[1]));
                if (shared(keysA, keysB) < 1) continue;
                conflicts.add("даты: рядом с «" + preview(keysA, keysB) + "» названы разные годы ("
                        + left[0] + " против " + right[0] + ")");
                return;
            }
        }
    }

    private static List<int[]> yearHits(String text) {
        List<int[]> hits = new ArrayList<int[]>();
        Matcher matcher = YEAR.matcher(text);
        while (matcher.find() && hits.size() < 8) {
            Integer year = parseIntSafe(matcher.group(1));
            if (year == null) continue;
            hits.add(new int[]{year.intValue(), matcher.start()});
        }
        return hits;
    }

    private static String window(String text, int at) {
        int from = Math.max(0, at - 48);
        int to = Math.min(text.length(), at + 4);
        return text.substring(from, to);
    }

    private static void premiseFork(String a, String b, List<String> conflicts) {
        if (conflicts.size() >= MAX_CONFLICTS) return;
        boolean opposed = false;
        for (String conflict : conflicts) {
            if (conflict.startsWith("полярность") || conflict.startsWith("направление")) {
                opposed = true;
                break;
            }
        }
        if (!opposed) return;
        List<String> keysA = premiseKeys(a);
        List<String> keysB = premiseKeys(b);
        if (keysA.isEmpty() || keysB.isEmpty()) return;
        int sharedKeys = shared(keysA, keysB);
        if (sharedKeys >= 2) {
            conflicts.add("предпосылки: общий фундамент, но противоположный вывод — приоритетный конфликт, не косметика");
        } else if (sharedKeys == 0) {
            conflicts.add("предпосылки: выводы опираются на разные основания; сначала согласуй допущения, потом рекомендацию");
        }
    }

    private static List<String> premiseKeys(String text) {
        List<String> keys = new ArrayList<String>();
        if (text == null) return keys;
        Matcher matcher = PREMISE.matcher(text);
        while (matcher.find() && keys.size() < 6) {
            for (String key : keywords(matcher.group(2))) {
                if (!keys.contains(key)) keys.add(key);
                if (keys.size() >= 6) break;
            }
        }
        return keys;
    }

    private static void reversibilityGap(String task, String a, String b, List<String> conflicts) {
        if (conflicts.size() >= MAX_CONFLICTS) return;
        String lowerTask = task == null ? "" : task.toLowerCase(Locale.ROOT);
        if (countHits(lowerTask, DECISION_TASK) == 0) return;
        String blob = ((a == null ? "" : a) + "\n" + (b == null ? "" : b)).toLowerCase(Locale.ROOT);
        if (countHits(blob, REVERSE) > 0) return;
        conflicts.add("обратимость: задача похожа на решение, но ни один эксперт не оценил, можно ли его откатить (one-way vs two-way)");
    }

    private static int computeAgreementScore(String a, String b, int conflictCount, int polarityHits) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        int base = 65;
        base -= Math.min(conflictCount * 10, 35);
        base -= Math.min(polarityHits * 15, 25);
        Set<String> wordsA = new HashSet<String>();
        for (String w : a.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) if (w.length() > 3 && !STOP.contains(w)) wordsA.add(w);
        Set<String> wordsB = new HashSet<String>();
        for (String w : b.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) if (w.length() > 3 && !STOP.contains(w)) wordsB.add(w);
        if (wordsA.isEmpty() || wordsB.isEmpty()) return Math.max(0, Math.min(100, base));
        int intersection = 0;
        for (String w : wordsA) if (wordsB.contains(w)) intersection++;
        int union = wordsA.size() + wordsB.size() - intersection;
        double jaccard = union == 0 ? 0.0 : (double) intersection / union;
        base += (int) (jaccard * 35);
        return Math.max(0, Math.min(100, base));
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
                double relDiff = Math.abs(x.value - y.value) / Math.max(Math.abs(x.value), Math.abs(y.value) + 1e-6);
                if (relDiff < 0.05) continue;
                conflicts.add("около «" + preview(x.keywords, y.keywords) + "»: "
                        + format(x.value) + unitSuffix(x.unit) + " против " + format(y.value) + unitSuffix(y.unit)
                        + " (относит. разница ~" + (int)(relDiff * 100) + "%)");
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
