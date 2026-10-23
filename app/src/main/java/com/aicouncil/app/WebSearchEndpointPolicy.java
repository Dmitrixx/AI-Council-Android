package com.aicouncil.app;

import java.net.URL;
import java.util.Locale;

/** Pure-Java allowlist for outbound search API calls; intentionally separate from result-page fetching. */
final class WebSearchEndpointPolicy {
    private WebSearchEndpointPolicy() {}

    static boolean isAllowedGetEndpoint(String value) {
        try {
            if (value == null || value.length() > 4096) return false;
            URL url = new URL(value);
            if (!"https".equalsIgnoreCase(url.getProtocol()) || url.getHost() == null
                    || url.getUserInfo() != null || !isDefaultHttpsPort(url) || url.getQuery() == null || url.getRef() != null) return false;
            String host = url.getHost().toLowerCase(Locale.ROOT);
            String path = url.getPath();
            return isBraveEndpoint(value)
                    || (host.matches("[a-z]{2,3}\\.wikipedia\\.org") && "/w/api.php".equals(path))
                    || ("api.duckduckgo.com".equals(host) && "/".equals(path));
        } catch (Exception invalid) { return false; }
    }

    static boolean isAllowedPostJsonEndpoint(String value) {
        try {
            if (value == null || value.length() > 4096) return false;
            URL url = new URL(value);
            return "https".equalsIgnoreCase(url.getProtocol())
                    && "api.tavily.com".equalsIgnoreCase(url.getHost())
                    && "/search".equals(url.getPath()) && url.getUserInfo() == null
                    && isDefaultHttpsPort(url) && url.getQuery() == null && url.getRef() == null;
        } catch (Exception invalid) { return false; }
    }

    static boolean isBraveEndpoint(String value) {
        try {
            if (value == null || value.length() > 4096) return false;
            URL url = new URL(value);
            return "https".equalsIgnoreCase(url.getProtocol())
                    && "api.search.brave.com".equalsIgnoreCase(url.getHost())
                    && "/res/v1/web/search".equals(url.getPath())
                    && url.getUserInfo() == null && isDefaultHttpsPort(url) && url.getQuery() != null && url.getRef() == null;
        } catch (Exception invalid) { return false; }
    }

    static boolean maySendHeader(String name, String value, String address) {
        if (name == null || value == null || value.length() > 4096) return false;
        if ("X-Subscription-Token".equalsIgnoreCase(name)) return isBraveEndpoint(address);
        if ("Authorization".equalsIgnoreCase(name)) {
            return isAllowedPostJsonEndpoint(address)
                    && value.matches("Bearer [A-Za-z0-9._~+/=-]{8,4096}");
        }
        if ("Api-User-Agent".equalsIgnoreCase(name)) {
            try {
                URL url = new URL(address);
                return isAllowedGetEndpoint(address)
                        && url.getHost().toLowerCase(Locale.ROOT).matches("[a-z]{2,3}\\.wikipedia\\.org");
            } catch (Exception invalid) { return false; }
        }
        return false;
    }

    private static boolean isDefaultHttpsPort(URL url) {
        return url != null && (url.getPort() == -1 || url.getPort() == 443);
    }

    static boolean isResponseLimitAllowed(int maxBytes) {
        return maxBytes >= 1 && maxBytes <= 700_000;
    }
}
