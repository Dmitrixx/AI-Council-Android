package com.aicouncil.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Pure policy for safely adding system speech-recognition text to a task draft. */
public final class VoicePromptPolicy {
    private VoicePromptPolicy() {}

    public static String normalize(String recognized) {
        if (recognized == null) return "";
        return recognized.replace("\u0000", "").trim();
    }

    public static String append(String current, String recognized) {
        String existing = current == null ? "" : current;
        String incoming = normalize(recognized);
        if (incoming.isEmpty()) return existing;
        if (existing.trim().isEmpty()) return incoming;
        return existing + (existing.endsWith("\n") ? "" : "\n") + incoming;
    }

    /** Keeps only non-empty, normalized speech alternatives, preserving recognizer ranking. */
    public static ArrayList<String> uniqueCandidates(Iterable<String> candidates) {
        ArrayList<String> result = new ArrayList<String>();
        Set<String> seen = new HashSet<String>();
        if (candidates == null) return result;
        for (String candidate : candidates) {
            String normalized = normalize(candidate);
            if (normalized.isEmpty()) continue;
            String key = normalized.toLowerCase(Locale.ROOT);
            if (seen.add(key)) result.add(normalized);
            if (result.size() >= 5) break;
        }
        return result;
    }

    public static boolean fits(String current, String recognized, int maxChars) {
        if (maxChars < 0) return false;
        String merged = append(current, recognized);
        return merged.length() <= maxChars;
    }
}
