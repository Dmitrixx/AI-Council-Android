package com.aicouncil.app;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local diagnosis of what kind of disagreement two answers actually are.
 * A shared recommendation held up by incompatible mechanisms is not agreement.
 * A factual close and a normative close are not two estimates of one quantity.
 * Naming the split, or already stating the observation that would flip it, is not an error.
 * The result is a hypothesis for the coordinator, not a verdict.
 */
public final class DisagreementDiagnosisPolicy {
    private static final int MAX_SIGNALS = 1;
    private static final Pattern TARGET = Pattern.compile(
            "(?iu)(?:рекомендую|выбираю|стоит выбрать|лучше выбрать|i recommend|choose)\\s+([\\p{L}\\d][\\p{L}\\d+.#_-]{1,32})");
    private static final Pattern BECAUSE = Pattern.compile(
            "(?iu)(?:потому что|because|бо)\\s+([^\\.;\\n]{8,90})");

    private DisagreementDiagnosisPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        detect(null, answerA, answerB, conflicts);
    }

    public static void detect(String task, String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        if (a.length() < 24 || b.length() < 24) return;
        if (acknowledges(a) || acknowledges(b)) return;

        String kind = null;
        if (fragileAgreement(a, b)) {
            kind = "хрупкое согласие: цель совпадает, но механизмы несовместимы и нет наблюдения, которое переключит вывод";
        } else if (factValueFork(a, b)) {
            kind = "факт против нормы: одна сторона закрывает оценку, другая — долженствование; их нельзя усреднить в одну уверенность";
        }
        if (kind == null) return;
        add(conflicts, kind);
    }

    private static void add(List<String> conflicts, String core) {
        if (conflicts == null || core == null) return;
        int hits = 0;
        for (String existing : conflicts) {
            if (existing != null && existing.contains("disagreement diagnosis")) hits++;
            if (existing != null && existing.contains(core.substring(0, Math.min(36, core.length())))) return;
        }
        if (hits >= MAX_SIGNALS) return;
        conflicts.add("disagreement diagnosis (possible signal, requires context): " + core);
    }

    private static boolean fragileAgreement(String a, String b) {
        String left = target(a);
        String right = target(b);
        if (left.isEmpty() || !left.equals(right)) return false;
        String mechanismA = mechanism(a);
        String mechanismB = mechanism(b);
        if (mechanismA.isEmpty() || mechanismB.isEmpty()) return false;
        if (sharesContentWord(mechanismA, mechanismB)) return false;
        return !hasFlip(a) && !hasFlip(b);
    }

    private static boolean factValueFork(String a, String b) {
        boolean normA = normativeClose(a);
        boolean normB = normativeClose(b);
        boolean factA = empiricalClose(a);
        boolean factB = empiricalClose(b);
        if (!((normA && factB && !normB) || (normB && factA && !normA))) return false;
        return opposed(a, b);
    }

    private static boolean opposed(String a, String b) {
        String left = target(a);
        String right = target(b);
        if (!left.isEmpty() && !right.isEmpty() && !left.equals(right)) return true;
        return (affirmative(a) && negative(b)) || (affirmative(b) && negative(a));
    }

    private static boolean affirmative(String text) {
        return has(text, "рекомендую запуск", "стоит запустить", "стоит делать", "нужно делать",
                "надо делать", "рекомендую делать", "go ahead", "should do it", "recommend doing");
    }

    private static boolean negative(String text) {
        return has(text, "не рекомендую", "не стоит", "не надо делать", "не стоит запускать",
                "не запускаем", "recommend against", "do not proceed", "should not");
    }

    private static boolean normativeClose(String text) {
        return has(text,
                "поэтому должен", "поэтому должна", "значит надо", "значит нужно",
                "это правильно, значит", "морально обязан", "справедливо, поэтому",
                "therefore we ought", "so we ought", "it is right, so");
    }

    private static boolean empiricalClose(String text) {
        return has(text,
                "данные показывают", "по вероятности", "ожидаемая ценность",
                "базовая частота", "evidence shows", "expected value", "base rate");
    }

    private static boolean hasFlip(String text) {
        return has(text,
                "изменил бы", "изменило бы", "переключ", "would change", "would flip",
                "опроверг", "если увидим", "если окажется", "если провал",
                "критерий остановки", "kill criterion", "что изменит вывод");
    }

    private static boolean acknowledges(String text) {
        return has(text,
                "disagreement diagnosis", "хрупкое согласие", "факт против нормы",
                "тип разногласия", "нормативная посылка", "normative premise");
    }

    private static String target(String text) {
        Matcher matcher = TARGET.matcher(text == null ? "" : text);
        if (!matcher.find()) return "";
        return matcher.group(1).toLowerCase(Locale.ROOT);
    }

    private static String mechanism(String text) {
        Matcher matcher = BECAUSE.matcher(text == null ? "" : text);
        if (!matcher.find()) return "";
        return matcher.group(1).toLowerCase(Locale.ROOT).trim();
    }

    private static boolean sharesContentWord(String left, String right) {
        String[] parts = left.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+");
        for (String part : parts) {
            if (part.length() < 5 || isStop(part)) continue;
            if (containsWord(right, part)) return true;
        }
        return false;
    }

    private static boolean containsWord(String text, String word) {
        String[] parts = text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+");
        for (String part : parts) {
            if (part.equals(word)) return true;
        }
        return false;
    }

    private static boolean isStop(String word) {
        return word.equals("потому") || word.equals("что") || word.equals("этого")
                || word.equals("этой") || word.equals("этом") || word.equals("будет")
                || word.equals("можно") || word.equals("нужно") || word.equals("только")
                || word.equals("which") || word.equals("there") || word.equals("their")
                || word.equals("about") || word.equals("would") || word.equals("could");
    }

    private static boolean has(String text, String... phrases) {
        if (text == null) return false;
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
