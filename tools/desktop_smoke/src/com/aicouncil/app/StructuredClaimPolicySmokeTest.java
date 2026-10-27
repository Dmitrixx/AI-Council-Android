package com.aicouncil.app;

import java.util.Collections;

public final class StructuredClaimPolicySmokeTest {
    private static int passed;
    public static void main(String[] args) {
        String json = "{\"schema_version\":1,\"claims\":[{\"id\":\"x\","
                + "\"statement\":\"Revenue rises\",\"numeric_value\":5,\"unit\":\"%\","
                + "\"polarity\":\"positive\",\"confidence\":0.8,\"evidence_class\":\"measured_data\","
                + "\"source_quote\":\"+5%\",\"source_id\":\"report-7\"}]}";
        String response = "Visible answer.\n\n" + StructuredClaimPolicy.START_MARKER
                + json + StructuredClaimPolicy.END_MARKER;
        StructuredClaimPolicy.ParsedResponse parsed = StructuredClaimPolicy.parseResponse(response);
        check(parsed.structured, "valid marked JSON accepted");
        check(parsed.answer.equals("Visible answer."), "markers hidden from user-facing prose");
        check(parsed.claims.size() == 1, "claims extracted");

        StructuredClaimPolicy.ParsedResponse malformed = StructuredClaimPolicy.parseResponse(
                "Visible prose " + StructuredClaimPolicy.START_MARKER + "{bad}"
                        + StructuredClaimPolicy.END_MARKER);
        check(!malformed.structured, "malformed JSON triggers fallback");
        check(malformed.answer.contains(StructuredClaimPolicy.START_MARKER),
                "fallback preserves original response rather than dropping data");
        check(StructuredClaimPolicy.parseResponse("just prose").answer.equals("just prose"),
                "legacy prose remains compatible");
        check(StructuredClaimPolicy.formatConflictsForCoordinator(
                Collections.<ExpertClaim>emptyList(), Collections.<ExpertClaim>emptyList()).isEmpty(),
                "no fake conflicts for missing structure");
        check(StructuredClaimPolicy.contractInstruction().contains("schema_version"),
                "contract prompt advertises versioned schema");
        System.out.println("StructuredClaimPolicySmokeTest: " + passed + "/" + passed + " passed");
    }
    private static void check(boolean ok, String description) {
        if (!ok) throw new AssertionError(description);
        passed++;
    }
}
