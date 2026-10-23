package org.json;

/** Minimal compile-time stub for offline smoke tests; production uses Android's org.json. */
public class JSONObject {
    public JSONObject() { }
    public JSONObject(String json) { }
    public JSONObject put(String key, Object value) { return this; }
    public JSONObject optJSONObject(String key) { return null; }
    public JSONArray optJSONArray(String key) { return null; }
    public String optString(String key) { return ""; }
    public String optString(String key, String fallback) { return fallback; }
    public boolean has(String key) { return false; }
    @Override public String toString() { return "{}"; }
}
