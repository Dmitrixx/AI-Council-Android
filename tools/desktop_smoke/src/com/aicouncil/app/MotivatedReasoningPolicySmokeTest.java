package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class MotivatedReasoningPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("protects preferred conclusion by explaining away data",
                "The inconvenient data can be explained away, therefore the conclusion stands and our side is right.",
                "");
        signal("both experts close on conclusion-preserving reading",
                "Это подтверждает нашу позицию, как мы и ожидали. Поэтому вердикт остаётся.",
                "This confirms our view as we expected all along, so we stick with the original conclusion.");
        noSignal("names the full balance of evidence",
                "We update on the full balance of evidence, including disconfirming data.",
                "");
        noSignal("ordinary update without protective language",
                "The new study lowers our confidence. We revise the prior.",
                "Agree. Priors should move.");
        noSignal("acknowledges motivated reasoning risk",
                "There is a risk of motivated reasoning here; we should still weigh the disconfirming points.",
                "");

        List<String> idempotent = new ArrayList<String>();
        MotivatedReasoningPolicy.detect(
                "The inconvenient data can be explained away, therefore the conclusion stands.",
                "",
                idempotent);
        int once = idempotent.size();
        MotivatedReasoningPolicy.detect(
                "The inconvenient data can be explained away, therefore the conclusion stands.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("MotivatedReasoningPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
        if (!out.isEmpty()) {
            check(out.get(0).contains("possible signal"), label + " is marked as a hypothesis");
        }
    }

    private static void noSignal(String label, String a, String b) {
        check(run(a, b).isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        MotivatedReasoningPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
