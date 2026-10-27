package com.aicouncil.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Conservative, deterministic comparison of claims that share a normalized
 * statement. It only emits conflicts when both experts make comparable claims;
 * absent values and merely different wording are not treated as disagreement.
 */
public final class StructuredClaimAnalyzer {
    public enum Type {
        NUMERIC_VALUE, POLARITY, TIME_HORIZON, REVERSIBILITY, EVIDENCE_QUALITY
    }

    public static final class Conflict {
        public final Type type;
        public final String statement;
        public final String expertAValue;
        public final String expertBValue;
        public final String note;

        private Conflict(Type type, String statement, String expertAValue,
                         String expertBValue, String note) {
            this.type = type;
            this.statement = statement;
            this.expertAValue = expertAValue;
            this.expertBValue = expertBValue;
            this.note = note;
        }
    }

    private StructuredClaimAnalyzer() {}

    public static List<Conflict> compare(List<ExpertClaim> expertA, List<ExpertClaim> expertB) {
        Map<String, ExpertClaim> a = index(expertA);
        Map<String, ExpertClaim> b = index(expertB);
        List<Conflict> conflicts = new ArrayList<>();
        for (Map.Entry<String, ExpertClaim> entry : a.entrySet()) {
            ExpertClaim left = entry.getValue();
            ExpertClaim right = b.get(entry.getKey());
            if (right == null) continue;
            String statement = left.statement;
            compareNumbers(left, right, statement, conflicts);
            if (isDefinite(left.polarity) && isDefinite(right.polarity)
                    && left.polarity != right.polarity) {
                conflicts.add(new Conflict(Type.POLARITY, statement, left.polarity.name(),
                        right.polarity.name(), "Experts assign opposite polarity to the same normalized claim."));
            }
            if (!left.timeHorizon.isEmpty() && !right.timeHorizon.isEmpty()
                    && !normalize(left.timeHorizon).equals(normalize(right.timeHorizon))) {
                conflicts.add(new Conflict(Type.TIME_HORIZON, statement, left.timeHorizon,
                        right.timeHorizon, "Time horizons differ; determine whether the claims can both hold."));
            }
            if (left.reversible != null && right.reversible != null
                    && !left.reversible.equals(right.reversible)) {
                conflicts.add(new Conflict(Type.REVERSIBILITY, statement, left.reversible.toString(),
                        right.reversible.toString(), "Experts disagree about whether the decision can be reversed."));
            }
            if (left.evidenceClass != right.evidenceClass) {
                conflicts.add(new Conflict(Type.EVIDENCE_QUALITY, statement, left.evidenceClass.name(),
                        right.evidenceClass.name(), "Evidence classes differ; inspect the cited evidence before choosing."));
            }
        }
        return Collections.unmodifiableList(conflicts);
    }

    private static Map<String, ExpertClaim> index(List<ExpertClaim> claims) {
        Map<String, ExpertClaim> indexed = new LinkedHashMap<>();
        if (claims == null) return indexed;
        for (ExpertClaim claim : claims) {
            if (claim == null) continue;
            String key = normalize(claim.statement);
            if (!key.isEmpty() && !indexed.containsKey(key)) indexed.put(key, claim);
        }
        return indexed;
    }

    private static void compareNumbers(ExpertClaim a, ExpertClaim b, String statement,
                                       List<Conflict> conflicts) {
        if (a.numericValue == null || b.numericValue == null) return;
        Double bInAUnit = convert(b.numericValue, b.unit, a.unit);
        if (bInAUnit == null) return; // Unknown units are not compared as if interchangeable.
        double scale = Math.max(1.0, Math.max(Math.abs(a.numericValue), Math.abs(bInAUnit)));
        if (Math.abs(a.numericValue - bInAUnit) > scale * 1e-9) {
            conflicts.add(new Conflict(Type.NUMERIC_VALUE, statement,
                    format(a.numericValue) + " " + a.unit,
                    format(b.numericValue) + " " + a.unit,
                    "Comparable units disagree after normalization."));
        }
    }

    /** Converts a small, explicit unit set. Unsupported conversions remain unknown. */
    private static Double convert(double value, String from, String to) {
        String source = unit(from), target = unit(to);
        if (source.equals(target)) return value;
        if (isKm(source) && isMi(target)) return value * 0.621371192237334;
        if (isMi(source) && isKm(target)) return value / 0.621371192237334;
        if (isM(source) && isKm(target)) return value / 1000.0;
        if (isKm(source) && isM(target)) return value * 1000.0;
        if (isM(source) && isMi(target)) return value / 1609.344;
        if (isMi(source) && isM(target)) return value * 1609.344;
        if (isKg(source) && isLb(target)) return value * 2.204622621848776;
        if (isLb(source) && isKg(target)) return value / 2.204622621848776;
        return null;
    }

    private static String unit(String value) {
        String s = normalize(value).replace(" ", "");
        if (s.equals("kilometer") || s.equals("kilometers") || s.equals("kilometre") || s.equals("kilometres")) return "km";
        if (s.equals("mile") || s.equals("miles")) return "mi";
        if (s.equals("meter") || s.equals("meters") || s.equals("metre") || s.equals("metres")) return "m";
        if (s.equals("kilogram") || s.equals("kilograms")) return "kg";
        if (s.equals("pound") || s.equals("pounds") || s.equals("lbs")) return "lb";
        if (s.equals("percent") || s.equals("percentage")) return "%";
        return s;
    }
    private static boolean isKm(String s) { return s.equals("km"); }
    private static boolean isMi(String s) { return s.equals("mi"); }
    private static boolean isM(String s) { return s.equals("m"); }
    private static boolean isKg(String s) { return s.equals("kg"); }
    private static boolean isLb(String s) { return s.equals("lb"); }

    private static boolean isDefinite(ExpertClaim.Polarity p) {
        return p == ExpertClaim.Polarity.POSITIVE || p == ExpertClaim.Polarity.NEGATIVE;
    }
    private static String normalize(String value) {
        if (value == null) return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < value.toLowerCase(Locale.ROOT).trim().length(); i++) {
            char c = value.toLowerCase(Locale.ROOT).trim().charAt(i);
            if (Character.isLetterOrDigit(c)) b.append(c);
            else if (Character.isWhitespace(c)) b.append(' ');
        }
        return b.toString().trim().replaceAll("\\s+", " ");
    }
    private static String format(double value) {
        if (value == Math.rint(value)) return Long.toString((long) value);
        return Double.toString(value);
    }
}
