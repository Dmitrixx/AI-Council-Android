package com.aicouncil.app;

public final class ProviderConfigFingerprintSmokeTest {
    public static void main(String[] args) {
        String base = ProviderConfigFingerprint.create("https://api.example.com/v1", "model-a", "secret-key-123");
        check("same configuration has stable fingerprint", base.equals(ProviderConfigFingerprint.create(" https://api.example.com/v1 ", " model-a ", " secret-key-123 ")));
        check("endpoint change changes fingerprint", !base.equals(ProviderConfigFingerprint.create("https://other.example.com/v1", "model-a", "secret-key-123")));
        check("model change changes fingerprint", !base.equals(ProviderConfigFingerprint.create("https://api.example.com/v1", "model-b", "secret-key-123")));
        check("API key change changes fingerprint", !base.equals(ProviderConfigFingerprint.create("https://api.example.com/v1", "model-a", "different-key")));
        check("fingerprint does not expose raw API key", !base.contains("secret-key-123") && base.length() == 64);
        check("null configuration fields are supported", ProviderConfigFingerprint.create(null, null, null).equals(ProviderConfigFingerprint.create("", "", "")));
        check("length-prefixed fields avoid simple delimiter collision", !ProviderConfigFingerprint.create("ab", "c", "").equals(ProviderConfigFingerprint.create("a", "bc", "")));
        check("catalog fingerprint is scoped to normalized endpoint and effective API key", ProviderConfigFingerprint.create("https://api.example.com/v1", "", "token-a").equals(ProviderConfigFingerprint.create(" https://api.example.com/v1 ", "", "token-a")) && !ProviderConfigFingerprint.create("https://api.example.com/v1", "", "token-a").equals(ProviderConfigFingerprint.create("https://other.example.com/v1", "", "token-a")) && !ProviderConfigFingerprint.create("https://api.example.com/v1", "", "token-a").equals(ProviderConfigFingerprint.create("https://api.example.com/v1", "", "token-b")));
        System.out.println("Provider config fingerprint smoke tests passed: 8/8");
    }

    private static void check(String label, boolean ok) {
        if (!ok) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
}
