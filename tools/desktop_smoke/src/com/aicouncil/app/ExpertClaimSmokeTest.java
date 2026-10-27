package com.aicouncil.app;

import java.util.Arrays;
import java.util.Collections;

public final class ExpertClaimSmokeTest {
    private static int passed;
    public static void main(String[] args) {
        ExpertClaim claim = new ExpertClaim("c1", "Revenue increases", 20.0, "%",
                ExpertClaim.Polarity.POSITIVE, "12 months", 0.8,
                ExpertClaim.EvidenceClass.MEASURED_DATA,
                Arrays.asList("customer", "customer", "investor"), true,
                "Revenue rose by 20%", "source-1");
        check(claim.hasNumericValue(), "numeric value retained");
        check(claim.hasConfidence(), "confidence retained");
        check(claim.hasSourceQuote(), "quote tied to source id");
        check(claim.stakeholders.size() == 2, "stakeholders deduplicated");
        check(claim.stakeholders.get(0).equals("customer"), "stakeholder order preserved");
        check(claim.reversible != null && claim.reversible, "reversibility retained");

        ExpertClaim unknowns = new ExpertClaim("", "A qualitative claim", null, "",
                null, "", null, null, null, null, "", "");
        check(!unknowns.hasNumericValue(), "missing number is not zero");
        check(!unknowns.hasConfidence(), "missing confidence stays missing");
        check(unknowns.evidenceClass == ExpertClaim.EvidenceClass.UNSOURCED,
                "missing evidence class is conservative");
        check(unknowns.reversible == null, "missing reversibility stays unknown");
        expectInvalid(() -> new ExpertClaim("", "bad confidence", null, "", null, "",
                1.1, null, null, null, "", ""), "confidence range enforced");
        expectInvalid(() -> new ExpertClaim("", "NaN", Double.NaN, "count", null, "",
                null, null, null, null, "", ""), "non-finite number rejected");
        expectInvalid(() -> new ExpertClaim("", "number without unit", 4.0, "", null, "",
                null, null, null, null, "", ""), "numeric unit required");
        expectInvalid(() -> new ExpertClaim("", "orphan quote", null, "", null, "",
                null, null, null, null, "quoted text", ""), "source quote requires source id");
        expectInvalid(() -> new ExpertClaim("", "", null, "", null, "",
                null, null, null, null, "", ""), "empty claim rejected");
        System.out.println("ExpertClaimSmokeTest: " + passed + "/" + passed + " passed");
    }
    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        passed++;
    }
    private static void expectInvalid(Runnable action, String name) {
        try { action.run(); } catch (IllegalArgumentException expected) { passed++; return; }
        throw new AssertionError(name);
    }
}
