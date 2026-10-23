package com.aicouncil.app;

public final class LocalAiPolicySmokeTest {
    public static void main(String[] args) {
        check("Ollama models endpoint", "http://192.168.1.25:11434/v1/models".equals(LocalAiPolicy.modelsEndpoint("http://192.168.1.25:11434/v1/")));
        check("LM Studio models endpoint", "http://192.168.1.25:1234/v1/models".equals(LocalAiPolicy.modelsEndpoint("http://192.168.1.25:1234/v1")));
        check("Existing models endpoint not duplicated", "https://api.example.com/v1/models".equals(LocalAiPolicy.modelsEndpoint("https://api.example.com/v1/models")));
        check("Chat completions path converted to models", "https://api.example.com/v1/models".equals(LocalAiPolicy.modelsEndpoint("https://api.example.com/v1/chat/completions")));
        check("Malformed base cannot create models endpoint", LocalAiPolicy.modelsEndpoint("http://example.com/v1").isEmpty());
        check("Cloud endpoint denied in local-only mode", !LocalAiPolicy.mayUseEndpoint("https://api.example.com/v1", true));
        check("LAN endpoint accepted in local-only mode", LocalAiPolicy.mayUseEndpoint("http://192.168.1.25:11434/v1", true));
        check("Local-only mode can keep explicitly enabled web search", LocalAiPolicy.shouldUseWebSearch(true, true));
        check("Local-only mode keeps web search off when user leaves it off", !LocalAiPolicy.shouldUseWebSearch(true, false));
        check("Internet search remains opt-in outside local-only", !LocalAiPolicy.shouldUseWebSearch(false, false));
        check("Explicit search can run outside local-only", LocalAiPolicy.shouldUseWebSearch(false, true));
        check("LAN endpoints never receive a saved key", !LocalAiPolicy.shouldSendAuthorization("https://modelbox.local/v1", "cloud-secret"));
        check("Private local HTTPS endpoint accepted in local-only mode", LocalAiPolicy.mayUseEndpoint("https://modelbox.local/v1", true));
        check("Cloud endpoint receives supplied key", LocalAiPolicy.shouldSendAuthorization("https://api.example.com/v1", "test-key"));
        check("Empty cloud key is never sent", !LocalAiPolicy.shouldSendAuthorization("https://api.example.com/v1", "  "));
        check("Privacy controls cannot change during active request", !LocalAiPolicy.mayChangePrivacyControls(true));
        check("Privacy controls can change while idle", LocalAiPolicy.mayChangePrivacyControls(false));
        System.out.println("Local AI policy smoke tests passed: 17/17");
    }

    private static void check(String label, boolean ok) {
        if (!ok) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
}
