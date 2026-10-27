package com.aicouncil.app;

import java.util.Arrays;
import java.util.List;

public final class ExpertClaimJsonParserSmokeTest {
    private static int passed;
    public static void main(String[] args) {
        String json = "{\"schema_version\":1,\"claims\":["
                + "{\"id\":\"c1\",\"statement\":\"Route length\",\"numeric_value\":100,"
                + "\"unit\":\"km\",\"polarity\":\"positive\",\"time_horizon\":\"today\","
                + "\"confidence\":0.9,\"evidence_class\":\"measured_data\","
                + "\"stakeholders\":[\"driver\",\"customer\"],\"reversible\":true,"
                + "\"source_quote\":\"100 km\",\"source_id\":\"s1\"},"
                + "{\"statement\":\"Qualitative claim\",\"polarity\":\"uncertain\"}]}";
        List<ExpertClaim> claims = ExpertClaimJsonParser.parse(json);
        check(claims.size() == 2, "two claims parsed");
        check(claims.get(0).numericValue == 100.0, "number parsed");
        check(claims.get(0).sourceId.equals("s1"), "source id parsed");
        check(claims.get(0).stakeholders.size() == 2, "stakeholders parsed");
        check(claims.get(1).numericValue == null, "unknown number remains null");
        check(claims.get(1).confidence == null, "unknown confidence remains null");
        check(ExpertClaimJsonParser.parse("{\"schema_version\":1,\"claims\":[]}").isEmpty(),
                "empty claims array allowed");

        invalid("{\"schema_version\":2,\"claims\":[]}", "unsupported version rejected");
        invalid("{\"schema_version\":1,\"claims\":[{\"statement\":\"x\",\"numeric_value\":2}]}",
                "numeric claim without unit rejected");
        invalid("{\"schema_version\":1,\"claims\":[{\"statement\":\"x\",\"confidence\":2}]}",
                "out-of-range confidence rejected");
        invalid("{\"schema_version\":1,\"claims\":[],\"claims\":[]}", "duplicate keys rejected");
        invalid("{\"schema_version\":1,\"claims\":[]} trailing", "trailing data rejected");
        invalid("{\"schema_version\":1,\"claims\":[{\"statement\":\"x\",\"polarity\":\"maybe-ish\"}]}",
                "unknown enum rejected");
        System.out.println("ExpertClaimJsonParserSmokeTest: " + passed + "/" + passed + " passed");
    }
    private static void check(boolean ok, String description) {
        if (!ok) throw new AssertionError(description);
        passed++;
    }
    private static void invalid(String json, String description) {
        try { ExpertClaimJsonParser.parse(json); }
        catch (IllegalArgumentException expected) { passed++; return; }
        throw new AssertionError(description);
    }
}
