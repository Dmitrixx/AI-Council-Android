package okhttp3;

public class RequestBody {
    private final String value;
    private final MediaType mediaType;
    private RequestBody(String value, MediaType mediaType) { this.value = value; this.mediaType = mediaType; }
    public static RequestBody create(String value, MediaType mediaType) { return new RequestBody(value, mediaType); }
    public String value() { return value; }
    public MediaType contentType() { return mediaType; }
}
