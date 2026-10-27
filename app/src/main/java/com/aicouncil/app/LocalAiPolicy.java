package com.aicouncil.app;

/** Shared local-AI routing rules; intentionally independent of Android UI/network classes. */
final class LocalAiPolicy {
    private LocalAiPolicy() {}

    static boolean mayUseEndpoint(String baseUrl, boolean localOnly) {
        return EndpointPolicy.isAllowedApiBase(baseUrl) && (!localOnly || EndpointPolicy.isLocalEndpoint(baseUrl));
    }

    /** Strict privacy boundary: local-only means the task must not call external search providers. */
    static boolean shouldUseWebSearch(boolean localOnly, boolean webSearchRequested) {
        return webSearchRequested && !localOnly;
    }

    static boolean mayChangePrivacyControls(boolean requestInProgress) {
        return !requestInProgress;
    }

    static boolean shouldSendAuthorization(String baseUrl, String apiKey) {
        return apiKey != null && !apiKey.trim().isEmpty() && !EndpointPolicy.isLocalEndpoint(baseUrl);
    }

    static String modelsEndpoint(String baseUrl) {
        if (baseUrl == null) return "";
        String base = baseUrl.trim().replaceAll("/+$", "");
        if (!EndpointPolicy.isAllowedApiBase(base)) return "";
        if (base.endsWith("/chat/completions")) return base.substring(0, base.length() - "/chat/completions".length()) + "/models";
        if (base.endsWith("/models")) return base;
        return base + "/models";
    }
}
