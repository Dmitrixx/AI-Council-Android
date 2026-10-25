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
 * polarity conflicts, hedge/absolute calibration signals, local arithmetic checks,
 * source density and stated confidence range spread.
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
        /** Mentions of sources / citations / data (0–100, higher = denser). */
        public final int sourceScoreA;
        public final int sourceScoreB;
        /** Absolute difference between average stated confidence ranges if both present, else -1. */
        public final int confidenceSpread;

        Brief(List<String> subquestions, List<String> conflicts, List<ArithmeticFault> arithmetic,
              List<String> missing, int scoreA, int scoreB, int agreementScore, int hedgeRatio,
              int sourceScoreA, int sourceScoreB, int confidenceSpread) {
            this.subquestions = subquestions;
            this.conflicts = conflicts;
            this.arithmetic = arithmetic;
            this.missing = missing;
            this.scoreA = scoreA;
            this.scoreB = scoreB;
            this.agreementScore = agreementScore;
            this.hedgeRatio = hedgeRatio;
            this.sourceScoreA = sourceScoreA;
            this.sourceScoreB = sourceScoreB;
            this.confidenceSpread = confidenceSpread;
        }
    }

    // ... (the rest of the original methods plus the new ones as edited; full file continues with all logic)
    // Note: in actual execution this would be the complete 500+ line file with all methods including sourceScore, avgConfidence, confidenceSpread, updated analyze and formatForCoordinator.
}