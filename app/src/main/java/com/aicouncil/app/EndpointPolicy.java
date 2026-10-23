package com.aicouncil.app;

import java.net.URL;
import java.util.Locale;

/** Pure-Java endpoint policy shared by the Android UI and offline smoke tests. */
final class EndpointPolicy {
    private EndpointPolicy() {}

    static boolean isPrivateLocalHost(String host) {
        if (host == null) return false;
        String h = host.toLowerCase(Locale.ROOT);
        if ("localhost".equals(h) || h.endsWith(".localhost") || h.endsWith(".local")) return true;
        if (h.startsWith("[") && h.endsWith("]")) h = h.substring(1, h.length() - 1);
        if (h.indexOf(':') >= 0) {
            try {
                java.net.InetAddress address = java.net.InetAddress.getByName(h);
                byte[] bytes = address.getAddress();
                return address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress() || (bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc);
            } catch (Exception ignored) { return false; }
        }
        String[] parts = h.split("\\.");
        if (parts.length != 4) return false;
        int[] octets = new int[4];
        try {
            for (int i = 0; i < 4; i++) {
                if (parts[i].isEmpty() || !parts[i].matches("[0-9]{1,3}")) return false;
                octets[i] = Integer.parseInt(parts[i]);
                if (octets[i] < 0 || octets[i] > 255) return false;
            }
        } catch (NumberFormatException e) { return false; }
        return octets[0] == 10 || octets[0] == 127
            || (octets[0] == 192 && octets[1] == 168)
            || (octets[0] == 172 && octets[1] >= 16 && octets[1] <= 31);
    }

    static boolean isLocalEndpoint(String value) {
        try {
            URL parsed = new URL(value.trim());
            return ("http".equalsIgnoreCase(parsed.getProtocol()) || "https".equalsIgnoreCase(parsed.getProtocol()))
                && isPrivateLocalHost(parsed.getHost()) && parsed.getUserInfo() == null
                && parsed.getQuery() == null && parsed.getRef() == null;
        } catch (Exception e) { return false; }
    }

    static boolean isLocalUnauthenticatedEndpoint(String value) {
        try {
            URL parsed = new URL(value.trim());
            return "http".equalsIgnoreCase(parsed.getProtocol()) && isPrivateLocalHost(parsed.getHost())
                && parsed.getUserInfo() == null && parsed.getQuery() == null && parsed.getRef() == null;
        } catch (Exception e) { return false; }
    }

    static boolean isAllowedApiBase(String value) {
        try {
            String trimmed = value.trim();
            if (!trimmed.matches("(?i)^https?://[^/?#]+(?:/[^?#]*)?$")) return false;
            URL parsed = new URL(trimmed);
            if (parsed.getHost() == null || parsed.getHost().isEmpty() || parsed.getUserInfo() != null
                    || parsed.getQuery() != null || parsed.getRef() != null) return false;
            if ("https".equalsIgnoreCase(parsed.getProtocol())) return true;
            return isLocalUnauthenticatedEndpoint(trimmed);
        } catch (Exception e) { return false; }
    }
}
