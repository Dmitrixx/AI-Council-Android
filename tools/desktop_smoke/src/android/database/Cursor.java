package android.database;
public interface Cursor extends AutoCloseable {
    boolean moveToFirst();
    int getColumnIndex(String name);
    boolean isNull(int index);
    long getLong(int index);
    String getString(int index);
    @Override void close();
}
