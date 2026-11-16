package com.aicouncil.app;

public final class AdaptiveLearningPolicySmokeTest {
    private static int checks;
    private static void check(String name, boolean value) {
        checks++;
        if (!value) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
    }

    public static void main(String[] args) {
        boolean[] all = {true, true, true, true};
        boolean[] free = {true, true, true, true};
        String profile = AdaptiveLearningPolicy.normalizeProfile(null);
        check("empty profile is fixed-size and versioned", profile.startsWith("v1:"));
        check("profiles never contain task text", !profile.contains("invoice"));
        check("task classes distinguish math and code",
                AdaptiveLearningPolicy.categoryForTask("Посчитай 12 процентов") !=
                        AdaptiveLearningPolicy.categoryForTask("Исправь баг в Java"));
        check("single rating cannot change Auto routing",
                AdaptiveLearningPolicy.recommendMode(profile, "general question", 1, all, false, free) == 1);

        for (int i = 0; i < 4; i++)
            profile = AdaptiveLearningPolicy.recordFeedback(profile, "general question", 2, true);
        check("four positive ratings teach a better strategy",
                AdaptiveLearningPolicy.recommendMode(profile, "general question", 1, all, false, free) == 2);
        check("sample count is tracked per task class and mode",
                AdaptiveLearningPolicy.sampleCount(profile, "general question", 2) == 4);
        check("no cross-category leakage",
                AdaptiveLearningPolicy.sampleCount(profile, "Посчитай 12 процентов", 2) == 0);

        String negative = "";
        for (int i = 0; i < 4; i++)
            negative = AdaptiveLearningPolicy.recordFeedback(negative, "general question", 0, true);
        check("quick mode cannot win without a configured primary provider",
                AdaptiveLearningPolicy.recommendMode(negative, "general question", 1,
                        new boolean[]{false, true, true, true}, false, free) == 1);
        check("free-only routing excludes unverified providers",
                AdaptiveLearningPolicy.recommendMode(profile, "general question", 1, all,
                        true, new boolean[]{true, false, true, true}) == 1);

        String critical = "";
        for (int i = 0; i < 4; i++)
            critical = AdaptiveLearningPolicy.recordFeedback(critical, "medical dosage calculation", 0, true);
        check("high-stakes tasks never learn down to one-model quick mode",
                AdaptiveLearningPolicy.recommendMode(critical, "medical dosage calculation", 2,
                        all, false, free) == 2);
        check("explicit manual baseline remains the fallback",
                AdaptiveLearningPolicy.recommendMode(profile, "general question", 1,
                        all, false, free) == 2);

        String corrupt = AdaptiveLearningPolicy.normalizeProfile("v9:broken");
        check("unknown profile versions safely reset", corrupt.startsWith("v1:"));
        String saturated = "";
        for (int i = 0; i < 90; i++)
            saturated = AdaptiveLearningPolicy.recordFeedback(saturated, "general question", 1, true);
        check("frequent use is bounded and decayed", AdaptiveLearningPolicy.sampleCount(
                saturated, "general question", 1) <= 33);
        check("out-of-range strategy cannot corrupt profile",
                AdaptiveLearningPolicy.recordFeedback(profile, "general question", 9, true).equals(profile));
        System.out.println("AdaptiveLearningPolicySmokeTest: " + checks + "/" + checks + " passed");
    }
}
