package com.aicouncil.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Small on-device store for prompts, history and projects. API credentials remain in
 * encrypted SharedPreferences and are deliberately never copied into this database.
 * The current JSON payloads are kept intact to preserve backup compatibility.
 */
public final class LocalDataStore extends SQLiteOpenHelper {
    private static final String DB_NAME = "aicouncil_local_data.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "local_payload";
    private static final String KEY = "payload_key";
    private static final String VALUE = "payload_value";
    private static final String MIGRATION_KEY = "__shared_preferences_migration_v1";
    private SharedPreferences legacyPreferences;

    public LocalDataStore(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
        setWriteAheadLoggingEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" + KEY + " TEXT PRIMARY KEY NOT NULL, "
                + VALUE + " TEXT NOT NULL, updated_at INTEGER NOT NULL)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Keep existing payloads during forward-compatible upgrades.
    }

    /** One-time migration from the legacy settings file; credentials are not in this allow-list. */
    public synchronized void migrateLegacy(SharedPreferences legacy) {
        legacyPreferences = legacy;
        if ("1".equals(readDatabaseValue(MIGRATION_KEY))) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            String[] keys = {"history", "projects", "draftPrompt", "draftFollowUp", "draftMode"};
            for (String key : keys) {
                if (readDatabaseValue(db, key) == null && legacy.contains(key)) {
                    Object value;
                    if ("draftMode".equals(key)) value = String.valueOf(legacy.getInt(key, 0));
                    else value = legacy.getString(key, "");
                    putDatabaseValue(db, key, String.valueOf(value));
                }
            }
            putDatabaseValue(db, MIGRATION_KEY, "1");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public synchronized String getString(String key, String fallback) {
        String value = readDatabaseValue(key);
        if (value != null) return value;
        // Read-through fallback protects recovery from interrupted/older upgrades.
        if (legacyPreferences != null && legacyPreferences.contains(key)) {
            try {
                return legacyPreferences.getString(key, fallback);
            } catch (ClassCastException wrongLegacyType) {
                try { return String.valueOf(legacyPreferences.getInt(key, 0)); }
                catch (ClassCastException ignored) { return fallback; }
            }
        }
        return fallback;
    }

    public synchronized void putString(String key, String value) {
        putDatabaseValue(getWritableDatabase(), key, value == null ? "" : value);
    }

    public synchronized int getInt(String key, int fallback) {
        try { return Integer.parseInt(getString(key, String.valueOf(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private String readDatabaseValue(String key) {
        return readDatabaseValue(getReadableDatabase(), key);
    }

    private String readDatabaseValue(SQLiteDatabase db, String key) {
        try (Cursor cursor = db.query(TABLE, new String[]{VALUE}, KEY + "=?",
                new String[]{key}, null, null, null, "1")) {
            return cursor.moveToFirst() ? cursor.getString(0) : null;
        }
    }

    private void putDatabaseValue(SQLiteDatabase db, String key, String value) {
        ContentValues values = new ContentValues();
        values.put(KEY, key);
        values.put(VALUE, value == null ? "" : value);
        values.put("updated_at", System.currentTimeMillis());
        db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
