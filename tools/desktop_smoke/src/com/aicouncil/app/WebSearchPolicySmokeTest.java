package com.aicouncil.app;

public final class WebSearchPolicySmokeTest {
    public static void main(String[] args) {
        check("null query empty", WebSearchPolicy.buildSearchQuery(null).isEmpty());
        check("trim and normalize whitespace", "погода у Львові".equals(WebSearchPolicy.buildSearchQuery("  погода\n\t у   Львові  ")));
        check("instruction verb removed", "історія Києва".equals(WebSearchPolicy.buildSearchQuery("Знайди історія Києва")));
        check("query capped at 180 chars", WebSearchPolicy.buildSearchQuery(repeat("слово ", 80)).length() <= 180);
        check("Ukrainian language chosen", "uk".equals(WebSearchPolicy.languageForQuery("історія Києва")));
        check("Russian language chosen", "ru".equals(WebSearchPolicy.languageForQuery("история Киева")));
        check("English language chosen", "en".equals(WebSearchPolicy.languageForQuery("history of Kyiv")));
        check("Ukrainian UI locale chosen", "uk-UA".equals(WebSearchPolicy.uiLanguageForQuery("історія Києва")));
        check("Russian UI locale chosen", "ru-RU".equals(WebSearchPolicy.uiLanguageForQuery("история Москвы")));
        check("NUL removed", !WebSearchPolicy.buildSearchQuery("hello\u0000world").contains("\u0000"));
        check("today intent uses 24-hour freshness", "pd".equals(WebSearchPolicy.freshnessForQuery("latest news today")));
        check("Ukrainian last-week intent recognized", "pw".equals(WebSearchPolicy.freshnessForQuery("новини за тиждень")));
        check("Russian last-month intent recognized", "pm".equals(WebSearchPolicy.freshnessForQuery("новости за месяц")));
        check("year intent recognized", "py".equals(WebSearchPolicy.freshnessForQuery("AI trends this year")));
        check("ordinary query does not force freshness", WebSearchPolicy.freshnessForQuery("how photosynthesis works").isEmpty());
        check("public HTTPS URL passes syntax gate", WebSearchPolicy.isPotentiallyPublicHttpsUrl("https://example.com/article"));
        check("private HTTP URL rejected", !WebSearchPolicy.isPotentiallyPublicHttpsUrl("http://192.168.1.1/admin"));
        check("localhost URL rejected", !WebSearchPolicy.isPotentiallyPublicHttpsUrl("https://localhost/secret"));
        check("user-info URL rejected", !WebSearchPolicy.isPotentiallyPublicHttpsUrl("https://u:p@example.com/"));
        check("IPv4 loopback rejected", !WebSearchPolicy.isPotentiallyPublicHttpsUrl("https://127.0.0.1/"));
        check("link-local and test-net IPv4 blocked", WebSearchPolicy.isNonPublicIpv4Literal("169.254.10.3") && WebSearchPolicy.isNonPublicIpv4Literal("203.0.113.8"));
        check("canonical duplicate removes tracking and fragment", "https://example.com/news?id=7".equals(WebSearchPolicy.canonicalizeUrl("https://EXAMPLE.com/news/?id=7&utm_source=feed#section")));
        check("canonical duplicate refuses non-HTTPS URL", WebSearchPolicy.canonicalizeUrl("http://example.com/a").isEmpty());
        int titleScore = WebSearchPolicy.scoreSearchResult("Ukraine energy policy", "Ukraine energy policy overview", "general information", "https://example.com/news", "DuckDuckGo Web Search");
        int snippetScore = WebSearchPolicy.scoreSearchResult("Ukraine energy policy", "Overview", "Ukraine energy policy is discussed here", "https://example.com/news2", "DuckDuckGo Web Search");
        check("title relevance scores higher than snippet-only match", titleScore > snippetScore);
        int officialScore = WebSearchPolicy.scoreSearchResult("Ukraine energy policy", "Overview", "general information", "https://energy.gov.ua/report", "DuckDuckGo Web Search");
        int ordinaryScore = WebSearchPolicy.scoreSearchResult("Ukraine energy policy", "Overview", "general information", "https://example.com/report", "DuckDuckGo Web Search");
        check("official-domain prior modestly increases heuristic rank", officialScore > ordinaryScore);
        check("ranking remains neutral to empty metadata without crashing", WebSearchPolicy.scoreSearchResult("???", "", "", "https://example.com/", "") >= 0);
        System.out.println("Web search policy smoke tests passed: 26/26");
    }
    private static String repeat(String word, int count) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < count; i++) b.append(word);
        return b.toString();
    }
    private static void check(String label, boolean ok) {
        if (!ok) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
}
