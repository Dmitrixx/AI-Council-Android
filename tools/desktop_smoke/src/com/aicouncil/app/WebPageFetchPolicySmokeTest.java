package com.aicouncil.app;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;

public final class WebPageFetchPolicySmokeTest {
    private static int passed;
    private static void check(String name, boolean actual, boolean expected) {
        if (actual != expected) throw new AssertionError(name + ": expected " + expected + " got " + actual);
        System.out.println("PASS: " + name); passed++;
    }
    private static InetAddress ip(String value) throws Exception { return InetAddress.getByName(value); }
    public static void main(String[] args) throws Exception {
        check("public HTTPS URL accepted", WebPageFetchPolicy.isAllowedUrl("https://example.com/article"), true);
        check("public HTTPS URL with query accepted", WebPageFetchPolicy.isAllowedUrl("https://example.com/search?q=test"), true);
        check("plain HTTP rejected", WebPageFetchPolicy.isAllowedUrl("http://example.com/article"), false);
        check("credentials in URL rejected", WebPageFetchPolicy.isAllowedUrl("https://user:pass@example.com/"), false);
        check("non-default TLS port rejected", WebPageFetchPolicy.isAllowedUrl("https://example.com:8443/"), false);
        check("URL fragment rejected", WebPageFetchPolicy.isAllowedUrl("https://example.com/article#section"), false);
        check("localhost rejected", WebPageFetchPolicy.isAllowedUrl("https://localhost/article"), false);
        check("local suffix rejected", WebPageFetchPolicy.isAllowedUrl("https://router.local/article"), false);
        check("IPv4 loopback literal rejected", WebPageFetchPolicy.isAllowedUrl("https://127.0.0.1/"), false);
        check("private IPv4 literal rejected", WebPageFetchPolicy.isAllowedUrl("https://192.168.1.1/"), false);
        check("redirect only allowed inside redirect budget", WebPageFetchPolicy.mayFollowRedirect("https://example.com/next", 1, 2), true);
        check("redirect rejected when budget exhausted", WebPageFetchPolicy.mayFollowRedirect("https://example.com/next", 2, 2), false);
        check("redirect to private host rejected", WebPageFetchPolicy.mayFollowRedirect("https://127.0.0.1/", 0, 2), false);
        check("redirect to non-HTTPS rejected", WebPageFetchPolicy.mayFollowRedirect("http://example.com/", 0, 2), false);
        check("empty DNS answers rejected", WebPageFetchPolicy.areResolvedAddressesPublic(Collections.<InetAddress>emptyList()), false);
        check("public IPv4 DNS answer accepted", WebPageFetchPolicy.areResolvedAddressesPublic(Arrays.asList(ip("93.184.216.34"))), true);
        check("loopback DNS answer rejected", WebPageFetchPolicy.areResolvedAddressesPublic(Arrays.asList(ip("127.0.0.1"))), false);
        check("mixed public and private DNS answers rejected", WebPageFetchPolicy.areResolvedAddressesPublic(Arrays.asList(ip("93.184.216.34"), ip("10.0.0.5"))), false);
        check("IPv4 special-purpose 192.0.0.0/24 blocked", WebPageFetchPolicy.areResolvedAddressesPublic(Arrays.asList(ip("192.0.0.8"))), false);
        check("IPv4 documentation block rejected", WebPageFetchPolicy.areResolvedAddressesPublic(Arrays.asList(ip("203.0.113.12"))), false);
        check("IPv6 ULA DNS answer rejected", WebPageFetchPolicy.areResolvedAddressesPublic(Arrays.asList(ip("fd00::1"))), false);
        check("IPv6 documentation range rejected", WebPageFetchPolicy.areResolvedAddressesPublic(Arrays.asList(ip("2001:db8::1"))), false);
        check("IPv6 global unicast accepted", WebPageFetchPolicy.areResolvedAddressesPublic(Arrays.asList(ip("2606:4700:4700::1111"))), true);
        System.out.println("Web page fetch policy smoke tests passed: " + passed + "/23");
    }
}
