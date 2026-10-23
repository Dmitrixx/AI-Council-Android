package okhttp3;

public final class Response implements java.io.Closeable {
    public int code() { return 599; }
    public boolean isSuccessful() { return code() >= 200 && code() < 300; }
    public ResponseBody body() { return null; }
    public String header(String name) { return null; }
    public String header(String name, String fallback) { String value = header(name); return value == null ? fallback : value; }
    @Override public void close() { }
}
