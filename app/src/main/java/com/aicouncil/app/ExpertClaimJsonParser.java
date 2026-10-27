package com.aicouncil.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Strict, bounded JSON decoder for the versioned structured-claim contract.
 *
 * <p>Expected root: {"schema_version":1,"claims":[...]}. Invalid JSON or an
 * unsupported schema throws IllegalArgumentException; callers may then use the
 * documented prose fallback. No network or Android classes are required.</p>
 */
public final class ExpertClaimJsonParser {
    private static final int MAX_JSON_CHARS = 200_000;
    private static final int MAX_CLAIMS = 100;
    private static final int MAX_DEPTH = 24;

    private ExpertClaimJsonParser() {}

    public static List<ExpertClaim> parse(String json) {
        if (json == null || json.trim().isEmpty()) throw invalid("JSON is empty");
        if (json.length() > MAX_JSON_CHARS) throw invalid("JSON exceeds size limit");
        Object rootValue = new JsonReader(json).readDocument();
        Map<?, ?> root = asObject(rootValue, "root");
        Object versionValue = root.get("schema_version");
        if (!(versionValue instanceof Number) || ((Number) versionValue).doubleValue() != 1.0) {
            throw invalid("unsupported or missing schema_version (expected 1)");
        }
        Object claimsValue = root.get("claims");
        if (!(claimsValue instanceof List<?>)) throw invalid("'claims' must be an array");
        List<?> rows = (List<?>) claimsValue;
        if (rows.size() > MAX_CLAIMS) throw invalid("too many claims");
        List<ExpertClaim> claims = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) claims.add(parseClaim(rows.get(i), i));
        return java.util.Collections.unmodifiableList(claims);
    }

    private static ExpertClaim parseClaim(Object value, int index) {
        Map<?, ?> row = asObject(value, "claims[" + index + "]");
        String id = optionalString(row, "id");
        String statement = requiredString(row, "statement");
        Double numericValue = optionalNumber(row, "numeric_value");
        String unit = optionalString(row, "unit");
        ExpertClaim.Polarity polarity = enumValue(row, "polarity", ExpertClaim.Polarity.class,
                ExpertClaim.Polarity.UNCERTAIN);
        String horizon = optionalString(row, "time_horizon");
        Double confidence = optionalNumber(row, "confidence");
        ExpertClaim.EvidenceClass evidence = enumValue(row, "evidence_class",
                ExpertClaim.EvidenceClass.class, ExpertClaim.EvidenceClass.UNSOURCED);
        List<String> stakeholders = optionalStringArray(row, "stakeholders");
        Boolean reversible = optionalBoolean(row, "reversible");
        String quote = optionalString(row, "source_quote");
        String sourceId = optionalString(row, "source_id");
        try {
            return new ExpertClaim(id, statement, numericValue, unit, polarity, horizon,
                    confidence, evidence, stakeholders, reversible, quote, sourceId);
        } catch (IllegalArgumentException error) {
            throw invalid("claims[" + index + "]: " + error.getMessage());
        }
    }

    private static String requiredString(Map<?, ?> row, String key) {
        Object value = row.get(key);
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
            throw invalid("'" + key + "' must be a non-empty string");
        }
        return ((String) value).trim();
    }

    private static String optionalString(Map<?, ?> row, String key) {
        Object value = row.get(key);
        if (value == null) return "";
        if (!(value instanceof String)) throw invalid("'" + key + "' must be a string or null");
        return ((String) value).trim();
    }

    private static Double optionalNumber(Map<?, ?> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (!(value instanceof Number)) throw invalid("'" + key + "' must be a number or null");
        double number = ((Number) value).doubleValue();
        if (Double.isNaN(number) || Double.isInfinite(number)) throw invalid("'" + key + "' must be finite");
        return number;
    }

    private static Boolean optionalBoolean(Map<?, ?> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (!(value instanceof Boolean)) throw invalid("'" + key + "' must be boolean or null");
        return (Boolean) value;
    }

    private static List<String> optionalStringArray(Map<?, ?> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (!(value instanceof List<?>)) throw invalid("'" + key + "' must be an array or null");
        List<String> values = new ArrayList<>();
        for (Object item : (List<?>) value) {
            if (!(item instanceof String)) throw invalid("'" + key + "' items must be strings");
            values.add((String) item);
        }
        return values;
    }

    private static <E extends Enum<E>> E enumValue(Map<?, ?> row, String key, Class<E> type, E fallback) {
        Object value = row.get(key);
        if (value == null) return fallback;
        if (!(value instanceof String)) throw invalid("'" + key + "' must be a string or null");
        String normalized = ((String) value).trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        try { return Enum.valueOf(type, normalized); }
        catch (IllegalArgumentException error) { throw invalid("unsupported " + key + ": " + value); }
    }

    private static Map<?, ?> asObject(Object value, String label) {
        if (!(value instanceof Map<?, ?>)) throw invalid(label + " must be a JSON object");
        return (Map<?, ?>) value;
    }

    private static IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException("Invalid structured claims JSON: " + message);
    }

    private static final class JsonReader {
        private final String text;
        private int pos;
        private JsonReader(String text) { this.text = text; }

        Object readDocument() {
            skipSpace();
            Object value = readValue(0);
            skipSpace();
            if (pos != text.length()) throw invalid("unexpected trailing data at character " + pos);
            return value;
        }

        private Object readValue(int depth) {
            if (depth > MAX_DEPTH) throw invalid("maximum JSON nesting exceeded");
            skipSpace();
            if (pos >= text.length()) throw invalid("unexpected end of JSON");
            char c = text.charAt(pos);
            if (c == '{') return readObject(depth + 1);
            if (c == '[') return readArray(depth + 1);
            if (c == '"') return readString();
            if (c == 't') { consume("true"); return Boolean.TRUE; }
            if (c == 'f') { consume("false"); return Boolean.FALSE; }
            if (c == 'n') { consume("null"); return null; }
            if (c == '-' || (c >= '0' && c <= '9')) return readNumber();
            throw invalid("unexpected character at " + pos);
        }

        private Map<String, Object> readObject(int depth) {
            pos++;
            skipSpace();
            Map<String, Object> map = new LinkedHashMap<>();
            if (take('}')) return map;
            while (true) {
                skipSpace();
                if (pos >= text.length() || text.charAt(pos) != '"') throw invalid("object key must be a string");
                String key = readString();
                skipSpace();
                require(':');
                Object value = readValue(depth);
                if (map.containsKey(key)) throw invalid("duplicate object key: " + key);
                map.put(key, value);
                skipSpace();
                if (take('}')) return map;
                require(',');
            }
        }

        private List<Object> readArray(int depth) {
            pos++;
            skipSpace();
            List<Object> list = new ArrayList<>();
            if (take(']')) return list;
            while (true) {
                if (list.size() > MAX_CLAIMS * 20) throw invalid("array item limit exceeded");
                list.add(readValue(depth));
                skipSpace();
                if (take(']')) return list;
                require(',');
            }
        }

        private String readString() {
            require('"');
            StringBuilder out = new StringBuilder();
            while (pos < text.length()) {
                char c = text.charAt(pos++);
                if (c == '"') return out.toString();
                if (c < 0x20) throw invalid("unescaped control character in string");
                if (c != '\\') { out.append(c); continue; }
                if (pos >= text.length()) throw invalid("unfinished string escape");
                char escaped = text.charAt(pos++);
                switch (escaped) {
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    case '/': out.append('/'); break;
                    case 'b': out.append('\b'); break;
                    case 'f': out.append('\f'); break;
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case 'u':
                        if (pos + 4 > text.length()) throw invalid("short unicode escape");
                        int code = 0;
                        for (int i = 0; i < 4; i++) {
                            int digit = Character.digit(text.charAt(pos++), 16);
                            if (digit < 0) throw invalid("invalid unicode escape");
                            code = (code << 4) | digit;
                        }
                        out.append((char) code);
                        break;
                    default: throw invalid("invalid string escape");
                }
            }
            throw invalid("unterminated string");
        }

        private Number readNumber() {
            int start = pos;
            if (take('-') && pos >= text.length()) throw invalid("invalid number");
            if (take('0')) {
                if (pos < text.length() && Character.isDigit(text.charAt(pos))) throw invalid("leading zero in number");
            } else {
                requireDigit();
                while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++;
            }
            if (take('.')) {
                requireDigit();
                while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++;
            }
            if (take('e') || take('E')) {
                if (!take('+')) take('-');
                requireDigit();
                while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++;
            }
            String value = text.substring(start, pos);
            try {
                double parsed = Double.parseDouble(value);
                if (Double.isInfinite(parsed) || Double.isNaN(parsed)) throw invalid("number is not finite");
                return parsed;
            } catch (NumberFormatException error) {
                throw invalid("invalid number");
            }
        }

        private void requireDigit() {
            if (pos >= text.length() || !Character.isDigit(text.charAt(pos))) throw invalid("digit expected");
        }
        private void consume(String token) {
            if (!text.startsWith(token, pos)) throw invalid("invalid literal at " + pos);
            pos += token.length();
        }
        private boolean take(char c) {
            if (pos < text.length() && text.charAt(pos) == c) { pos++; return true; }
            return false;
        }
        private void require(char c) {
            if (!take(c)) throw invalid("expected '" + c + "' at " + pos);
        }
        private void skipSpace() {
            while (pos < text.length()) {
                char c = text.charAt(pos);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') pos++;
                else break;
            }
        }
    }
}
