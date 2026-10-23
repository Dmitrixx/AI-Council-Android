package com.aicouncil.app;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Creates a non-reversible fingerprint for the provider configuration last tested. */
public final class ProviderConfigFingerprint {
    private ProviderConfigFingerprint() {}

    public static String create(String endpoint, String model, String apiKey) {
        String e = endpoint == null ? "" : endpoint.trim();
        String m = model == null ? "" : model.trim();
        String k = apiKey == null ? "" : apiKey.trim();
        // Length-prefix each field to avoid delimiter ambiguity between values.
        String canonical = e.length() + ":" + e + m.length() + ":" + m + k.length() + ":" + k;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) hex.append(String.format(java.util.Locale.ROOT, "%02x", b & 0xff));
            return hex.toString();
        } catch (Exception impossibleOnAndroidOrJava) {
            throw new IllegalStateException("SHA-256 недоступен", impossibleOnAndroidOrJava);
        }
    }
}
