package com.aicouncil.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Redacts common credentials from provider error text before it is shown to the user.
 * This is a best-effort display filter, not a guarantee that arbitrary text contains no secrets.
 */
public final class SecretRedactor {
    private static final String MASK = "[секрет скрыт]";
    private static final Pattern BEARER = Pattern.compile("(?i)(\\bBearer\\s+)[A-Za-z0-9._~+/\\-=]{6,}");
    private static final Pattern LABELED_SECRET = Pattern.compile(
            "(?i)([\\\"']?(?:api[_-]?key|x-api-key|authorization|access[_-]?token|refresh[_-]?token|client[_-]?secret|password|passwd|secret|token)[\\\"']?\\s*[:=]\\s*[\\\"']?)([^\\\"'\\s,;&}\\]]{6,})([\\\"']?)");
    private static final Pattern QUERY_KEY = Pattern.compile(
            "(?i)([?&](?:key|api[_-]?key|x-api-key|token|access_token|secret)=)([^&#\\s]{6,})");
    private static final Pattern OPENAI_STYLE_KEY = Pattern.compile("\\bsk-[A-Za-z0-9_-]{16,}\\b");
    private static final Pattern GOOGLE_API_KEY = Pattern.compile("\\bAIza[0-9A-Za-z_-]{20,}\\b");
    private static final Pattern GROQ_API_KEY = Pattern.compile("\\bgsk_[A-Za-z0-9_-]{16,}\\b");
    private static final Pattern XAI_API_KEY = Pattern.compile("\\bxai-[A-Za-z0-9_-]{16,}\\b");
    private static final Pattern HUGGINGFACE_TOKEN = Pattern.compile("\\bhf_[A-Za-z0-9_-]{16,}\\b");
    private static final Pattern GITHUB_TOKEN = Pattern.compile("\\bgh[pousr]_[A-Za-z0-9]{20,}\\b");
    private static final Pattern AWS_ACCESS_KEY = Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b");
    private static final Pattern PRIVATE_KEY_BLOCK = Pattern.compile(
            "-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----[\\s\\S]*?-----END [A-Z0-9 ]*PRIVATE KEY-----");
    private static final Pattern CONTROL = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");

    private SecretRedactor() { }

    /**
     * Masks a known configured secret first, then common token formats and credential fields.
     * Null input is safely treated as an empty string.
     */
    public static String redact(String text, String configuredSecret) {
        if (text == null || text.isEmpty()) return "";
        String value = text;
        if (configuredSecret != null && !configuredSecret.isEmpty()) {
            value = value.replace(configuredSecret, "[API-ключ скрыт]");
        }
        value = PRIVATE_KEY_BLOCK.matcher(value).replaceAll(MASK);
        value = BEARER.matcher(value).replaceAll("$1" + MASK);
        value = LABELED_SECRET.matcher(value).replaceAll("$1" + MASK + "$3");
        value = QUERY_KEY.matcher(value).replaceAll("$1" + MASK);
        value = replaceMatches(OPENAI_STYLE_KEY, value);
        value = replaceMatches(GOOGLE_API_KEY, value);
        value = replaceMatches(GROQ_API_KEY, value);
        value = replaceMatches(XAI_API_KEY, value);
        value = replaceMatches(HUGGINGFACE_TOKEN, value);
        value = replaceMatches(GITHUB_TOKEN, value);
        value = replaceMatches(AWS_ACCESS_KEY, value);
        value = CONTROL.matcher(value).replaceAll("");
        return value;
    }

    private static String replaceMatches(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll(MASK);
    }
}
