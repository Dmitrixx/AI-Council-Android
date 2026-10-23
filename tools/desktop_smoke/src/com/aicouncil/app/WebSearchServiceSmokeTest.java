package com.aicouncil.app;

import okhttp3.OkHttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Offline tests for the Activity-independent search service. No external network is used. */
public final class WebSearchServiceSmokeTest {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            WebSearchService service = new WebSearchService(pool, new WebSearchClient(), new WebPageFetchClient());
            String readable = service.htmlToReadableText("<html><body><h1>Alpha &amp; Beta</h1>"
                    + "<p>Value&nbsp;42 &#1046;</p><script>DO_NOT_INCLUDE</script>"
                    + "<style>.hidden{display:none}</style><nav>MENU</nav><p>End</p></body></html>");
            check("HTML extraction preserves readable text and decodes named/numeric entities",
                    readable.contains("Alpha & Beta") && readable.contains("Value 42 Ж") && readable.contains("End"));
            check("HTML extraction drops scripts, styles and navigation", !readable.contains("DO_NOT_INCLUDE")
                    && !readable.contains("display:none") && !readable.contains("MENU"));
            String snippet = service.decodeHtmlSnippet("<b>Привіт</b>&nbsp;&amp;&nbsp;<i>світ</i>");
            check("provider snippets become a single readable line", "Привіт & світ".equals(snippet));
            int before = OkHttpClient.callCount.get();
            String stopped = service.search("Пошук не должен запускаться", 1, "", "", () -> true, null);
            check("cancelled search exits before launching any network request",
                    stopped.isEmpty() && OkHttpClient.callCount.get() == before);
            String blank = service.search("   \n  ", 1, null, null, () -> false, null);
            check("empty search query exits before launching any network request",
                    blank.isEmpty() && OkHttpClient.callCount.get() == before);
            System.out.println("WebSearchServiceSmokeTest: 5/5 passed");
        } finally {
            pool.shutdownNow();
        }
    }

    private static void check(String label, boolean ok) {
        if (!ok) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
}
