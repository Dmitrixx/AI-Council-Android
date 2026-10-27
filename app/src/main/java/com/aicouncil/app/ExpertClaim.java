package com.aicouncil.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Typed, source-aware claim emitted by an expert.
 *
 * <p>This model deliberately keeps missing data distinct from zero/false. It is
 * Android-independent so parsing, conflict detection and tests can run on a JVM.
 * JSON decoding belongs at the provider boundary; prose is not a substitute for
 * a missing structured field.</p>
 */
public final class ExpertClaim {
    public enum Polarity { POSITIVE, NEGATIVE, NEUTRAL, UNCERTAIN }
    public enum EvidenceClass {
        PRIMARY_SOURCE, SECONDARY_SOURCE, MEASURED_DATA, CALCULATION,
        EXPERT_JUDGMENT, INFERENCE, ASSUMPTION, UNSOURCED
    }

    public final String id;
    public final String statement;
    public final Double numericValue;
    public final String unit;
    public final Polarity polarity;
    public final String timeHorizon;
    /** Calibrated self-reported confidence in [0, 1], or null when not stated. */
    public final Double confidence;
    public final EvidenceClass evidenceClass;
    public final List<String> stakeholders;
    /** Null means the expert did not assess reversibility. */
    public final Boolean reversible;
    public final String sourceQuote;
    public final String sourceId;

    public ExpertClaim(String id, String statement, Double numericValue, String unit,
                       Polarity polarity, String timeHorizon, Double confidence,
                       EvidenceClass evidenceClass, List<String> stakeholders,
                       Boolean reversible, String sourceQuote, String sourceId) {
        this.id = clean(id);
        this.statement = clean(statement);
        this.numericValue = numericValue;
        this.unit = clean(unit);
        this.polarity = polarity == null ? Polarity.UNCERTAIN : polarity;
        this.timeHorizon = clean(timeHorizon);
        this.confidence = confidence;
        this.evidenceClass = evidenceClass == null ? EvidenceClass.UNSOURCED : evidenceClass;
        this.stakeholders = immutableCleanList(stakeholders);
        this.reversible = reversible;
        this.sourceQuote = clean(sourceQuote);
        this.sourceId = clean(sourceId);
        validate();
    }

    private void validate() {
        if (statement.isEmpty()) throw new IllegalArgumentException("claim statement is required");
        if (numericValue != null && (Double.isNaN(numericValue) || Double.isInfinite(numericValue))) {
            throw new IllegalArgumentException("numericValue must be finite");
        }
        if (confidence != null && (Double.isNaN(confidence) || Double.isInfinite(confidence)
                || confidence < 0.0 || confidence > 1.0)) {
            throw new IllegalArgumentException("confidence must be in [0, 1]");
        }
        if (numericValue != null && unit.isEmpty()) {
            throw new IllegalArgumentException("numeric claims must specify a unit (use \"count\" or \"unitless\" when applicable)");
        }
        if (!sourceQuote.isEmpty() && sourceId.isEmpty()) {
            throw new IllegalArgumentException("source quote must identify its source");
        }
    }

    public boolean hasNumericValue() { return numericValue != null; }
    public boolean hasConfidence() { return confidence != null; }
    public boolean hasSourceQuote() { return !sourceQuote.isEmpty() && !sourceId.isEmpty(); }

    private static String clean(String value) { return value == null ? "" : value.trim(); }

    private static List<String> immutableCleanList(List<String> values) {
        if (values == null || values.isEmpty()) return Collections.emptyList();
        List<String> copy = new ArrayList<>();
        for (String value : values) {
            String cleaned = clean(value);
            if (!cleaned.isEmpty() && !copy.contains(cleaned)) copy.add(cleaned);
        }
        return Collections.unmodifiableList(copy);
    }
}
