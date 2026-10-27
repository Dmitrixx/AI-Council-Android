package com.aicouncil.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Versioned claim graph. Strict parser for model JSON, plus a local
 * sentence graph when models return prose. No network.
 */
public final class ClaimGraph {
    public static final int VERSION = 1;
    public static final int MAX_CLAIMS = 24;
    public static final int MAX_EDGES = 48;
    public static final int MAX_TEXT = 400;

    public static final class Claim {
        public final String id;
        public final String source;
        public final String text;
        public final String polarity;
        public final int confidence;

        public Claim(String id, String source, String text, String polarity, int confidence) {
            this.id = id;
            this.source = source;
            this.text = text;
            this.polarity = polarity;
            this.confidence = confidence;
        }
    }

    public static final class Edge {
        public final String from;
        public final String to;
        public final String rel;

        public Edge(String from, String to, String rel) {
            this.from = from;
            this.to = to;
            this.rel = rel;
        }
    }

    public static final class Graph {
        public final int version;
        public final List<Claim> claims;
        public final List<Edge> edges;
        public final List<String> problems;

        public Graph(int version, List<Claim> claims, List<Edge> edges, List<String> problems) {
            this.version = version;
            this.claims = claims == null ? Collections.<Claim>emptyList() : claims;
            this.edges = edges == null ? Collections.<Edge>emptyList() : edges;
            this.problems = problems == null ? Collections.<String>emptyList() : problems;
        }

        public boolean ok() {
            return problems.isEmpty() && !claims.isEmpty();
        }
    }

    private ClaimGraph() {}

    public static Graph parse(String json) {
        List<String> problems = new ArrayList<String>();
        if (json == null || json.trim().isEmpty()) {
            problems.add("пустой граф");
            return new Graph(0, Collections.<Claim>emptyList(), Collections.<Edge>emptyList(), problems);
        }
        try {
            Parser parser = new Parser(json);
            return parser.parseGraph();
        } catch (IllegalArgumentException ex) {
            problems.add(ex.getMessage() == null ? "не разобран" : ex.getMessage());
            return new Graph(0, Collections.<Claim>emptyList(), Collections.<Edge>emptyList(), problems);
        }
    }

    /** Sentence-level graph from two expert answers. Empty when there is nothing to link. */
    public static Graph fromAnswers(String answerA, String answerB) {
        List<Claim> claims = new ArrayList<Claim>();
        addSentences(claims, "1", "a", answerA);
        addSentences(claims, "2", "b", answerB);
        List<Edge> edges = new ArrayList<Edge>();
        for (int i = 0; i < claims.size(); i++) {
            for (int j = i + 1; j < claims.size(); j++) {
                Claim left = claims.get(i);
                Claim right = claims.get(j);
                if (left.source.equals(right.source)) continue;
                if (!sharesSubject(left.text, right.text)) continue;
                if (opposite(left.polarity, right.polarity)) {
                    edges.add(new Edge(left.id, right.id, "contradicts"));
                } else if (left.polarity.equals(right.polarity) && !"neutral".equals(left.polarity)) {
                    edges.add(new Edge(left.id, right.id, "supports"));
                }
                if (edges.size() >= MAX_EDGES) break;
            }
            if (edges.size() >= MAX_EDGES) break;
        }
        return new Graph(VERSION, claims, edges, Collections.<String>emptyList());
    }

