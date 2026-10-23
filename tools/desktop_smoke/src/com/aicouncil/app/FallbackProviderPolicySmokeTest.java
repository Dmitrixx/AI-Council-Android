package com.aicouncil.app;

/** Desktop smoke for FallbackProviderPolicy. */
public final class FallbackProviderPolicySmokeTest {
    public static void main(String[] args) {
        boolean[] all = new boolean[]{true, true, true};
        boolean[] free = new boolean[]{true, true, true};
        int fb = FallbackProviderPolicy.chooseFallbackSlot(0, all, false, free);
        if (fb < 0 || fb == 0) throw new AssertionError("expected alternate slot, got " + fb);
        int x = FallbackProviderPolicy.chooseFallbackSlot(1, new boolean[]{true, true, false}, false, free);
        if (x != 0) throw new AssertionError("expected 0, got " + x);
        if (!FallbackProviderPolicy.shouldRetrySameSlot(1)) throw new AssertionError("retry");
        if (FallbackProviderPolicy.shouldRetrySameSlot(0)) throw new AssertionError("no retry on 0");
        String st = FallbackProviderPolicy.fallbackStatus(0, 2);
        if (st == null || !st.contains("ИИ №1") || !st.contains("ИИ №3")) throw new AssertionError(st);
        System.out.println("FallbackProviderPolicySmokeTest: OK");
    }
}
