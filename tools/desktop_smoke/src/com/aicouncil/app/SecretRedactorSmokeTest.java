package com.aicouncil.app;

public final class SecretRedactorSmokeTest {
    private static int passed;

    private static void check(String name, boolean condition) {
        if (!condition) throw new AssertionError("FAIL: " + name);
        passed++;
        System.out.println("PASS: " + name);
    }

    public static void main(String[] args) {
        check("configured secret is redacted", !SecretRedactor.redact("Rejected my-secret-value", "my-secret-value").contains("my-secret-value"));
        check("OpenAI-style key is redacted", !SecretRedactor.redact("key=sk-live-1234567890abcdefghijkl", "").contains("sk-live-1234567890abcdefghijkl"));
        check("Google API key is redacted in query strings", !SecretRedactor.redact("https://api.test/?key=AIzaSyAbcdefghijklmnopqrstuvw0123456789", "").contains("AIzaSyAbcdefghijklmnopqrstuvw0123456789"));
        check("Groq token is redacted", !SecretRedactor.redact("gsk_0123456789abcdefghijklmnop", "").contains("gsk_0123456789abcdefghijklmnop"));
        check("xAI token is redacted", !SecretRedactor.redact("xai-0123456789abcdefghijklmnop", "").contains("xai-0123456789abcdefghijklmnop"));
        check("credential assignments are redacted", !SecretRedactor.redact("{\"client_secret\":\"client-secret-123456\"}", "").contains("client-secret-123456"));
        check("Bearer authorization value and token are redacted", !SecretRedactor.redact("Authorization: Bearer abcdef.ghijklmnop-123456", "").contains("abcdef.ghijklmnop-123456"));
        check("private key blocks are replaced", !SecretRedactor.redact("-----BEGIN PRIVATE KEY-----\nSUPER_SECRET_MATERIAL\n-----END PRIVATE KEY-----", "").contains("SUPER_SECRET_MATERIAL"));
        check("normal provider diagnostics are preserved", SecretRedactor.redact("invalid temperature; request exceeded the token limit", "").contains("invalid temperature; request exceeded the token limit"));
        check("null and empty inputs are safe", SecretRedactor.redact(null, "").isEmpty() && SecretRedactor.redact("", "").isEmpty());
        System.out.println("SecretRedactorSmokeTest: " + passed + "/10 passed");
    }
}
