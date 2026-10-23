package com.aicouncil.app;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public final class ProviderModelsClientSmokeTest {
    public static void main(String[] args) {
        ProviderModelsClient client = new ProviderModelsClient();
        int initial = OkHttpClient.callCount.get();

        ProviderModelsClient.Result publicHttp = client.fetch("http://api.example.com/v1", "cloud-secret-123", false, () -> false);
        check("public HTTP model catalogue is rejected before network", !publicHttp.error.isEmpty() && OkHttpClient.callCount.get() == initial);

        ProviderModelsClient.Result localOnly = client.fetch("https://api.example.com/v1", "cloud-secret-123", true, () -> false);
        check("local-only mode blocks cloud model catalogue before network", !localOnly.error.isEmpty() && OkHttpClient.callCount.get() == initial);

        ProviderModelsClient.Result local = client.fetch("http://192.168.1.20:11434/v1", "cloud-secret-123", false, () -> false);
        Request localRequest = OkHttpClient.lastRequest;
        check("local models endpoint uses private HTTP /models URL", localRequest != null && "http://192.168.1.20:11434/v1/models".equals(localRequest.url()));
        check("saved cloud key is not sent to a local endpoint", localRequest != null && localRequest.header("Authorization") == null);
        check("local endpoint request has bounded client error without secret", !local.error.contains("cloud-secret-123"));

        ProviderModelsClient.Result cloud = client.fetch("https://api.example.com/v1", "cloud-secret-123", false, () -> false);
        Request cloudRequest = OkHttpClient.lastRequest;
        check("cloud models endpoint uses HTTPS /models URL", cloudRequest != null && "https://api.example.com/v1/models".equals(cloudRequest.url()));
        check("cloud key is sent only to its configured HTTPS API", cloudRequest != null && "Bearer cloud-secret-123".equals(cloudRequest.header("Authorization")));
        check("cloud diagnostics do not leak the configured key", !cloud.error.contains("cloud-secret-123"));

        final int[] cancellationChecks = {0};
        ProviderModelsClient.Result cancelled = client.fetch("http://192.168.1.20:11434/v1", "", false,
                () -> ++cancellationChecks[0] > 1);
        check("cancellation after call registration is classified as user cancellation", cancelled.cancelled && cancelled.error.isEmpty());

        ProviderModelsClient.Result blockedPrivateSyntax = client.fetch("http://192.168.1.999:11434/v1", "", false, () -> false);
        check("malformed private IPv4 is rejected", !blockedPrivateSyntax.error.isEmpty());
        System.out.println("ProviderModelsClientSmokeTest: 9/9 passed");
    }

    private static void check(String label, boolean ok) {
        if (!ok) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
}
