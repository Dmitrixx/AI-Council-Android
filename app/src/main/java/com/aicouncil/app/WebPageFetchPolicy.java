package com.aicouncil.app;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/** SSRF guard for public web-page previews. DNS is checked again at connection time. */
final class WebPageFetchPolicy {
    private WebPageFetchPolicy() {}

    static boolean isAllowedUrl(String value) {
        if (!WebSearchPolicy.isPotentiallyPublicHttpsUrl(value)) return false;
        try {
            URL url = new URL(value);
            if (url.getPort() != -1 && url.getPort() != 443) return false;
            if (url.getRef() != null || url.getHost().length() > 253) return false;
            String host = url.getHost().toLowerCase(Locale.ROOT);
            if (host.isEmpty() || host.endsWith(".")) return false;
            // Avoid URL parser ambiguities and unsupported schemes/credentials at the request boundary.
            return "https".equalsIgnoreCase(url.getProtocol()) && url.getUserInfo() == null;
        } catch (Exception invalid) { return false; }
    }

    static boolean areResolvedAddressesPublic(List<InetAddress> addresses) {
        if (addresses == null || addresses.isEmpty()) return false;
        for (InetAddress address : addresses) if (!isPublicAddress(address)) return false;
        return true;
    }

    static boolean isPublicAddress(InetAddress address) {
        if (address == null || address.isAnyLocalAddress() || address.isLoopbackAddress()
                || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                || address.isMulticastAddress()) return false;
        byte[] bytes = address.getAddress();
        if (address instanceof Inet4Address || bytes.length == 4) {
            String ip = address.getHostAddress().replaceFirst("%.*$", "");
            if (WebSearchPolicy.isNonPublicIpv4Literal(ip)) return false;
            int[] n = ipv4(bytes);
            int a=n[0], b=n[1], c=n[2];
            // Additional special-purpose IPv4 blocks not covered by the generic helper.
            if (a == 192 && b == 0 && c == 0) return false;
            if (a == 192 && b == 88 && c == 99) return false;
            return true;
        }
        if (address instanceof Inet6Address || bytes.length == 16) {
            // Only global-unicast 2000::/3 is accepted; this excludes ULA, link-local,
            // multicast, unspecified, loopback, IPv4-compatible and most special-use ranges.
            if ((bytes[0] & 0xE0) != 0x20) return false;
            // Documentation prefix 2001:db8::/32 is never a public destination.
            if ((bytes[0] & 0xff) == 0x20 && (bytes[1] & 0xff) == 0x01
                    && (bytes[2] & 0xff) == 0x0d && (bytes[3] & 0xff) == 0xb8) return false;
            return true;
        }
        return false;
    }

    private static int[] ipv4(byte[] bytes) {
        return new int[] { bytes[0] & 0xff, bytes[1] & 0xff, bytes[2] & 0xff, bytes[3] & 0xff };
    }

    static boolean mayFollowRedirect(String nextAddress, int redirectsAlreadyFollowed, int maximumRedirects) {
        return redirectsAlreadyFollowed < maximumRedirects && isAllowedUrl(nextAddress);
    }
}
