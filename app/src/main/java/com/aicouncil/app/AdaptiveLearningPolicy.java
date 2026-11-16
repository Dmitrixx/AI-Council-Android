package com.aicouncil.app;

import java.util.Locale;

/**
 * Tiny on-device bandit memory for Auto mode.
 * Stores only aggregate helpful/not-helpful counters per task class and strategy.
 * It never stores prompts, answers, provider names, credentials, or model weights.
 */
public final class AdaptiveLearningPolicy {
    private static final String PREFIX = "v1:";
    private static final int CATEGORIES = 6, MODES = 4, MAX_COUNT = 32, MIN_SAMPLES = 4;
    private static final double MIN_RATE = 0.68, MIN_GAIN = 0.12;

    private AdaptiveLearningPolicy() {}

    public static String recordFeedback(String profile, String task, int mode, boolean helpful) {
        if (mode < 0 || mode >= MODES) return normalizeProfile(profile);
        int[] counts = decode(profile);
        int offset = (category(task) * MODES + mode) * 2 + (helpful ? 0 : 1);
        int total = counts[offset] + counts[offset + 1];
        if (total >= MAX_COUNT) {
            counts[offset] = (counts[offset] + 1) / 2;
            counts[offset + 1] = (counts[offset + 1] + 1) / 2;
        }
        counts[offset]++;
        return encode(counts);
    }

    /** Keep the deterministic safety floor; only use an alternative with repeated positive evidence. */
    public static int recommendMode(String profile, String task, int baseline,
                                    boolean[] configured, boolean freeOnly, boolean[] markedFree) {
        int safeBaseline = clampMode(baseline);
        int[] counts = decode(profile);
        int category = category(task);
        int baselineOffset = (category * MODES + safeBaseline) * 2;
        double baselineRate = rate(counts, baselineOffset);
        double bestRate = baselineRate;
        int bestMode = safeBaseline;
        int minimumRank = Math.max(safetyRank(safeBaseline), isHighStakes(task) ? 1 : 0);

        for (int mode = 0; mode < MODES; mode++) {
            if (mode == safeBaseline || !available(mode, configured, freeOnly, markedFree)) continue;
            if (safetyRank(mode) < minimumRank) continue;
            int offset = (category * MODES + mode) * 2;
            int samples = counts[offset] + counts[offset + 1];
            if (samples < MIN_SAMPLES) continue;
            double candidateRate = rate(counts, offset);
            if (candidateRate > bestRate) {
                bestRate = candidateRate;
                bestMode = mode;
            }
        }
        return bestMode != safeBaseline && bestRate >= MIN_RATE
                && bestRate - baselineRate >= MIN_GAIN ? bestMode : safeBaseline;
    }

    public static int sampleCount(String profile, String task, int mode) {
        if (mode < 0 || mode >= MODES) return 0;
        int offset = (category(task) * MODES + mode) * 2;
        int[] counts = decode(profile);
        return counts[offset] + counts[offset + 1];
    }

    public static String normalizeProfile(String profile) {
        return encode(decode(profile));
    }

    public static int categoryForTask(String task) {
        return category(task);
    }

    private static double rate(int[] counts, int offset) {
        // Beta(2,2) prior prevents one click from dominating the router.
        return (counts[offset] + 2.0) / (counts[offset] + counts[offset + 1] + 4.0);
    }

    private static int category(String task) {
        String s = task == null ? "" : task.toLowerCase(Locale.ROOT);
        if (TaskRoutingPolicy.looksLikeMath(s)) return 0;
        if (TaskRoutingPolicy.looksLikeCodeTask(s)) return 1;
        if (has(s, "исслед", "обзор", "источник", "research", "literature", "latest", "обґрунтуй")) return 2;
        if (has(s, "выбрать", "сравни", "решение", "стратег", "decision", "compare", "trade-off", "варіант")) return 3;
        if (has(s, "напиши", "составь текст", "письмо", "статья", "переведи", "write", "draft", "rewrite", "caption")) return 4;
        return 5;
    }

    private static boolean isHighStakes(String task) {
        String s = task == null ? "" : task.toLowerCase(Locale.ROOT);
        return has(s, "медицин", "здоров", "диагноз", "лекарств", "дозиров", "юрид", "закон", "налог",
                "финанс", "инвест", "безопас", "критичн", "medical", "health", "diagnos", "medication",
                "dosage", "legal", "law ", "tax ", "financial", "invest", "safety", "security", "high stakes");
    }

    private static int safetyRank(int mode) {
        switch (mode) {
            case 0: return 0; // quick answer
            case 2: return 1; // independent audit
            case 1: return 2; // council + coordinator
            default: return 3; // deep analysis
        }
    }

    private static boolean available(int mode, boolean[] configured, boolean freeOnly, boolean[] markedFree) {
        int[] required = mode == 0 ? new int[]{0} : mode == 2 ? new int[]{0, 1} : new int[]{0, 1, 2, 3};
        for (int slot : required) {
            if (configured == null || slot >= configured.length || !configured[slot]) return false;
            if (freeOnly && (markedFree == null || slot >= markedFree.length || !markedFree[slot])) return false;
        }
        return true;
    }

    private static int[] decode(String profile) {
        int[] result = new int[CATEGORIES * MODES * 2];
        if (profile == null || !profile.startsWith(PREFIX)) return result;
        String[] values = profile.substring(PREFIX.length()).split(",", -1);
        if (values.length != result.length) return result;
        try {
            for (int i = 0; i < result.length; i++) {
                int value = Integer.parseInt(values[i]);
                if (value < 0 || value > MAX_COUNT) return new int[result.length];
                result[i] = value;
            }
            return result;
        } catch (NumberFormatException invalid) {
            return new int[result.length];
        }
    }

    private static String encode(int[] counts) {
        StringBuilder out = new StringBuilder(PREFIX);
        for (int i = 0; i < CATEGORIES * MODES * 2; i++) {
            if (i > 0) out.append(',');
            int value = counts != null && i < counts.length ? counts[i] : 0;
            out.append(Math.max(0, Math.min(MAX_COUNT, value)));
        }
        return out.toString();
    }

    private static int clampMode(int mode) {
        return Math.max(0, Math.min(MODES - 1, mode));
    }

    private static boolean has(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }
}
