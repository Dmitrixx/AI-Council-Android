package android.graphics;
public class Bitmap {
    public enum Config { RGB_565 }
    public boolean isRecycled() { return false; }
    public void recycle() { }
    public int getWidth() { return 1; }
    public int getHeight() { return 1; }
    public void eraseColor(int color) { }
    public static Bitmap createBitmap(int width, int height, Config config) { return new Bitmap(); }
    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, Matrix matrix, boolean filter) { return source; }
}
