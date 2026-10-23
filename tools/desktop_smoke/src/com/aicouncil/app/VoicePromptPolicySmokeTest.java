package com.aicouncil.app;

import java.util.Arrays;
import java.util.ArrayList;

public final class VoicePromptPolicySmokeTest {
    private static int checks;
    private static void check(String name, boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
    }
    public static void main(String[] args) {
        check("null recognition normalizes to empty", VoicePromptPolicy.normalize(null).isEmpty());
        check("blank recognition normalizes to empty", VoicePromptPolicy.normalize("  \n ").isEmpty());
        check("NUL characters are removed", "тест".equals(VoicePromptPolicy.normalize("\u0000тест\u0000")));
        check("recognized text starts an empty draft", "привет мир".equals(VoicePromptPolicy.append("", " привет мир ")));
        check("recognized text appends on a new line", "моя задача\nдетали".equals(VoicePromptPolicy.append("моя задача", "детали")));
        check("existing newline is not duplicated", "моя задача\nдетали".equals(VoicePromptPolicy.append("моя задача\n", "детали")));
        check("empty recognition leaves the draft unchanged", "задача".equals(VoicePromptPolicy.append("задача", "   ")));
        check("fits exact boundary", VoicePromptPolicy.fits("abc", "de", 6));
        check("rejects content exceeding boundary", !VoicePromptPolicy.fits("abc", "de", 4));
        check("negative capacity is rejected", !VoicePromptPolicy.fits("", "x", -1));
        ArrayList<String> candidates = VoicePromptPolicy.uniqueCandidates(Arrays.asList(" Привет мир ", "привет мир", "Другой текст", "", null));
        check("voice alternatives remove duplicates case-insensitively", candidates.size() == 2);
        check("voice alternatives preserve recognizer ranking", "Привет мир".equals(candidates.get(0)));
        check("null candidate list is safe", VoicePromptPolicy.uniqueCandidates(null).isEmpty());
        check("voice alternatives are capped", VoicePromptPolicy.uniqueCandidates(Arrays.asList("a", "b", "c", "d", "e", "f")).size() == 5);
        System.out.println("VoicePromptPolicySmokeTest: " + checks + "/" + checks + " passed");
    }
}
