package com.aicouncil.app;

public final class EndpointPolicySmokeTest {
    public static void main(String[] args) {
        check("HTTPS cloud URL accepted", EndpointPolicy.isAllowedApiBase("https://api.example.com/v1"));
        check("private IPv4 Ollama HTTP accepted", EndpointPolicy.isAllowedApiBase("http://192.168.1.25:11434/v1"));
        check("10/8 private host accepted", EndpointPolicy.isAllowedApiBase("http://10.0.2.2:1234/v1"));
        check("172.16/12 private host accepted", EndpointPolicy.isAllowedApiBase("http://172.20.0.5:1234/v1"));
        check("localhost HTTP accepted", EndpointPolicy.isAllowedApiBase("http://localhost:1234/v1"));
        check("local endpoint identified for privacy mode", EndpointPolicy.isLocalEndpoint("https://modelbox.local:1234/v1"));
        check("cloud endpoint rejected by local mode", !EndpointPolicy.isLocalEndpoint("https://api.example.com/v1"));
        check("public HTTP rejected", !EndpointPolicy.isAllowedApiBase("http://example.com/v1"));
        check("userinfo rejected", !EndpointPolicy.isAllowedApiBase("https://user:pass@example.com/v1"));
        check("query string rejected", !EndpointPolicy.isAllowedApiBase("https://api.example.com/v1?token=x"));
        check("malformed private IPv4 rejected", !EndpointPolicy.isAllowedApiBase("http://192.168.999.2:11434/v1"));
        check("loopback alias cannot accept credentials", !EndpointPolicy.isAllowedApiBase("http://user@localhost:11434/v1"));
        System.out.println("Endpoint policy smoke tests passed: 12/12");
    }
    private static void check(String label, boolean ok) {
        if (!ok) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
}
