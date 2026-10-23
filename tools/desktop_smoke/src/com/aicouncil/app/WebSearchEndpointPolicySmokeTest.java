package com.aicouncil.app;

public final class WebSearchEndpointPolicySmokeTest {
    public static void main(String[] args) {
        check("Brave search endpoint accepted", WebSearchEndpointPolicy.isAllowedGetEndpoint("https://api.search.brave.com/res/v1/web/search?q=ukraine"));
        check("Wikipedia API endpoint accepted", WebSearchEndpointPolicy.isAllowedGetEndpoint("https://uk.wikipedia.org/w/api.php?action=query&format=json"));
        check("DuckDuckGo JSON endpoint accepted", WebSearchEndpointPolicy.isAllowedGetEndpoint("https://api.duckduckgo.com/?q=weather&format=json"));
        check("Tavily fixed POST endpoint accepted", WebSearchEndpointPolicy.isAllowedPostJsonEndpoint("https://api.tavily.com/search"));
        check("HTTP search endpoint rejected", !WebSearchEndpointPolicy.isAllowedGetEndpoint("http://api.search.brave.com/res/v1/web/search?q=test"));
        check("Unlisted GET host rejected", !WebSearchEndpointPolicy.isAllowedGetEndpoint("https://example.com/?q=secret"));
        check("Wrong Brave path rejected", !WebSearchEndpointPolicy.isAllowedGetEndpoint("https://api.search.brave.com/other?q=test"));
        check("Non-default API port rejected", !WebSearchEndpointPolicy.isAllowedGetEndpoint("https://api.search.brave.com:8443/res/v1/web/search?q=test")
                && !WebSearchEndpointPolicy.isAllowedPostJsonEndpoint("https://api.tavily.com:8443/search"));
        check("Brave user-info rejected", !WebSearchEndpointPolicy.isAllowedGetEndpoint("https://user:pass@api.search.brave.com/res/v1/web/search?q=test"));
        check("Unexpected Tavily path rejected", !WebSearchEndpointPolicy.isAllowedPostJsonEndpoint("https://api.tavily.com/admin"));
        check("Tavily query override rejected", !WebSearchEndpointPolicy.isAllowedPostJsonEndpoint("https://api.tavily.com/search?redirect=https://evil.example"));
        check("Brave token can be sent to Brave only", WebSearchEndpointPolicy.maySendHeader("X-Subscription-Token", "token", "https://api.search.brave.com/res/v1/web/search?q=test")
                && !WebSearchEndpointPolicy.maySendHeader("X-Subscription-Token", "token", "https://uk.wikipedia.org/w/api.php?action=query"));
        check("Wikipedia user-agent is scoped to Wikipedia", WebSearchEndpointPolicy.maySendHeader("Api-User-Agent", "AI Council", "https://uk.wikipedia.org/w/api.php?action=query")
                && !WebSearchEndpointPolicy.maySendHeader("Api-User-Agent", "AI Council", "https://api.duckduckgo.com/?q=test"));
        check("Tavily bearer key is scoped to its exact endpoint", WebSearchEndpointPolicy.maySendHeader("Authorization", "Bearer tvly-1234567890", "https://api.tavily.com/search")
                && !WebSearchEndpointPolicy.maySendHeader("Authorization", "Bearer tvly-1234567890", "https://api.search.brave.com/res/v1/web/search?q=test")
                && !WebSearchEndpointPolicy.maySendHeader("Authorization", "Bearer secret with spaces", "https://api.tavily.com/search"));
        check("Unknown headers are rejected", !WebSearchEndpointPolicy.maySendHeader("X-Api-Key", "secret", "https://api.tavily.com/search"));
        check("Response size bounds enforced", WebSearchEndpointPolicy.isResponseLimitAllowed(1)
                && WebSearchEndpointPolicy.isResponseLimitAllowed(700_000)
                && !WebSearchEndpointPolicy.isResponseLimitAllowed(0)
                && !WebSearchEndpointPolicy.isResponseLimitAllowed(700_001));
        System.out.println("Web search endpoint policy smoke tests passed: 16/16");
    }

    private static void check(String label, boolean condition) {
        if (!condition) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
}
