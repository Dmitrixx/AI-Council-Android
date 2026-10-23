package com.aicouncil.app;

import okhttp3.Call;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** Bounded public HTML fetcher with DNS pinning, explicit redirect checks and cancellation. */
final class WebPageFetchClient {
    interface Cancellation { boolean isCancelled(); }
    static final class PageResponse {
        final String html;
        final String finalUrl;
        PageResponse(String html, String finalUrl) { this.html = html; this.finalUrl = finalUrl; }
        boolean isEmpty() { return html == null || html.isEmpty(); }
    }

    private static final int MAX_BYTES = 300_000;
    private static final int MAX_REDIRECTS = 2;
    private final Set<Call> activeCalls = ConcurrentHashMap.newKeySet();
    private final OkHttpClient http;

    WebPageFetchClient() {
        Dns safeDns = hostname -> {
            List<InetAddress> resolved = Dns.SYSTEM.lookup(hostname);
            if (!WebPageFetchPolicy.areResolvedAddressesPublic(resolved)) {
                throw new UnknownHostException("Destination is not a public unicast address");
            }
            // Return the validated records directly so OkHttp connects to these addresses,
            // instead of resolving the hostname again after the safety check (DNS rebinding).
            return new ArrayList<>(resolved);
        };
        http = new OkHttpClient.Builder()
                .dns(safeDns)
                .connectTimeout(3500, TimeUnit.MILLISECONDS)
                .readTimeout(4000, TimeUnit.MILLISECONDS)
                .writeTimeout(3500, TimeUnit.MILLISECONDS)
                .callTimeout(5000, TimeUnit.MILLISECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .build();
    }

    PageResponse fetchHtml(String originalUrl, Cancellation cancellation) {
        String current = originalUrl;
        for (int redirects = 0; redirects <= MAX_REDIRECTS; redirects++) {
            if (isCancelled(cancellation) || !WebPageFetchPolicy.isAllowedUrl(current)) return empty(current);
            Call call = null;
            try {
                Request request = new Request.Builder().url(current).get()
                        .header("User-Agent", "AI-Council-Android/2.0 (source preview)")
                        .header("Accept", "text/html,application/xhtml+xml;q=0.9")
                        .header("Accept-Encoding", "identity")
                        .build();
                call = http.newCall(request);
                activeCalls.add(call);
                if (isCancelled(cancellation)) call.cancel();
                try (Response response = call.execute()) {
                    int code = response.code();
                    if (code >= 300 && code < 400) {
                        String location = response.header("Location");
                        if (location == null || !WebPageFetchPolicy.mayFollowRedirect(
                                resolveLocation(current, location), redirects, MAX_REDIRECTS)) return empty(current);
                        current = resolveLocation(current, location);
                        continue;
                    }
                    if (!response.isSuccessful() || response.body() == null) return empty(current);
                    String contentType = response.header("Content-Type", "").toLowerCase(java.util.Locale.ROOT);
                    if (!(contentType.contains("text/html") || contentType.contains("application/xhtml+xml"))) return empty(current);
                    long length = response.body().contentLength();
                    if (length > MAX_BYTES) return empty(current);
                    String html = readBounded(response.body(), MAX_BYTES, cancellation);
                    return html.isEmpty() ? empty(current) : new PageResponse(html, current);
                }
            } catch (Exception ignored) {
                return empty(current);
            } finally {
                if (call != null) activeCalls.remove(call);
            }
        }
        return empty(current);
    }

    void cancelAll() {
        for (Call call : activeCalls) {
            try { call.cancel(); } catch (Exception ignored) { }
        }
    }

    private static String resolveLocation(String current, String location) throws Exception {
        URL base = new URL(current);
        URL next = new URL(base, location);
        // Fragments are client-side only. Reject instead of silently changing a URL that will be tested.
        if (next.getRef() != null) throw new IOException("Redirect URL contains fragment");
        return next.toExternalForm();
    }

    private static String readBounded(ResponseBody body, int limit, Cancellation cancellation) throws IOException {
        try (java.io.InputStream input = body.byteStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int total = 0;
            while (true) {
                if (isCancelled(cancellation)) throw new IOException("Cancelled");
                int count = input.read(buffer, 0, Math.min(buffer.length, limit + 1 - total));
                if (count == -1) break;
                total += count;
                if (total > limit) return "";
                output.write(buffer, 0, count);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static boolean isCancelled(Cancellation cancellation) {
        return Thread.currentThread().isInterrupted() || cancellation != null && cancellation.isCancelled();
    }

    private static PageResponse empty(String address) { return new PageResponse("", address == null ? "" : address); }
}
