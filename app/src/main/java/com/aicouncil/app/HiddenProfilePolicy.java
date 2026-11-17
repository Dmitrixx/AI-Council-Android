package com.aicouncil.app;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared-information bias inside a council.
 * A claim every seat repeats is one observation, not several.
 * Closing because "everyone agrees", while a measurement or named fact
 * held on only one side goes unanswered, is a hidden profile.
 * Quoting that fact, or saying agreement is not independent evidence, is not an error.
 * The result is a hypothesis for the coordinator, not a verdict.
 */
public final class HiddenProfilePolicy {
    private static final int MAX_SIGNALS = 1;
    private static final Pattern NUMBER = Pattern.compile("(?<!\\d)(\\d{2,6})(?!\\d)");

    private HiddenProfilePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        if (a.length() < 24 || b.length() < 24) return;
        if (acknowledges(a) || acknowledges(b)) return;
        if (hidden(a, b) || hidden(b, a)) {
            add(conflicts, "скрытый профиль: согласие принято за независимое доказательство, а наблюдение только одной стороны не отвечено");
        }
    }

    private static void add(List<String> conflicts, String core) {
        if (conflicts == null || core == null) return;
        int hits = 0;
        for (String existing : conflicts) {
            if (existing != null && existing.contains("hidden profile")) hits++;
            if (existing != null && existing.contains(core.substring(0, Math.min(24, core.length())))) return;
        }
        if (hits >= MAX_SIGNALS) return;
        conflicts.add("hidden profile (possible signal, requires context): " + core);
    }

    private static boolean hidden(String consensus, String unique) {
        if (!consensusAsProof(consensus) || !closes(consensus)) return false;
        if (consensusAsProof(unique)) return false;
        return unmatchedMeasure(unique, consensus) || explicitUnshared(unique, consensus);
    }

    private static boolean consensusAsProof(String text) {
        return has(text,
                "все эксперты", "все согласны", "мнения совпали", "консенсус моделей",
                "оба эксперта подтверждают", "раз все сходятся", "поскольку все сходятся",
                "all experts agree", "since everyone agrees", "consensus of the models",
                "both experts agree");
    }

    private static boolean closes(String text) {
        return has(text,
                "рекомендую", "стоит выбрать", "стоит делать", "стоит запуск",
                "i recommend", "we should", "the decision is");
    }

    private static boolean unmatchedMeasure(String unique, String consensus) {
        if (!has(unique, "%", "процент", "отток", "churn", "частот", "вероятност",
                "выборк", "замер", "latency", "руб", "nnt", " мс", " ms")) {
            return false;
        }
        Matcher matcher = NUMBER.matcher(unique);
        while (matcher.find()) {
            if (!hasNumber(consensus, matcher.group(1))) return true;
        }
        return false;
    }

    private static boolean explicitUnshared(String unique, String consensus) {
        if (!has(unique, "единственн", "только у одного", "никто другой", "не прозвучал",
                "unshared", "only one expert", "only this seat")) {
            return false;
        }
        Matcher words = Pattern.compile("[\\p{L}]{6,}").matcher(unique);
        while (words.find()) {
            String word = words.group();
            if (isStop(word) || isMarker(word)) continue;
            if (!containsWord(consensus, word)) return true;
        }
        return false;
    }

    private static boolean acknowledges(String text) {
        return has(text,
                "скрытый профиль", "hidden profile", "shared information",
                "уникальное наблюдение учтено", "согласие не доказательство",
                "agreement is not independent");
    }

    private static boolean hasNumber(String text, String number) {
        return Pattern.compile("(?<!\\d)" + Pattern.quote(number) + "(?!\\d)").matcher(text).find();
    }

    private static boolean containsWord(String text, String word) {
        String[] parts = text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+");
        for (String part : parts) {
            if (part.equals(word)) return true;
        }
        return false;
    }

    private static boolean isMarker(String word) {
        return word.equals("единственное") || word.equals("единственный") || word.equals("единственная")
                || word.equals("прозвучал") || word.equals("прозвучало") || word.equals("unshared")
                || word.equals("expert") || word.equals("эксперты") || word.equals("эксперта");
    }

    private static boolean isStop(String word) {
        return word.equals("потому") || word.equals("этого") || word.equals("этой")
                || word.equals("только") || word.equals("одной") || word.equals("стороны")
                || word.equals("общего") || word.equals("пересказе") || word.equals("нет")
                || word.equals("which") || word.equals("there") || word.equals("their")
                || word.equals("about") || word.equals("would") || word.equals("should");
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
