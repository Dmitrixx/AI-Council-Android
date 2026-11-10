package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local propositional-fallacy policy.
 * Affirming the consequent treats an observed effect as proof of one cause.
 * Denying the antecedent treats a missing sufficient condition as proof the effect cannot occur.
 * Modus ponens and modus tollens are valid and must stay quiet.
 * Naming the fallacy is not the error. Signals remain hypotheses that require context.
 */
public final class PropositionalFallacyPolicy {
    private static final int MAX_SIGNALS = 3;

    private PropositionalFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        int already = countOwn(conflicts);
        boolean affA = affirmsConsequent(a);
        boolean affB = affirmsConsequent(b);
        boolean denA = deniesAntecedent(a);
        boolean denB = deniesAntecedent(b);
        add(conflicts, initial, already, affA || affB,
                "утверждение консеквента: следствие принято за доказательство одной причины");
        add(conflicts, initial, already, affA && affB,
                "оба эксперта выводят причину из одного наблюдаемого следствия");
        add(conflicts, initial, already, affA ^ affB,
                "утверждение консеквента: один эксперт выводит причину из следствия");
        add(conflicts, initial, already, denA || denB,
                "отрицание антецедента: несработавшее достаточное условие принято за опровержение следствия");
        add(conflicts, initial, already, denA && denB,
                "оба эксперта отменяют следствие из-за одного несработавшего условия");
        add(conflicts, initial, already, denA ^ denB,
                "отрицание антецедента: один эксперт отменяет следствие без отрицания самого следствия");
    }

    private static void add(List<String> conflicts, int initial, int already, boolean hit, String core) {
        if (!hit) return;
        if (already + conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "propositional fallacy (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static int countOwn(List<String> conflicts) {
        int n = 0;
        for (String existing : conflicts) {
            if (existing.contains("propositional fallacy")) n++;
        }
        return n;
    }

    /** Q, therefore P — not modus ponens (P, therefore Q) and not modus tollens (not Q, therefore not P). */
    private static boolean affirmsConsequent(String t) {
        if (t == null || t.isEmpty() || acknowledges(t) || validInference(t)) return false;
        return has(t,
                "раз следствие есть, значит причина",
                "следствие подтверждено — значит посылка",
                "следствие есть, значит причина доказана",
                "поскольку результат есть, причина доказана",
                "симптом есть, значит диагноз доказан",
                "консеквент истинен, следовательно антецедент",
                "следствие наблюдается — причина установлена",
                "раз наслідок підтверджено, причина встановлена",
                "наслідок є, отже причина доведена",
                "affirming the consequent",
                "the consequent is true so the antecedent",
                "because the effect happened, the cause is proven",
                "effect is present, so the cause must be",
                "the result happened, therefore the plan was the cause",
                "q is true, therefore p");
    }

    /** Not P, therefore not Q — a failed sufficient condition is not a disproof. */
    private static boolean deniesAntecedent(String t) {
        if (t == null || t.isEmpty() || acknowledges(t) || validInference(t)) return false;
        return has(t,
                "denying the antecedent",
                "отрицание антецедента",
                "заперечення антецедента",
                "условия нет — значит следствия нет",
                "раз причины нет, следствия быть не может",
                "достаточное условие не выполнено, значит вывод ложен",
                "один из путей не сработал — значит результат невозможен",
                "умови немає — отже наслідку бути не може",
                "antecedent is false, so the consequent is false",
                "not p, therefore not q",
                "the sufficient condition failed, so the effect is impossible");
    }

    /** Naming the repair, or stating the valid forms, is not the error. */
    private static boolean acknowledges(String t) {
        return has(t,
                "утверждение консеквента не доказательство",
                "отрицание антецедента не доказательство",
                "следствие не доказывает причину",
                "отсутствие одного достаточного условия не отменяет следствие",
                "не выводи причину из одного следствия",
                "affirming the consequent is invalid",
                "denying the antecedent is invalid",
                "a consequent does not prove its antecedent",
                "a failed sufficient condition does not refute the effect");
    }

    /** Modus ponens and modus tollens stay valid even beside conditional language. */
    private static boolean validInference(String t) {
        return has(t,
                "modus ponens",
                "modus tollens",
                "нет следствия, значит нет этой причины",
                "не q, значит не p",
                "not q, therefore not p",
                "если p, то q; p, значит q",
                "if p then q; p, therefore q");
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
