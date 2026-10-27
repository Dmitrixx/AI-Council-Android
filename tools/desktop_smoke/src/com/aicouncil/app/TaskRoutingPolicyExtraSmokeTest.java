package com.aicouncil.app;

/** Extra smoke for tool-aware TaskRoutingPolicy helpers. */
public final class TaskRoutingPolicyExtraSmokeTest {
    public static void main(String[] args) {
        boolean[] cfg = new boolean[]{true, true, true, true};
        boolean[] free = new boolean[]{true, true, true};
        int mode = TaskRoutingPolicy.chooseMode("напиши код на python для сортировки", cfg, false, free);
        if (mode != 3) throw new AssertionError("code task should prefer deep analysis, got " + mode);
        String hint = TaskRoutingPolicy.toolAwarenessHint("посчитай интеграл и процент");
        if (hint == null || hint.isEmpty()) throw new AssertionError("math hint empty");
        if (!TaskRoutingPolicy.looksLikeMath("calculate the sum")) throw new AssertionError("math");
        if (!TaskRoutingPolicy.looksLikeCodeTask("fix this stack trace bug")) throw new AssertionError("code");
        System.out.println("TaskRoutingPolicyExtraSmokeTest: OK");
    }
}
