package android.os;
import java.io.File;
import java.io.IOException;
public class ParcelFileDescriptor implements AutoCloseable {
    public static final int MODE_READ_ONLY = 0;
    public static ParcelFileDescriptor open(File file, int mode) throws IOException { return new ParcelFileDescriptor(); }
    @Override public void close() throws IOException { }
}
