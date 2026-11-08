package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Local assumption-ledger signals.
 * A recommendation that names neither a load-bearing assumption nor an
 * observation that would reverse it, and a pair of answers that rest on
 * opposite assumptions about the same thing, are hypotheses — not verdicts.
 * Distinct from crux (opposed advice without a switch), underdetermination
 * (one mechanism declared the only story) and motte-and-bailey (strength retreat).
 * The coordinator must not average incompatible assumptions into a middle.
 */
public final class AssumptionLedgerPolicy {
    private static final int MAX_SIGNALS = 2;

    private AssumptionLedgerPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, !acknowledgesClash(a) && !acknowledgesClash(b) && oppositeAssumptions(a, b),
                "несовместимые несущие допущения: ответы стоят на противоположных условиях одного и того же");
        add(conflicts, initial, bareRecommendation(a) || bareRecommendation(b),
                "рекомендация без названного допущения и без наблюдения, которое её отменит");
        add(conflicts, initial, bareRecommendation(a) && bareRecommendation(b),
                "оба эксперта закрывают выбор, не назвав допущение и не назвав, что его отменит");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "журнал допущений (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean oppositeAssumptions(String a, String b) {
        List<String> left = clauses(a);
        List<String> right = clauses(b);
        for (int i = 0; i < left.size(); i++) {
            int poleA = pole(left.get(i));
            if (poleA == 0) continue;
            for (int j = 0; j < right.size(); j++) {
                int poleB = pole(right.get(j));
                if (poleB == 0 || poleB == poleA) continue;
                if (sharesContent(left.get(i), right.get(j))) return true;
            }
        }
        return false;
    }

    private static List<String> clauses(String text) {
        List<String> out = new ArrayList<String>();
        if (text == null || text.isEmpty()) return out;
        String[] parts = text.split("[\\.!\\?;\\n]");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.length() < 12) continue;
            if (isAssumption(part)) out.add(part);
        }
        return out;
    }

    private static boolean isAssumption(String t) {
        return has(t,
                "допущен", "допуска",
                "assuming", "we assume", "i assume", "if we assume",
                "при условии", "исходя из того");
    }

    private static int pole(String clause) {
        boolean up = has(clause,
                "выраст", "увеличи", "выше", "больше",
                "grow", "increase", "rising", "higher", "go up", "will rise")
                || hasToken(clause, "рост");
        boolean down = has(clause,
                "упад", "сниз", "уменьш", "ниже", "меньше", "сократ",
                "shrink", "decrease", "falling", "lower", "go down", "will fall", "will drop");
        if (up == down) {
            boolean yes = has(clause, "спрос есть", "будет спрос", "demand exists", "there is demand");
            boolean no = has(clause, "спроса нет", "нет спроса", "не будет спрос", "no demand", "there is no demand");
            if (yes == no) return 0;
            return yes ? 1 : -1;
        }
        if (up && down) return 0;
        return up ? 1 : -1;
    }

    private static boolean hasToken(String text, String token) {
        List<String> words = tokens(text);
        for (int i = 0; i < words.size(); i++) {
            if (token.equals(words.get(i))) return true;
        }
        return false;
    }

    private static boolean sharesContent(String left, String right) {
        List<String> a = tokens(left);
        List<String> b = tokens(right);
        for (int i = 0; i < a.size(); i++) {
            String word = a.get(i);
            if (word.length() < 5 || stop(word)) continue;
            for (int j = 0; j < b.size(); j++) {
                if (word.equals(b.get(j))) return true;
            }
        }
        return false;
    }

    private static List<String> tokens(String text) {
        List<String> out = new ArrayList<String>();
        StringBuilder buf = new StringBuilder();
        String s = text == null ? "" : text;
        for (int i = 0; i <= s.length(); i++) {
            char c = i == s.length() ? ' ' : s.charAt(i);
            if (Character.isLetter(c)) {
                buf.append(c);
            } else if (buf.length() > 0) {
                out.add(buf.toString());
                buf.setLength(0);
            }
        }
        return out;
    }

    private static boolean stop(String w) {
        return w.equals("допущение") || w.equals("допущения") || w.equals("допускаем")
                || w.equals("допускаю") || w.equals("assuming") || w.equals("assume")
                || w.equals("условии") || w.equals("условия") || w.equals("исходя")
                || w.equals("поэтому") || w.equals("потому") || w.equals("который")
                || w.equals("которая") || w.equals("следующем") || w.equals("квартале")
                || w.equals("будет") || w.equals("этого") || w.equals("этой");
    }

    private static boolean bareRecommendation(String t) {
        if (t == null || t.length() < 80) return false;
        if (acknowledgesLedger(t) || isAssumption(t) || hasFalsifier(t)) return false;
        return hasRecommendation(t);
    }

    private static boolean hasRecommendation(String t) {
        return has(t,
                "рекомендую", "стоит делать", "не стоит", "не рекомендую",
                "i recommend", "should proceed", "should not proceed", "do not proceed");
    }

    private static boolean hasFalsifier(String t) {
        return has(t,
                "изменит вывод", "изменило бы вывод", "отменит вывод", "отменит рекомендац",
                "если окажется", "что переверн", "перевернёт вывод", "перевернет вывод",
                "would change my", "would flip", "what would change", "observation that would");
    }

    private static boolean acknowledgesLedger(String t) {
        return has(t, "журнал допущений", "assumption ledger", "несущие допущения перечислены");
    }

    private static boolean acknowledgesClash(String t) {
        return has(t,
                "несовместимые допущения", "допущения расходятся", "assumptions conflict",
                "assumptions are incompatible", "противоположные допущения");
    }

    private static boolean has(String text, String... phrases) {
        if (text == null) return false;
        for (int i = 0; i < phrases.length; i++) {
            if (text.contains(phrases[i])) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
