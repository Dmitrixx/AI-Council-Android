package com.aicouncil.app;

public final class ProviderErrorFormatterSmokeTest {
    private static int passed;

    private static void check(String name, boolean condition) {
        if (!condition) throw new AssertionError("FAIL: " + name);
        passed++;
        System.out.println("PASS: " + name);
    }

    public static void main(String[] args) {
        check("401 explains API-key/authentication issue",
                ProviderErrorFormatter.formatHttpError(401, "invalid api key", "", "secret-123").contains("авторизация"));
        check("403 explains permission/access restrictions",
                ProviderErrorFormatter.formatHttpError(403, "denied", "", "").contains("Доступ запрещён"));
        check("404 mentions URL/model and configured model id",
                ProviderErrorFormatter.formatHttpError(404, "not found", "model-test", "").contains("model-test"));
        check("429 explains rate limit or quota",
                ProviderErrorFormatter.formatHttpError(429, "rate limit", "", "").contains("квоты"));
        check("5xx errors give provider-side guidance",
                ProviderErrorFormatter.formatHttpError(503, "unavailable", "", "").contains("серверная ошибка"));
        check("provider response details are preserved",
                ProviderErrorFormatter.formatHttpError(400, "invalid temperature", "", "").contains("invalid temperature"));
        check("configured API key is redacted from response details",
                !ProviderErrorFormatter.formatHttpError(401, "rejected secret-123", "", "secret-123").contains("rejected secret-123")
                        && ProviderErrorFormatter.formatHttpError(401, "rejected secret-123", "", "secret-123").contains("[API-ключ скрыт]"));
        check("Bearer token in error details is redacted",
                !ProviderErrorFormatter.formatHttpError(400, "Bearer abc.def_token-123", "", "").contains("abc.def_token-123"));
        check("null/empty response bodies are handled",
                !ProviderErrorFormatter.formatHttpError(500, null, "", "").contains("Детали провайдера")
                        && !ProviderErrorFormatter.formatHttpError(500, "  ", "", "").contains("Детали провайдера"));
        String longBody = new String(new char[1600]).replace("\0", "x");
        check("large error details are capped",
                ProviderErrorFormatter.formatHttpError(500, longBody, "", "").length() < 1500);
        check("504 has dedicated timeout guidance",
                ProviderErrorFormatter.formatHttpError(504, "", "", "").contains("Шлюз провайдера"));
        check("provider-style OpenAI keys are redacted", !ProviderErrorFormatter.formatHttpError(400, "key sk-proj-0123456789abcdefghijkl", "", "").contains("sk-proj-0123456789abcdefghijkl"));
        check("provider diagnostic URL keys are redacted", !ProviderErrorFormatter.formatHttpError(400, "GET https://api.test/?key=AIzaSyAbcdefghijklmnopqrstuvw0123456789", "", "").contains("AIzaSyAbcdefghijklmnopqrstuvw0123456789"));
        check("credential fields in JSON errors are redacted", !ProviderErrorFormatter.formatHttpError(500, "{\"access_token\":\"access-1234567890-token\"}", "", "").contains("access-1234567890-token"));
        check("authorization header token is redacted", !ProviderErrorFormatter.formatHttpError(400, "Authorization: Bearer xyz.abcdef0123456789", "", "").contains("xyz.abcdef0123456789"));
        check("diagnostic messages unrelated to secrets remain readable", ProviderErrorFormatter.formatHttpError(400, "invalid_temperature: value is outside range", "", "").contains("invalid_temperature: value is outside range"));
        check("private key blocks never appear in provider details", !ProviderErrorFormatter.formatHttpError(500, "-----BEGIN PRIVATE KEY-----\nPRIVATE_MATERIAL_123\n-----END PRIVATE KEY-----", "", "").contains("PRIVATE_MATERIAL_123"));
        System.out.println("ProviderErrorFormatterSmokeTest: " + passed + "/17 passed");
    }
}
