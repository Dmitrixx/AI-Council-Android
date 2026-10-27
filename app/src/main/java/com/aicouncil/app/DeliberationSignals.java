package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Extra local deliberation signals (v2.26+): urgency, evidence grade,
 * cost/benefit framing and obligation clashes. No network.
 */
public final class DeliberationSignals {
    private DeliberationSignals() {}

    public static void detectAll(String a, String b, List<String> conflicts) {
        detectUrgency(a, b, conflicts);
        detectEvidenceGrade(a, b, conflicts);
        detectCostBenefitFraming(a, b, conflicts);
        detectObligation(a, b, conflicts);
    }

    /** One expert pushes immediate action; the other wants to wait. */
    static void detectUrgency(String a, String b, List<String> conflicts) {
        int ua = urgencySign(a), ub = urgencySign(b);
        if (ua != 0 && ub != 0 && ua != ub) {
            conflicts.add("срочность: действовать сейчас против отложить / подождать");
        }
    }

    private static int urgencySign(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        boolean now = lower.contains("немедленно") || lower.contains("сейчас") || lower.contains("срочно")
                || lower.contains("как можно скорее") || lower.contains("without delay")
                || lower.contains("immediately") || lower.contains("right now") || lower.contains("asap")
                || lower.contains("негайно") || lower.contains("якомога швидше");
        boolean later = lower.contains("подождать") || lower.contains("отложить") || lower.contains("не сейчас")
                || lower.contains("позже") || lower.contains("wait until") || lower.contains("postpone")
                || lower.contains("not yet") || lower.contains("defer") || lower.contains("зачекати")
                || lower.contains("відкласти") || lower.contains("пізніше");
        if (now && later) return 0;
        if (now) return 1;
        if (later) return -1;
        return 0;
    }

    /** Hard evidence (RCT / meta / systematic) vs anecdote / single case. */
    static void detectEvidenceGrade(String a, String b, List<String> conflicts) {
        int ga = evidenceGrade(a), gb = evidenceGrade(b);
        if ((ga >= 2 && gb <= 0) || (gb >= 2 && ga <= 0)) {
            conflicts.add("сила доказательств: систематика/RCT против анекдота или единичного случая");
        }
    }

    private static int evidenceGrade(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        int grade = 0;
        if (lower.contains("meta-analysis") || lower.contains("мета-анализ") || lower.contains("метааналіз")
                || lower.contains("systematic review") || lower.contains("систематическ") || lower.contains("систематичн")) {
            grade = Math.max(grade, 3);
        }
        if (lower.contains("randomized") || lower.contains("рандомиз") || lower.contains("rct")
                || lower.contains("двойное слепое") || lower.contains("double-blind")) {
            grade = Math.max(grade, 2);
        }
        if (lower.contains("когорт") || lower.contains("cohort") || lower.contains("case-control")
                || lower.contains("controlled trial")) {
            grade = Math.max(grade, 1);
        }
        if (lower.contains("анекдот") || lower.contains("anecdote") || lower.contains("один случай")
                || lower.contains("в моей практике") || lower.contains("in my experience")
                || lower.contains("у моїй практиці") || lower.contains("heard that")) {
            grade = Math.min(grade, 0) - 1;
        }
        return grade;
    }

    /** One answer frames only costs; the other only benefits. */
    static void detectCostBenefitFraming(String a, String b, List<String> conflicts) {
        int fa = costBenefitFrame(a), fb = costBenefitFrame(b);
        if (fa != 0 && fb != 0 && fa != fb) {
            conflicts.add("рамка: только издержки против только выгод (нет общего баланса)");
        }
    }

    private static int costBenefitFrame(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        int cost = count(lower, "стоимость", "затрат", "издержк", "цена", "риск", "убыт", "cost", "expense",
                "drawback", "downside", "вартість", "витрат", "збитк");
        int benefit = count(lower, "выгод", "польз", "преимущ", "профит", "benefit", "upside", "gain",
                "advantage", "вигод", "переваг");
        if (cost >= 2 && benefit == 0) return -1;
        if (benefit >= 2 && cost == 0) return 1;
        return 0;
    }

    /** Must / required vs optional / discretionary. */
    static void detectObligation(String a, String b, List<String> conflicts) {
        int oa = obligationSign(a), ob = obligationSign(b);
        if (oa != 0 && ob != 0 && oa != ob) {
            conflicts.add("обязательство: обязательно / must против по желанию / optional");
        }
    }

    private static int obligationSign(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        boolean must = lower.contains("обязательн") || lower.contains("необходимо") || lower.contains("должен")
                || lower.contains("нужно обязательно") || lower.contains("must ") || lower.contains("required")
                || lower.contains("mandatory") || lower.contains("обов'язков") || lower.contains("має бути");
        boolean optional = lower.contains("по желанию") || lower.contains("опциональн") || lower.contains("можно не")
                || lower.contains("не обязательно") || lower.contains("optional") || lower.contains("discretionary")
                || lower.contains("nice to have") || lower.contains("за бажанням") || lower.contains("не обов");
        if (must && optional) return 0;
        if (must) return 1;
        if (optional) return -1;
        return 0;
    }

    private static int count(String text, String... words) {
        int c = 0;
        for (String w : words) if (text.contains(w)) c++;
        return c;
    }
}
