package com.aicouncil.app;

/**
 * Best-effort pre-send guard for common credentials pasted into prompts.
 * It reuses the same masking rules as provider diagnostics so the app has one
 * consistent definition of common secret formats. It cannot identify every secret.
 */
public final class PromptPrivacyPolicy {
    private static final String MASK = "[секрет скрыт]";
    private static final String CONFIGURED_MASK = "[API-ключ скрыт]";

    private PromptPrivacyPolicy() { }

    /** Return a copy suitable for a user-approved redacted send. */
    public static String redact(String prompt) {
        return redact(prompt, (String[]) null);
    }

    /** Also masks currently configured secrets, including provider keys with nonstandard prefixes. */
    public static String redact(String prompt, String... configuredSecrets) {
        if (prompt == null || prompt.isEmpty()) return "";
        String value = prompt;
        if (configuredSecrets != null) {
            for (String secret : configuredSecrets) {
                if (secret != null && secret.trim().length() >= 8) {
                    value = value.replace(secret, "[API-ключ скрыт]");
                }
            }
        }
        return SecretRedactor.redact(value, "");
    }

    /** Number of new mask markers introduced by the redactor, not pre-existing markers. */
    public static int countRedactions(String prompt, String... configuredSecrets) {
        if (prompt == null || prompt.isEmpty()) return 0;
        String redacted = redact(prompt, configuredSecrets);
        int added = countMarkers(redacted) - countMarkers(prompt);
        return Math.max(0, added);
    }

    public static boolean hasLikelySecrets(String prompt, String... configuredSecrets) {
        return countRedactions(prompt, configuredSecrets) > 0;
    }

    private static int countMarkers(String value) {
        if (value == null || value.isEmpty()) return 0;
        int count = 0;
        count += occurrences(value, MASK);
        count += occurrences(value, CONFIGURED_MASK);
        return count;
    }

    private static int occurrences(String text, String marker) {
        int count = 0;
        int from = 0;
        while (true) {
            int found = text.indexOf(marker, from);
            if (found < 0) return count;
            count++;
            from = found + marker.length();
        }
    }
}
