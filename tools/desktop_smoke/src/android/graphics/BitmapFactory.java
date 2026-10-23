package android.graphics;
import java.io.InputStream;
public final class BitmapFactory {
    public static final class Options {
        public boolean inJustDecodeBounds;
        public int inSampleSize = 1;
        public Bitmap.Config inPreferredConfig;
        public int outWidth = 1;
        public int outHeight = 1;
    }
    public static Bitmap decodeStream(InputStream input, Object padding, Options options) { return options != null && options.inJustDecodeBounds ? null : new Bitmap(); }
    private BitmapFactory() { }
}
