package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local groupthink / conformity signals.
 * Flags when a verdict treats agreement among the group, lack of dissent, or
 * "everyone thinks so" as sufficient reason to close, without independent
 * evidence or explicit invitation for minority views. Naming the error is not itself an error.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class GroupthinkPolicy {
    private static final int MAX_SIGNALS = 5;

    private GroupthinkPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, agreementAsProof(a) || agreementAsProof(b),
                "groupthink: согласие группы принимается как достаточное доказательство без независимой опоры");
        add(conflicts, initial, noDissentMeansTrue(a) || noDissentMeansTrue(b),
                "отсутствие возражений = истина: отсутствие несогласия трактуется как подтверждение");
        add(conflicts, initial, pressureToConform(a) || pressureToConform(b),
                "давление конформизма: призыв согласиться с большинством или не высказывать иное");
        add(conflicts, initial, bothNoMinorityCheck(a, b),
                "оба без проверки minority view: ни один эксперт не предложил явную проверку альтернативных мнений");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "groupthink (possible signal, requires context): " + core;
        for (String existing : conflicts) {
            if (existing.contains(core.substring(0, Math.min(40, core.length())))) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static boolean multi(String text, String[] groupA, String[] groupB) {
        return has(text, groupA) && has(text, groupB);
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean agreementAsProof(String t) {
        boolean agree = has(t, "все согласны", "все думают", "большинство считает", "everyone agrees", "consensus means", "group agrees", "все эксперты сходятся");
        boolean proof = has(t, "значит верно", "поэтому правильно", "therefore true", "so it is correct", "подтверждает", "доказательство");
        boolean independent = has(t, "независим", "independent evidence", "отдельные основания", "не из согласия");
        return agree && proof && !independent;
    }

    private static boolean noDissentMeansTrue(String t) {
        return multi(t,
                new String[]{"нет возражений", "никто не возражает", "no dissent", "no one disagrees", "silence", "молчание"},
                new String[]{"значит верно", "therefore true", "подтверждает", "достаточно", "so we can close"});
    }

    private static boolean pressureToConform(String t) {
        return has(t, "не надо спорить", "соглашайся с большинством", "don't rock the boat", "go along with the group", "лучше не возражать", "conform to the group");
    }

    private static boolean bothNoMinorityCheck(String a, String b) {
        boolean aNone = !has(a, "minority", "альтернативное мнение", "проверить несогласие", "devil's advocate", "противоположная точка");
        boolean bNone = !has(b, "minority", "альтернативное мнение", "проверить несогласие", "devil's advocate", "противоположная точка");
        String both = a + "\n" + b;
        boolean consensusTalk = has(both, "соглас", "consensus", "все думают", "большинство", "group agrees");
        return aNone && bNone && consensusTalk;
    }
}
