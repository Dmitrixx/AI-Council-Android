package com.aicouncil.app;

import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** OpenAI-compatible streaming transport shared by all cloud and LAN chat endpoints. */
public final class ChatCompletionsClient {
    public interface Cancellation { boolean isCancelled(); }
    public interface TokenListener { void onToken(String token); }

    public static final class Result {
        public final String text;
        public final String error;
        public final boolean cancelled;
        public final boolean truncated;
        Result(String text, String error, boolean cancelled, boolean truncated) {
            this.text = text == null ? "" : text;
            this.error = error == null ? "" : error;
            this.cancelled = cancelled;
            this.truncated = truncated;
        }
    }

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int MAX_RESPONSE_BYTES = 2_000_000;
    private static final int MAX_ERROR_BYTES = 12_000;
    private final OkHttpClient http;
    private final Set<Call> activeCalls = ConcurrentHashMap.newKeySet();

    public ChatCompletionsClient() {
        http = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .callTimeout(110, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
    }

    public Result execute(String endpoint, String requestJson, String model, String apiKey,
                          boolean sendAuthorization, Cancellation cancellation, TokenListener listener) {
        String target = normalizeChatEndpoint(endpoint);
        if (target.isEmpty() || !EndpointPolicy.isAllowedApiBase(endpoint)) {
            return new Result("", "Ошибка настроек: облачные API должны использовать HTTPS; HTTP разрешён только локальным адресам.", false, false);
        }
        JSONObject body;
        try {
            body = new JSONObject(requestJson);
            body.put("stream", true);
        } catch (Exception invalidJson) {
            return new Result("", "Ошибка запроса: не удалось подготовить JSON.", false, false);
        }

        int retry = 0;
        boolean completionTokenField = body.has("max_completion_tokens");
        while (true) {
            if (isCancelled(cancellation)) return new Result("", "", true, false);
            Request.Builder builder = new Request.Builder().url(target)
                    .header("Accept", "text/event-stream")
                    .header("Cache-Control", "no-cache")
                    .post(RequestBody.create(body.toString(), JSON));
            if (sendAuthorization && apiKey != null && !apiKey.trim().isEmpty()) {
                builder.header("Authorization", "Bearer " + apiKey.trim());
            }
            Call call = http.newCall(builder.build());
            activeCalls.add(call);
            StringBuilder text = new StringBuilder();
            String finishReason = "";
            boolean retriedTokenField = false;
            try (Response response = call.execute()) {
                if (isCancelled(cancellation)) return new Result(text.toString(), "", true, false);
                if (!response.isSuccessful()) {
                    String errorBody = readLimited(response.body(), MAX_ERROR_BYTES);
                    int code = response.code();
                    String lower = errorBody.toLowerCase(java.util.Locale.ROOT);
                    boolean tokenFieldRejected = code == 400 && !completionTokenField
                            && lower.contains("max_tokens")
                            && (lower.contains("max_completion_tokens") || lower.contains("unsupported parameter")
                            || lower.contains("unrecognized request argument") || lower.contains("unknown parameter"));
                    if (tokenFieldRejected && !retriedTokenField) {
                        Object maxTokens = body.opt("max_tokens");
                        body.remove("max_tokens");
                        try {
                            body.put("max_completion_tokens", maxTokens == null ? 2048 : maxTokens);
                        } catch (Exception invalidFallbackJson) {
                            return new Result(text.toString(), "Ошибка запроса: не удалось подготовить совместимый лимит токенов.", false, false);
                        }
                        completionTokenField = true;
                        retriedTokenField = true;
                        // Compatibility fallback changes only the parameter name; no backoff is used.
                        continue;
                    }
                    if ((code == 429 || code >= 500 && code <= 599) && retry < 2) {
                        retry++;
                        if (!sleepBeforeRetry(retry, cancellation)) return new Result(text.toString(), "", true, false);
                        continue;
                    }
                    return new Result("", ProviderErrorFormatter.formatHttpError(code, errorBody, model, apiKey), false, false);
                }
                ResponseBody responseBody = response.body();
                if (responseBody == null) return new Result("", "Ошибка API: пустое потоковое тело ответа.", false, false);
                long received = 0;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (isCancelled(cancellation) || call.isCanceled()) {
                            return new Result(text.toString(), "", true, false);
                        }
                        received += line.getBytes(StandardCharsets.UTF_8).length + 1L;
                        if (received > MAX_RESPONSE_BYTES) {
                            return new Result(text.toString(), "Ошибка API: потоковый ответ превышает лимит 2 МБ.", false, false);
                        }
                        if (line.isEmpty() || !line.startsWith("data:")) continue;
                        String data = line.substring(5).trim();
                        if ("[DONE]".equals(data)) break;
                        try {
                            JSONObject event = new JSONObject(data);
                            JSONArray choices = event.optJSONArray("choices");
                            if (choices == null || choices.length() == 0) continue;
                            JSONObject choice = choices.optJSONObject(0);
                            if (choice == null) continue;
                            String currentFinish = choice.optString("finish_reason", "");
                            if (!currentFinish.isEmpty() && !"null".equals(currentFinish)) finishReason = currentFinish;
                            JSONObject delta = choice.optJSONObject("delta");
                            if (delta == null) continue;
                            String token = extractDeltaText(delta.opt("content"));
                            if (token.isEmpty()) token = delta.optString("text", "");
                            if (!token.isEmpty()) {
                                text.append(token);
                                if (listener != null) listener.onToken(token);
                            }
                        } catch (Exception malformedEvent) {
                            // Ignore provider-specific metadata events; valid text deltas still pass through.
                        }
                    }
                }
                boolean truncated = "length".equalsIgnoreCase(finishReason);
                return new Result(text.toString(), "", false, truncated);
            } catch (IOException io) {
                if (isCancelled(cancellation) || call.isCanceled() || Thread.currentThread().isInterrupted()) {
                    return new Result(text.toString(), "", true, false);
                }
                return new Result(text.toString(), "Ошибка запроса: " + io.getClass().getSimpleName()
                        + (io.getMessage() == null ? "" : " — " + SecretRedactor.redact(io.getMessage(), apiKey))
                        + "\nПроверьте подключение к серверу и доступность выбранной модели.", false, false);
            } finally {
                activeCalls.remove(call);
            }
        }
    }

    public void cancelAll() {
        for (Call call : activeCalls) {
            try { call.cancel(); } catch (Exception ignored) { }
        }
    }

    private static boolean sleepBeforeRetry(int retry, Cancellation cancellation) {
        try {
            Thread.sleep(retry == 1 ? 450L : 1000L);
            return !isCancelled(cancellation);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static boolean isCancelled(Cancellation cancellation) {
        return Thread.currentThread().isInterrupted() || cancellation != null && cancellation.isCancelled();
    }

    private static String normalizeChatEndpoint(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) return "";
        String target = endpoint.trim();
        while (target.endsWith("/")) target = target.substring(0, target.length() - 1);
        try {
            java.net.URL parsed = new java.net.URL(target);
            if (!parsed.getPath().endsWith("/chat/completions")) target += "/chat/completions";
            return target;
        } catch (Exception ignored) { return ""; }
    }

    private static String readLimited(ResponseBody body, int maximumBytes) {
        if (body == null) return "";
        try (InputStream input = body.byteStream(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int total = 0;
            int count;
            while (total < maximumBytes && (count = input.read(buffer, 0, Math.min(buffer.length, maximumBytes - total))) != -1) {
                bytes.write(buffer, 0, count);
                total += count;
            }
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException ignored) { return ""; }
    }

    private static String extractDeltaText(Object value) {
        if (value instanceof String) return (String) value;
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            StringBuilder joined = new StringBuilder();
            for (int i = 0; i < array.length(); i++) {
                JSONObject part = array.optJSONObject(i);
                if (part != null) joined.append(part.optString("text", ""));
            }
            return joined.toString();
        }
        return "";
    }
}
