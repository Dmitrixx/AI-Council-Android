package com.aicouncil.app;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Bounded OkHttp transport for the app's fixed web-search API endpoints.
 * It deliberately does not accept arbitrary page URLs; page preview has a separate SSRF guard.
 */
public final class WebSearchClient {
    public interface Cancellation { boolean isCancelled(); }

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int DEFAULT_MAX_RESPONSE_BYTES = 700_000;
    private final OkHttpClient http;
    private final Set<Call> activeCalls = ConcurrentHashMap.newKeySet();

    public WebSearchClient() {
        http = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(7, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .callTimeout(9, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .build();
    }

    public String get(String address, Map<String, String> headers, int maxBytes, Cancellation cancellation) {
        if (!WebSearchEndpointPolicy.isAllowedGetEndpoint(address) || maxBytes < 1
                || maxBytes > DEFAULT_MAX_RESPONSE_BYTES || !WebSearchEndpointPolicy.isResponseLimitAllowed(maxBytes)) return "";
        if (isCancelled(cancellation)) return "";
        Request.Builder builder = new Request.Builder().url(address).get()
                .header("Accept", "application/json")
                .header("Accept-Encoding", "identity")
                .header("User-Agent", "AI-Council-Android/2.0 (web search)");
        addSafeHeaders(builder, headers, address);
        return execute(builder.build(), maxBytes, cancellation);
    }

    public String postJson(String address, String jsonBody, Map<String, String> headers, int maxBytes, Cancellation cancellation) {
        // Tavily accepts POST requests only at its fixed API host/path. Never forward arbitrary credentials.
        if (!WebSearchEndpointPolicy.isAllowedPostJsonEndpoint(address) || jsonBody == null || jsonBody.length() > 30_000
                || maxBytes < 1 || maxBytes > DEFAULT_MAX_RESPONSE_BYTES
                || !WebSearchEndpointPolicy.isResponseLimitAllowed(maxBytes)) return "";
        if (isCancelled(cancellation)) return "";
        Request.Builder builder = new Request.Builder().url(address)
                .post(RequestBody.create(jsonBody, JSON))
                .header("Accept", "application/json")
                .header("Accept-Encoding", "identity")
                .header("User-Agent", "AI-Council-Android/2.0 (web search)");
        addSafeHeaders(builder, headers, address);
        return execute(builder.build(), maxBytes, cancellation);
    }

    public void cancelAll() {
        for (Call call : activeCalls) {
            try { call.cancel(); } catch (Exception ignored) { }
        }
    }

    private String execute(Request request, int maxBytes, Cancellation cancellation) {
        if (isCancelled(cancellation)) return "";
        Call call = http.newCall(request);
        activeCalls.add(call);
        // Re-check after registration to close the race with cancelAll().
        if (isCancelled(cancellation)) call.cancel();
        try (Response response = call.execute()) {
            if (!response.isSuccessful() || response.body() == null) return "";
            return readBounded(response.body(), maxBytes);
        } catch (IOException ignored) {
            return "";
        } finally {
            activeCalls.remove(call);
        }
    }

    private static boolean isCancelled(Cancellation cancellation) {
        return Thread.currentThread().isInterrupted() || cancellation != null && cancellation.isCancelled();
    }

    private static String readBounded(ResponseBody body, int maxBytes) throws IOException {
        try (java.io.InputStream input = body.byteStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int total = 0;
            int count;
            while ((count = input.read(buffer, 0, Math.min(buffer.length, maxBytes + 1 - total))) != -1) {
                total += count;
                if (total > maxBytes) return "";
                output.write(buffer, 0, count);
                if (total == maxBytes) {
                    // Read one additional byte on the next iteration to distinguish exact limit from truncation.
                    continue;
                }
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static void addSafeHeaders(Request.Builder builder, Map<String, String> headers, String address) {
        if (headers == null) return;
        for (Map.Entry<String, String> item : headers.entrySet()) {
            String name = item.getKey();
            String value = item.getValue();
            if (!WebSearchEndpointPolicy.maySendHeader(name, value, address)) continue;
            try { builder.header(name, value); } catch (IllegalArgumentException ignored) { }
        }
    }
}
