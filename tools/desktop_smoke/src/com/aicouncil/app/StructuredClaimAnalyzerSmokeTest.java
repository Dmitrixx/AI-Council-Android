package com.aicouncil.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class StructuredClaimAnalyzerSmokeTest {
    private static int passed;
    public static void main(String[] args) {
        ExpertClaim a = claim("Traffic increases", 100.0, "km",
                ExpertClaim.Polarity.POSITIVE, "12 months", true, ExpertClaim.EvidenceClass.MEASURED_DATA);
        ExpertClaim b = claim("traffic increases", 62.1371192237334, "mi",
                ExpertClaim.Polarity.NEGATIVE, "1 month", false, ExpertClaim.EvidenceClass.ASSUMPTION);
        List<StructuredClaimAnalyzer.Conflict> conflicts =
                StructuredClaimAnalyzer.compare(Arrays.asList(a), Arrays.asList(b));
        check(has(conflicts, StructuredClaimAnalyzer.Type.POLARITY), "opposite polarity conflict");
        check(has(conflicts, StructuredClaimAnalyzer.Type.TIME_HORIZON), "time horizon conflict");
        check(has(conflicts, StructuredClaimAnalyzer.Type.REVERSIBILITY), "reversibility conflict");
        check(has(conflicts, StructuredClaimAnalyzer.Type.EVIDENCE_QUALITY), "evidence class mismatch");
        check(!has(conflicts, StructuredClaimAnalyzer.Type.NUMERIC_VALUE),
                "kilometres and miles normalized to equivalent values");

        ExpertClaim c = claim("Weather tomorrow", 20.0, "%", ExpertClaim.Polarity.POSITIVE,
                "", null, ExpertClaim.EvidenceClass.UNSOURCED);
        ExpertClaim d = claim("Weather tomorrow", 0.2, "fraction", ExpertClaim.Polarity.POSITIVE,
                "", null, ExpertClaim.EvidenceClass.UNSOURCED);
        check(!has(StructuredClaimAnalyzer.compare(Arrays.asList(c), Arrays.asList(d)),
                StructuredClaimAnalyzer.Type.NUMERIC_VALUE), "unsupported unit conversions remain unknown");
        check(StructuredClaimAnalyzer.compare(Collections.singletonList(a), Collections.<ExpertClaim>emptyList()).isEmpty(),
                "missing peer claim is not treated as disagreement");
        System.out.println("StructuredClaimAnalyzerSmokeTest: " + passed + "/" + passed + " passed");
    }
    private static ExpertClaim claim(String statement, Double n, String unit, ExpertClaim.Polarity p,
                                     String horizon, Boolean reversible, ExpertClaim.EvidenceClass evidence) {
        return new ExpertClaim("", statement, n, unit, p, horizon, null, evidence,
                null, reversible, "", "");
    }
    private static boolean has(List<StructuredClaimAnalyzer.Conflict> conflicts,
                               StructuredClaimAnalyzer.Type type) {
        for (StructuredClaimAnalyzer.Conflict conflict : conflicts) if (conflict.type == type) return true;
        return false;
    }
    private static void check(boolean ok, String description) {
        if (!ok) throw new AssertionError(description);
        passed++;
    }
}
