package okhttp3;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class OkHttpClient {
    public static volatile Request lastRequest;
    public static final java.util.concurrent.atomic.AtomicInteger callCount = new java.util.concurrent.atomic.AtomicInteger();
    public static final class Builder {
        public Builder dns(Dns value) { return this; }
        public Builder connectTimeout(long value, TimeUnit unit) { return this; }
        public Builder readTimeout(long value, TimeUnit unit) { return this; }
        public Builder writeTimeout(long value, TimeUnit unit) { return this; }
        public Builder callTimeout(long value, TimeUnit unit) { return this; }
        public Builder followRedirects(boolean value) { return this; }
        public Builder followSslRedirects(boolean value) { return this; }
        public Builder retryOnConnectionFailure(boolean value) { return this; }
        public OkHttpClient build() { return new OkHttpClient(); }
    }
    public Call newCall(Request request) {
        lastRequest = request;
        callCount.incrementAndGet();
        return new Call() {
            private volatile boolean canceled;
            @Override public Response execute() throws IOException {
                if (canceled) throw new IOException("Canceled");
                throw new IOException("Desktop smoke-test OkHttp stub does not perform network access");
            }
            @Override public void cancel() { canceled = true; }
            @Override public boolean isCanceled() { return canceled; }
        };
    }
}