    public static String noteForCoordinator(Graph graph) {
        if (graph == null || graph.claims.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("ГРАФ УТВЕРЖДЕНИЙ v").append(graph.version).append(" (локально, не источник):\n");
        int shown = 0;
        for (Claim claim : graph.claims) {
            if (shown >= 8) break;
            sb.append("• [").append(claim.source).append("/").append(claim.polarity).append("] ")
                    .append(claim.id).append(": ").append(claim.text).append("\n");
            shown++;
        }
        boolean anyContra = false;
        for (Edge edge : graph.edges) {
            if (!"contradicts".equals(edge.rel)) continue;
            if (!anyContra) {
                sb.append("ПРОТИВОРЕЧИЯ ГРАФА:\n");
                anyContra = true;
            }
            sb.append("• ").append(edge.from).append(" ↔ ").append(edge.to).append("\n");
        }
        if (!graph.problems.isEmpty()) {
            sb.append("ПРОБЛЕМЫ РАЗБОРА:\n");
            for (String problem : graph.problems) sb.append("• ").append(problem).append("\n");
        }
        sb.append("Не склеивай противоречащие узлы в среднее. Сохрани открытое ребро как dissent.\n");
        return sb.toString().trim();
    }

    private static void addSentences(List<Claim> claims, String source, String prefix, String text) {
        if (text == null || claims.size() >= MAX_CLAIMS) return;
        String[] parts = text.split("(?<=[.!?])\\s+");
        int n = 0;
        for (String part : parts) {
            String sentence = part.trim().replaceAll("\\s+", " ");
            if (sentence.length() < 40 || sentence.length() > MAX_TEXT) continue;
            n++;
            claims.add(new Claim(prefix + n, source, sentence, polarityOf(sentence), 0));
            if (n >= 4 || claims.size() >= MAX_CLAIMS) return;
        }
    }

    static String polarityOf(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        String stripped = lower.replace("не рекоменду", " ").replace("не стоит", " ").replace("нельзя", " ");
        boolean against = lower.contains("не рекоменду") || lower.contains("не стоит") || lower.contains("нельзя")
                || lower.contains("против") || lower.contains("запрещ") || lower.contains("паден");
        boolean support = stripped.contains("рекоменд") || stripped.contains("стоит")
                || stripped.contains("нужно") || stripped.contains("рост");
        if (against && !support) return "against";
        if (support && !against) return "for";
        return "neutral";
    }

    private static boolean opposite(String left, String right) {
        return ("for".equals(left) && "against".equals(right)) || ("against".equals(left) && "for".equals(right));
    }

    private static boolean sharesSubject(String left, String right) {
        Set<String> words = tokens(left);
        words.retainAll(tokens(right));
        return !words.isEmpty();
    }

    private static Set<String> tokens(String text) {
        Set<String> set = new HashSet<String>();
        String[] parts = (text == null ? "" : text.toLowerCase(Locale.ROOT)).replaceAll("[^а-яёa-z0-9\\s]", " ").split("\\s+");
        for (String part : parts) {
            if (part.length() > 4 && !STOP.contains(part)) set.add(part);
        }
        return set;
    }

    private static final Set<String> STOP = new HashSet<String>();
    static {
        String[] words = {"этом", "этого", "этой", "которые", "который", "потому", "после", "перед", "между", "около", "будет", "могут", "можно", "нельзя"};
        for (String word : words) STOP.add(word);
    }

    /** Tiny strict parser for the council claim-graph schema. Rejects duplicate keys. */
    static final class Parser {
        private final String raw;
        private int i;

        Parser(String raw) {
            this.raw = raw;
            this.i = 0;
        }

        Graph parseGraph() {
            skip();
            Obj obj = object();
            skip();
            if (i != raw.length()) throw new IllegalArgumentException("хвост после JSON");
            if (obj.hasDuplicate) throw new IllegalArgumentException("повтор ключа");
            requireKeys(obj, "v", "claims");
            int version = obj.integer("v");
            if (version != VERSION) throw new IllegalArgumentException("неподдерживаемая версия графа");
            List<Val> claimVals = obj.array("claims");
            if (claimVals.size() > MAX_CLAIMS) throw new IllegalArgumentException("слишком много утверждений");
            List<Claim> claims = new ArrayList<Claim>();
            Set<String> ids = new HashSet<String>();
            for (Val val : claimVals) {
                if (val.obj == null) throw new IllegalArgumentException("утверждение не объект");
                Claim claim = readClaim(val.obj);
                if (!ids.add(claim.id)) throw new IllegalArgumentException("повтор id " + claim.id);
                claims.add(claim);
            }
            List<Edge> edges = new ArrayList<Edge>();
            if (obj.fields.containsKey("edges")) {
                List<Val> edgeVals = obj.array("edges");
                if (edgeVals.size() > MAX_EDGES) throw new IllegalArgumentException("слишком много рёбер");
                Set<String> seen = new HashSet<String>();
                for (Val val : edgeVals) {
                    if (val.obj == null) throw new IllegalArgumentException("ребро не объект");
                    Edge edge = readEdge(val.obj, ids);
                    String key = edge.from + ">" + edge.rel + ">" + edge.to;
                    if (!seen.add(key)) throw new IllegalArgumentException("повтор ребра");
                    edges.add(edge);
                }
            }
            return new Graph(version, claims, edges, Collections.<String>emptyList());
        }

        private Claim readClaim(Obj obj) {
            if (obj.hasDuplicate) throw new IllegalArgumentException("повтор ключа в утверждении");
            requireKeys(obj, "id", "source", "text", "polarity", "confidence");
            String id = obj.string("id");
            if (!id.matches("[A-Za-z][A-Za-z0-9_-]{0,16}")) throw new IllegalArgumentException("плохой id");
            String source = obj.string("source");
            if (!source.matches("1|2|3|user")) throw new IllegalArgumentException("плохой source");
            String text = obj.string("text");
            if (text.isEmpty() || text.length() > MAX_TEXT) throw new IllegalArgumentException("плохой текст утверждения");
            String polarity = obj.string("polarity");
            if (!polarity.matches("for|against|neutral")) throw new IllegalArgumentException("плохая полярность");
            int confidence = obj.integer("confidence");
            if (confidence < 0 || confidence > 100) throw new IllegalArgumentException("уверенность вне 0..100");
            return new Claim(id, source, text, polarity, confidence);
        }

        private Edge readEdge(Obj obj, Set<String> ids) {
            if (obj.hasDuplicate) throw new IllegalArgumentException("повтор ключа в ребре");
            requireKeys(obj, "from", "to", "rel");
            String from = obj.string("from");
            String to = obj.string("to");
            String rel = obj.string("rel");
            if (!ids.contains(from) || !ids.contains(to)) throw new IllegalArgumentException("ребро на неизвестный id");
            if (from.equals(to)) throw new IllegalArgumentException("петля");
            if (!rel.matches("supports|contradicts|depends")) throw new IllegalArgumentException("плохой тип ребра");
            return new Edge(from, to, rel);
        }

        private void requireKeys(Obj obj, String... keys) {
            for (String key : keys) {
                if (!obj.fields.containsKey(key)) throw new IllegalArgumentException("нет поля " + key);
            }
        }

        private Obj object() {
            expect('{');
            Obj obj = new Obj();
            skip();
            if (peek('}')) {
                i++;
                return obj;
            }
            while (true) {
                skip();
                String key = string();
                skip();
                expect(':');
                skip();
                Val val = value();
                if (obj.fields.containsKey(key)) obj.hasDuplicate = true;
                obj.fields.put(key, val);
                skip();
                if (peek('}')) {
                    i++;
                    break;
                }
                expect(',');
            }
            return obj;
        }

        private List<Val> array() {
            expect('[');
            List<Val> list = new ArrayList<Val>();
            skip();
            if (peek(']')) {
                i++;
                return list;
            }
            while (true) {
                skip();
                list.add(value());
                skip();
                if (peek(']')) {
                    i++;
                    break;
                }
                expect(',');
            }
            return list;
        }

        private Val value() {
            skip();
            char c = ch();
            if (c == '{') return Val.obj(object());
            if (c == '[') return Val.arr(array());
            if (c == '"') return Val.str(string());
            if (c == '-' || (c >= '0' && c <= '9')) return Val.num(number());
            throw new IllegalArgumentException("ожидалось значение");
        }

        private String string() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (i < raw.length()) {
                char c = raw.charAt(i++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (i >= raw.length()) throw new IllegalArgumentException("оборванный escape");
                    char n = raw.charAt(i++);
                    if (n == '"' || n == '\\' || n == '/') sb.append(n);
                    else if (n == 'n') sb.append('\n');
                    else if (n == 't') sb.append('\t');
                    else throw new IllegalArgumentException("плохой escape");
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("оборванная строка");
        }

        private int number() {
            int start = i;
            if (ch() == '-') i++;
            if (i >= raw.length() || raw.charAt(i) < '0' || raw.charAt(i) > '9') throw new IllegalArgumentException("плохое число");
            while (i < raw.length() && raw.charAt(i) >= '0' && raw.charAt(i) <= '9') i++;
            if (i < raw.length() && (raw.charAt(i) == '.' || raw.charAt(i) == 'e' || raw.charAt(i) == 'E')) {
                throw new IllegalArgumentException("только целые");
            }
            try {
                return Integer.parseInt(raw.substring(start, i));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("число вне диапазона");
            }
        }

        private void skip() {
            while (i < raw.length()) {
                char c = raw.charAt(i);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') i++;
                else break;
            }
        }

        private char ch() {
            if (i >= raw.length()) throw new IllegalArgumentException("неожиданный конец");
            return raw.charAt(i);
        }

        private boolean peek(char c) {
            return i < raw.length() && raw.charAt(i) == c;
        }

        private void expect(char c) {
            if (!peek(c)) throw new IllegalArgumentException("ожидалось " + c);
            i++;
        }
    }

    static final class Obj {
        final java.util.LinkedHashMap<String, Val> fields = new java.util.LinkedHashMap<String, Val>();
        boolean hasDuplicate;

        String string(String key) {
            Val val = fields.get(key);
            if (val == null || val.str == null) throw new IllegalArgumentException("поле " + key + " не строка");
            return val.str;
        }

        int integer(String key) {
            Val val = fields.get(key);
            if (val == null || val.num == null) throw new IllegalArgumentException("поле " + key + " не число");
            return val.num.intValue();
        }

        List<Val> array(String key) {
            Val val = fields.get(key);
            if (val == null || val.arr == null) throw new IllegalArgumentException("поле " + key + " не массив");
            return val.arr;
        }
    }

    static final class Val {
        final Obj obj;
        final List<Val> arr;
        final String str;
        final Integer num;

        private Val(Obj obj, List<Val> arr, String str, Integer num) {
            this.obj = obj;
            this.arr = arr;
            this.str = str;
            this.num = num;
        }

        static Val obj(Obj obj) { return new Val(obj, null, null, null); }
        static Val arr(List<Val> arr) { return new Val(null, arr, null, null); }
        static Val str(String str) { return new Val(null, null, str, null); }
        static Val num(int num) { return new Val(null, null, null, Integer.valueOf(num)); }
    }
}
