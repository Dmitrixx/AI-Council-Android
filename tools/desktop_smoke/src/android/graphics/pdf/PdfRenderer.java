package android.graphics.pdf;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import java.io.IOException;
public class PdfRenderer implements AutoCloseable {
    public PdfRenderer(ParcelFileDescriptor descriptor) throws IOException { }
    public int getPageCount() { return 1; }
    public Page openPage(int index) throws IOException { return new Page(); }
    @Override public void close() throws IOException { }
    public static class Page implements AutoCloseable {
        public static final int RENDER_MODE_FOR_DISPLAY = 0;
        public int getWidth() { return 1; }
        public int getHeight() { return 1; }
        public void render(Bitmap destination, Object destClip, Object transform, int mode) { }
        @Override public void close() throws IOException { }
    }
}
