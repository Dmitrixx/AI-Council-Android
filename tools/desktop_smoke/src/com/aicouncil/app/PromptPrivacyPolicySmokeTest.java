package com.aicouncil.app;

public final class PromptPrivacyPolicySmokeTest {
    private static int checks;
    private static void check(String name, boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
    }

    public static void main(String[] args) {
        check("null and empty prompts are safe", PromptPrivacyPolicy.redact(null).isEmpty()
                && PromptPrivacyPolicy.redact("").isEmpty()
                && PromptPrivacyPolicy.countRedactions(null) == 0);

        String openAi = "Сравни API, пример ключа: sk-ABCDEFGHIJKLMNOPQRST123456";
        String safeOpenAi = PromptPrivacyPolicy.redact(openAi);
        check("OpenAI-style token triggers guard and is masked", PromptPrivacyPolicy.hasLikelySecrets(openAi)
                && !safeOpenAi.contains("sk-ABCDEFGHIJKLMNOPQRST123456")
                && safeOpenAi.contains("[секрет скрыт]"));

        String bearer = "Authorization: Bearer abcdefghijklmnop-123456789";
        check("Bearer credentials are masked", !PromptPrivacyPolicy.redact(bearer).contains("abcdefghijklmnop-123456789"));

        String assigned = "client_secret=client-secret-value-123 password='very-secret-password'";
        String assignedSafe = PromptPrivacyPolicy.redact(assigned);
        check("credential assignments are masked", !assignedSafe.contains("client-secret-value-123")
                && !assignedSafe.contains("very-secret-password"));
        check("multiple secrets are counted", PromptPrivacyPolicy.countRedactions(assigned) >= 2);

        String query = "Open this example https://api.example.test/?api_key=abcdefghijk123456";
        check("URL query token is masked", !PromptPrivacyPolicy.redact(query).contains("abcdefghijk123456"));

        String pem = "-----BEGIN PRIVATE KEY-----\nSUPER_PRIVATE_MATERIAL\n-----END PRIVATE KEY-----";
        check("private key block is masked", !PromptPrivacyPolicy.redact(pem).contains("SUPER_PRIVATE_MATERIAL"));

        String ordinary = "Помоги спроектировать приложение и оценить количество токенов.";
        check("ordinary prompt stays unchanged", ordinary.equals(PromptPrivacyPolicy.redact(ordinary))
                && PromptPrivacyPolicy.countRedactions(ordinary) == 0);

        String alreadyMasked = "Используй значение [секрет скрыт] как placeholder";
        check("an already-masked placeholder does not trigger another warning", PromptPrivacyPolicy.countRedactions(alreadyMasked) == 0);

        String multiline = "Задача:\nПроанализируй пример token=123456789abcdef\nСохрани порядок действий.";
        String safeMultiline = PromptPrivacyPolicy.redact(multiline);
        check("redaction preserves surrounding instructions and line breaks", safeMultiline.startsWith("Задача:\n")
                && safeMultiline.contains("\nСохрани порядок действий.") && !safeMultiline.contains("123456789abcdef"));

        String customCredential = "Include this sample key in a comparison: custom-provider-secret-987654";
        String customSafe = PromptPrivacyPolicy.redact(customCredential, "custom-provider-secret-987654");
        check("configured nonstandard API key is masked", !customSafe.contains("custom-provider-secret-987654")
                && customSafe.contains("[API-ключ скрыт]"));
        check("configured keys count toward the warning", PromptPrivacyPolicy.countRedactions(customCredential,
                "custom-provider-secret-987654") == 1);
        check("short values are not treated as configured secrets", "a is an ordinary letter".equals(
                PromptPrivacyPolicy.redact("a is an ordinary letter", "a")));
        check("redaction is stable when applied twice", PromptPrivacyPolicy.redact(safeOpenAi).equals(safeOpenAi));
        System.out.println("PromptPrivacyPolicySmokeTest: " + checks + "/" + checks + " passed");
    }
}
