package android.content;
import android.database.Cursor;
import android.net.Uri;
import java.io.InputStream;
public abstract class ContentResolver {
    public abstract Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);
    public abstract InputStream openInputStream(Uri uri) throws java.io.FileNotFoundException;
}
