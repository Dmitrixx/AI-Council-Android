package com.aicouncil.app;

/**
 * Rules for recovering when a primary expert call fails.
 * Pure logic — no network.
 */
public final class FallbackProviderPolicy {
    public static final int MAX_SLOT_RETRIES = 1;

    public static final String RETRYING = "Повтор запроса после сбоя…";
    public static final String FALLBACK_USED = "Ответ получен через запасной провайдер";
    public static final String NO_FALLBACK = "Запасной провайдер недоступен";

    private FallbackProviderPolicy() {}

    public static int chooseFallbackSlot(int failedSlot, boolean[] configured, boolean freeOnly, boolean[] markedFree) {
        if (configured == null) return -1;
        int failed = clampSlot(failedSlot);
        int[] order = new int[]{2, 1, 0, 3};
        for (int slot : order) {
            if (slot == failed) continue;
            if (!usable(slot, configured, freeOnly, markedFree)) continue;
            return slot;
        }
        return -1;
    }

    public static boolean shouldRetrySameSlot(int httpOrLogicFailureCount) {
        return httpOrLogicFailureCount >= 1 && httpOrLogicFailureCount <= MAX_SLOT_RETRIES;
    }

    public static String retryStatus(int attempt) {
        int a = Math.max(1, attempt);
        return RETRYING + " (попытка " + a + ")";
    }

    public static String fallbackStatus(int fromSlot, int toSlot) {
        return FALLBACK_USED + " · ИИ №" + (clampSlot(fromSlot) + 1) + " → ИИ №" + (clampSlot(toSlot) + 1);
    }

    private static int clampSlot(int slot) {
        if (slot < 0) return 0;
        if (slot > 3) return 3;
        return slot;
    }

    private static boolean usable(int index, boolean[] configured, boolean freeOnly, boolean[] markedFree) {
        boolean present = configured != null && index >= 0 && index < configured.length && configured[index];
        if (!present) return false;
        return !freeOnly || (markedFree != null && index < markedFree.length && markedFree[index]);
    }
}
