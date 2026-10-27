package com.aicouncil.app;

public final class ClaimGraphSmokeTest {
    private static int checks;

    private static void check(String name, boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
    }

    public static void main(String[] args) {
        String json = "{\"v\":1,\"claims\":["
                + "{\"id\":\"a1\",\"source\":\"1\",\"text\":\"Очередь стоит держать в Postgres.\",\"polarity\":\"for\",\"confidence\":70},"
                + "{\"id\":\"b1\",\"source\":\"2\",\"text\":\"Очередь не стоит держать в Postgres.\",\"polarity\":\"against\",\"confidence\":40}"
                + "],\"edges\":[{\"from\":\"a1\",\"to\":\"b1\",\"rel\":\"contradicts\"}]}";
        ClaimGraph.Graph graph = ClaimGraph.parse(json);
        check("parses two claims and one edge", graph.ok() && graph.claims.size() == 2 && graph.edges.size() == 1);
        check("note names the contradiction", ClaimGraph.noteForCoordinator(graph).contains("a1 ↔ b1"));

        ClaimGraph.Graph dup = ClaimGraph.parse(json.replace("\"b1\"", "\"a1\""));
        check("rejects duplicate ids", !dup.problems.isEmpty());

        String dupKey = "{\"v\":1,\"v\":1,\"claims\":[]}";
        check("rejects duplicate keys", !ClaimGraph.parse(dupKey).problems.isEmpty());

        String badConf = "{\"v\":1,\"claims\":[{\"id\":\"a1\",\"source\":\"1\",\"text\":\"Достаточно длинное утверждение для проверки границы.\",\"polarity\":\"for\",\"confidence\":140}]}";
        check("rejects confidence above 100", !ClaimGraph.parse(badConf).problems.isEmpty());

        check("empty input is a problem", !ClaimGraph.parse("").problems.isEmpty());
        check("trailing junk is rejected", !ClaimGraph.parse(json + " {}").problems.isEmpty());

        ClaimGraph.Graph prose = ClaimGraph.fromAnswers(
                "Рекомендую держать очередь в Postgres, потому что транзакции переживут сбой узла.",
                "Не рекомендую держать очередь в Postgres: один кластер здесь дороже пользы.");
        check("prose graph links a contradiction", hasRel(prose, "contradicts"));
        check("negated стоит is against", "against".equals(ClaimGraph.polarityOf("Не стоит запускать очередь в этом квартале без отката.")));
        check("plain стоит is for", "for".equals(ClaimGraph.polarityOf("Стоит запускать очередь после проверки отката на копии.")));

        System.out.println("ClaimGraphSmokeTest: " + checks + "/" + checks + " passed");
    }

    private static boolean hasRel(ClaimGraph.Graph graph, String rel) {
        for (ClaimGraph.Edge edge : graph.edges) if (rel.equals(edge.rel)) return true;
        return false;
    }
}
