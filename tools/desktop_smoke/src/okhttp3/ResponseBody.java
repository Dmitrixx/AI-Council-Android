package okhttp3;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ResponseBody {
    public long contentLength() { return -1L; }
    public InputStream byteStream() { return new ByteArrayInputStream(new byte[0]); }
}
