package okhttp3;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Request {
    private final String url;
    private final Map<String, String> headers;
    private final String method;
    private final RequestBody body;
    private Request(String url, Map<String, String> headers, String method, RequestBody body) {
        this.url = url; this.headers = Collections.unmodifiableMap(new LinkedHashMap<String, String>(headers)); this.method = method; this.body = body;
    }
    public String url() { return url; }
    public String method() { return method; }
    public RequestBody body() { return body; }
    public String header(String name) {
        for (Map.Entry<String, String> header : headers.entrySet()) if (header.getKey().equalsIgnoreCase(name)) return header.getValue();
        return null;
    }
    public Map<String, String> headers() { return headers; }
    public static final class Builder {
        private String url;
        private final Map<String, String> headers = new LinkedHashMap<String, String>();
        private String method = "GET";
        private RequestBody body;
        public Builder url(String value) { this.url = value; return this; }
        public Builder get() { this.method = "GET"; this.body = null; return this; }
        public Builder post(RequestBody value) { this.method = "POST"; this.body = value; return this; }
        public Builder header(String name, String value) { this.headers.put(name, value); return this; }
        public Request build() { return new Request(url, headers, method, body); }
    }
}
