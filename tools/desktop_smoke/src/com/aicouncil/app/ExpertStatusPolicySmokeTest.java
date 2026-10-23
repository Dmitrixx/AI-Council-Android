package com.aicouncil.app;

/** Desktop smoke for ExpertStatusPolicy. */
public final class ExpertStatusPolicySmokeTest {
    public static void main(String[] args) {
        if (!ExpertStatusPolicy.DONE.equals(ExpertStatusPolicy.afterCall(false, false)))
            throw new AssertionError("done");
        if (!ExpertStatusPolicy.ERROR.equals(ExpertStatusPolicy.afterCall(true, false)))
            throw new AssertionError("error");
        if (!ExpertStatusPolicy.PARTIAL.equals(ExpertStatusPolicy.afterCall(false, true)))
            throw new AssertionError("partial");
        String p = ExpertStatusPolicy.progressStage(3, 2);
        if (p == null || !p.contains("критик")) throw new AssertionError(p);
        String b = ExpertStatusPolicy.finalBanner(false, false, true);
        if (b == null || !b.contains("Частичный")) throw new AssertionError(b);
        System.out.println("ExpertStatusPolicySmokeTest: OK");
    }
}
