package com.aicouncil.app;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Cancellable, bounded transport for OpenAI-compatible /models endpoints.
 * URL/local-only and credential rules are shared with LocalAiPolicy and EndpointPolicy.
 */
public final class ProviderModelsClient {
    public interface Cancellation { boolean isCancelled(); }

    private static final int MAX_RESPONSE_BYTES = 250_000;
    private static final int MAX_ERROR_BYTES = 12_000;
    private final OkHttpClient http;
    private final Set<Call> activeCalls = ConcurrentHashMap.newKeySet();

    public ProviderModelsClient() {
        http = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .callTimeout(12, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .build();
    }

    public Result fetch(String baseUrl, String apiKey, boolean localOnly, Cancellation cancellation) {
        if (isCancelled(cancellation)) return Result.cancelledResult();
        if (!LocalAiPolicy.mayUseEndpoint(baseUrl, localOnly)) {
            return Result.failure("Недопустимый URL списка моделей или адрес заблокирован режимом «Только локальные ИИ». Проверьте HTTPS и настройки локального сервера.", 0);
        }
        String address = LocalAiPolicy.modelsEndpoint(baseUrl);
        if (address.isEmpty() || !EndpointPolicy.isAllowedApiBase(address)) {
            return Result.failure("Не удалось сформировать безопасный адрес /models из базового URL.", 0);
        }

        Request.Builder builder = new Request.Builder().url(address).get()
                .header("Accept", "application/json")
                .header("Accept-Encoding", "identity")
                .header("User-Agent", "AI-Council-Android/2.0 (model catalogue)");
        // A configured key is never sent to loopback, private-LAN or .local endpoints.
        if (LocalAiPolicy.shouldSendAuthorization(baseUrl, apiKey)) {
            builder.header("Authorization", "Bearer " + apiKey.trim());
        }
        Call call = http.newCall(builder.build());
        activeCalls.add(call);
        // Close the race where cancelAll() runs just before a new call is registered.
        if (isCancelled(cancellation)) call.cancel();
        try (Response response = call.execute()) {
            if (isCancelled(cancellation) || call.isCanceled()) return Result.cancelledResult();
            int code = response.code();
            ResponseBody body = response.body();
            boolean successful = code >= 200 && code < 300;
            String responseText = readLimited(body, successful ? MAX_RESPONSE_BYTES : MAX_ERROR_BYTES, successful);
            if (responseText == null) {
                return Result.failure("Ответ со списком моделей превышает лимит 250 КБ.", code);
            }
            if (code < 200 || code >= 300) {
                String formatted = ProviderErrorFormatter.formatHttpError(code, responseText, "список моделей", apiKey);
                return Result.failure(SecretRedactor.redact(limit(formatted, 1200), apiKey), code);
            }
            return Result.success(responseText, code);
        } catch (IOException error) {
            if (isCancelled(cancellation) || call.isCanceled() || Thread.currentThread().isInterrupted()) {
                return Result.cancelledResult();
            }
            String message = error.getMessage();
            String detail = "Не удалось получить список моделей: " + error.getClass().getSimpleName()
                    + (message == null || message.trim().isEmpty() ? "" : " — " + message);
            return Result.failure(SecretRedactor.redact(limit(detail, 1200), apiKey), 0);
        } finally {
            activeCalls.remove(call);
        }
    }

    public void cancelAll() {
        for (Call call : activeCalls) {
            try { call.cancel(); } catch (Exception ignored) { }
        }
    }

    private static boolean isCancelled(Cancellation cancellation) {
        return Thread.currentThread().isInterrupted() || (cancellation != null && cancellation.isCancelled());
    }

    /** Reads a bounded response. Large success bodies are rejected; error bodies are truncated so HTTP hints survive. */
    private static String readLimited(ResponseBody body, int maxBytes, boolean rejectOversize) throws IOException {
        if (body == null) return "";
        try (InputStream input = body.byteStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int total = 0;
            int count;
            boolean truncated = false;
            while ((count = input.read(buffer, 0, buffer.length)) != -1) {
                int allowed = Math.min(count, maxBytes - total);
                if (allowed > 0) { output.write(buffer, 0, allowed); total += allowed; }
                if (allowed < count) {
                    if (rejectOversize) return null;
                    truncated = true;
                    break;
                }
            }
            String value = new String(output.toByteArray(), StandardCharsets.UTF_8);
            return truncated ? value + "\n[Детали ошибки обрезаны из-за ограничения размера ответа.]" : value;
        }
    }

    private static String limit(String value, int maximum) {
        if (value == null) return "";
        return value.length() <= maximum ? value : value.substring(0, maximum) + "…";
    }

    public static final class Result {
        public final String body;
        public final String error;
        public final boolean cancelled;
        public final int httpCode;

        private Result(String body, String error, boolean cancelled, int httpCode) {
            this.body = body == null ? "" : body;
            this.error = error == null ? "" : error;
            this.cancelled = cancelled;
            this.httpCode = httpCode;
        }

        static Result success(String body, int httpCode) { return new Result(body, "", false, httpCode); }
        static Result failure(String error, int httpCode) { return new Result("", error, false, httpCode); }
        static Result cancelledResult() { return new Result("", "", true, 0); }
    }
}
