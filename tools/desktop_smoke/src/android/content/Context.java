package android.content;
import java.io.File;
public class Context {
    public Context getApplicationContext() { return this; }
    public ContentResolver getContentResolver() { return null; }
    public File getCacheDir() { return new File(System.getProperty("java.io.tmpdir")); }
}
