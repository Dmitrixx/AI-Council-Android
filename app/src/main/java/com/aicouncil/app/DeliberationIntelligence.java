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

/** RESTORE_PLACEHOLDER - will be replaced */
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
            this.scoreA = scoreA; this.scoreB = scoreB; this.hedgeRatio = hedgeRatio;
            this.agreement = agreement; this.evidenceA = evidenceA; this.evidenceB = evidenceB;
            this.qualityGaps = qualityGaps == null ? Collections.emptyList() : qualityGaps;
        }
    }
    private DeliberationIntelligence() {}
    public static List<ArithmeticFault> findArithmetic(String text) { return new ArrayList<>(); }
    public static String localCheckFooter(String text) { return ""; }
    public static List<String> subquestions(String task) { return new ArrayList<>(); }
    public static String expertAddendum(int slot, String task) { return ""; }
    public static Brief analyze(String task, String answerA, String answerB) {
        List<String> conflicts = new ArrayList<>();
        DeliberationSignals.detectAll(answerA == null ? "" : answerA, answerB == null ? "" : answerB, conflicts);
        return new Brief(conflicts, new ArrayList<>(), 50, 50, 50, 50, 50, 50, new ArrayList<>());
    }
    public static String formatForCoordinator(Brief brief) {
        if (brief == null || brief.conflicts.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("ЛОКАЛЬНАЯ КАРТА РАЗНОГЛАСИЙ:\nРАСХОЖДЕНИЯ:\n");
        for (String c : brief.conflicts) sb.append("• ").append(c).append("\n");
        sb.append(resolutionLine(brief)).append("\n");
        sb.append("Не усредняй срочность, силу доказательств, рамку cost/benefit и обязательства.\n");
        return sb.toString().trim();
    }
    public static String resolutionLine(Brief brief) {
        if (brief == null || brief.conflicts.isEmpty())
            return "РЕШЕНИЕ КАРТЫ: явных конфликтов нет; не повышай уверенность из-за похожих формулировок.";
        return "РЕШЕНИЕ КАРТЫ: не усредняй. Сначала разреши «" + brief.conflicts.get(0) + "».";
    }
}
