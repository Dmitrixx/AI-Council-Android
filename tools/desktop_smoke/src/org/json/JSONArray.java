package org.json;

/** Minimal compile-time stub for offline smoke tests; production uses Android's org.json. */
public class JSONArray {
    public int length() { return 0; }
    public JSONObject optJSONObject(int index) { return null; }
    public String optString(int index) { return ""; }
}
