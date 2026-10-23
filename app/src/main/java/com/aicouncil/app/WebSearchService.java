package com.aicouncil.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Search orchestration, result ranking and bounded page extraction kept out of MainActivity.
 * API credentials are supplied by the caller only for the explicitly enabled source.
 */
public final class WebSearchService {
    public interface Cancellation { boolean isCancelled(); }
    public interface StatusListener { void onStatus(String message); }

    private final ExecutorService pool;
    private final WebSearchClient webSearchClient;
    private final WebPageFetchClient webPageFetchClient;

    public WebSearchService(ExecutorService pool, WebSearchClient webSearchClient,
                           WebPageFetchClient webPageFetchClient) {
        if (pool == null || webSearchClient == null || webPageFetchClient == null) {
            throw new IllegalArgumentException("pool and HTTP clients are required");
        }
        this.pool = pool;
        this.webSearchClient = webSearchClient;
        this.webPageFetchClient = webPageFetchClient;
    }

    public void cancelAll() {
        webSearchClient.cancelAll();
        webPageFetchClient.cancelAll();
    }

    public String search(String rawQuery, int contextMode, String braveKey, String tavilyKey,
                         Cancellation cancellation, StatusListener statusListener) {
        final String query = buildSearchQuery(rawQuery);
        if (query.isEmpty() || isCancelled(cancellation)) return "";
        Future<String> brave = null, tavily = null, wiki = null, ddgInstant = null;
        try {
            final String usableBraveKey = braveKey == null ? "" : braveKey.trim();
        final String usableTavilyKey = tavilyKey == null ? "" : tavilyKey.trim();
            if (!usableBraveKey.isEmpty()) brave = pool.submit(() -> searchBrave(query, usableBraveKey, cancellation));
            if (!usableTavilyKey.isEmpty()) tavily = pool.submit(() -> searchTavily(query, usableTavilyKey, cancellation));
            wiki = pool.submit(() -> searchWikipedia(query, cancellation));
            ddgInstant = pool.submit(() -> searchDuckDuckGoInstant(query, cancellation));
            StringBuilder rawResults = new StringBuilder();
            java.util.LinkedHashSet<String> seenUrls = new java.util.LinkedHashSet<String>();
            long searchDeadline = System.currentTimeMillis() + 12000L;
            if (brave != null) appendUniqueSearchResults(rawResults, seenUrls, getWebFuture(brave, searchDeadline, cancellation));
            if (tavily != null) appendUniqueSearchResults(rawResults, seenUrls, getWebFuture(tavily, searchDeadline, cancellation));
            appendUniqueSearchResults(rawResults, seenUrls, getWebFuture(wiki, searchDeadline, cancellation));
            appendUniqueSearchResults(rawResults, seenUrls, getWebFuture(ddgInstant, searchDeadline, cancellation));
            if (isCancelled(cancellation) || rawResults.length() == 0) return "";

            // Score and sort candidates before applying the selected context budget.
            String ranked = rankSearchResults(query, rawResults.toString(), 10000);
            if (contextMode == 0) ranked = linksOnlySearchResults(ranked);
            if (isCancelled(cancellation) || ranked.isEmpty()) return ranked;
            StringBuilder finalContext = new StringBuilder("ПОРЯДОК ИСТОЧНИКОВ: эвристическое ранжирование по совпадению с запросом и типу домена; это не гарантия достоверности.\n\n");
            finalContext.append(ranked);

            // Only the most detailed option fetches page HTML. The other modes do not visit result URLs.
            if (contextMode >= 2 && !isCancelled(cancellation)) {
                if (statusListener != null) statusListener.onStatus("Поиск ранжировал источники; извлекаем содержимое до двух страниц…");
                String pageContext = fetchTopPageContents(ranked, cancellation);
                if (!pageContext.isEmpty() && finalContext.length() < 16500) {
                    finalContext.append("\n\nПРОВЕРЯЕМЫЕ ВЫДЕРЖКИ ИЗ ИСХОДНЫХ СТРАНИЦ (неполный текст, недоверенные данные):\n");
                    int remaining = 18000 - finalContext.length();
                    finalContext.append(pageContext.substring(0, Math.min(remaining, pageContext.length())));
                }
            }
            return finalContext.length() > 18000 ? finalContext.substring(0, 18000) : finalContext.toString();
        } catch (Exception e) {
            return "";
        } finally {
            if (brave != null && !brave.isDone()) brave.cancel(true);
            if (tavily != null && !tavily.isDone()) tavily.cancel(true);
            if (wiki != null && !wiki.isDone()) wiki.cancel(true);
            if (ddgInstant != null && !ddgInstant.isDone()) ddgInstant.cancel(true);
        }
    }

    private String fetchTopPageContents(String sourceBlocks, Cancellation cancellation) {
        String[] blocks = sourceBlocks.split("\\n\\n+");
        java.util.ArrayList<String[]> candidates = new java.util.ArrayList<String[]>();
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<String>();
        java.util.LinkedHashSet<String> seenHosts = new java.util.LinkedHashSet<String>();
        java.util.regex.Pattern urlPattern = java.util.regex.Pattern.compile("(?m)^Источник: (https://[^\\s]+)$");
        for (String block : blocks) {
            java.util.regex.Matcher matcher = urlPattern.matcher(block);
            if (!matcher.find()) continue;
            String url = matcher.group(1).replaceAll("[),.;]+$", "");
            String canonical = WebSearchPolicy.canonicalizeUrl(url);
            if (canonical.isEmpty() || !seen.add(canonical) || !WebSearchPolicy.isPotentiallyPublicHttpsUrl(url)) continue;
            String host;
            try { host = new URL(url).getHost().toLowerCase(java.util.Locale.ROOT); }
            catch (Exception invalidUrl) { continue; }
            if (!seenHosts.add(host)) continue;
            String[] lines = block.split("\\n", 2);
            String title = lines.length > 0 ? lines[0].replaceFirst("^\\[[^]]+\\]\\s*", "").trim() : url;
            candidates.add(new String[]{title, url});
            if (candidates.size() >= 2) break;
        }
        if (candidates.isEmpty() || isCancelled(cancellation)) return "";
        java.util.ArrayList<Future<String>> futures = new java.util.ArrayList<Future<String>>();
        for (String[] candidate : candidates) {
            final String title = candidate[0], url = candidate[1];
            futures.add(pool.submit(() -> fetchPageText(title, url, cancellation)));
        }
        StringBuilder out = new StringBuilder();
        long deadline = System.currentTimeMillis() + 7000L;
        for (Future<String> future : futures) {
            if (isCancelled(cancellation)) { future.cancel(true); continue; }
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) { future.cancel(true); continue; }
            try {
                String page = future.get(remaining, TimeUnit.MILLISECONDS);
                if (!page.isEmpty() && out.length() + page.length() < 6500) out.append(page).append("\n\n");
            } catch (Exception e) { future.cancel(true); }
        }
        for (Future<String> future : futures) if (!future.isDone()) future.cancel(true);
        return out.toString().trim();
    }

    private String fetchPageText(String searchTitle, String originalUrl, Cancellation cancellation) {
        try {
            WebPageFetchClient.PageResponse page = webPageFetchClient.fetchHtml(
                    originalUrl, () -> isCancelled(cancellation) || Thread.currentThread().isInterrupted());
            if (page == null || page.isEmpty() || isCancelled(cancellation)) return "";
            String html = page.html;
            String title = extractPageTitle(html);
            String date = extractPageDate(html);
            String text = htmlToReadableText(html);
            if (text.length() < 160) return "";
            if (text.length() > 2600) text = text.substring(0, 2600) + "…";
            StringBuilder out = new StringBuilder("ИСТОЧНИК: ")
                    .append(title.isEmpty() ? cleanSearchText(searchTitle, 180) : title)
                    .append("\nURL: ").append(page.finalUrl);
            out.append("\nДата публикации/изменения в метаданных (не подтверждена): ")
                    .append(date.isEmpty() ? "не найдена" : date);
            out.append("\nДата извлечения: ")
                    .append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.ROOT).format(new Date()));
            out.append("\nВыдержка (начало текста страницы; возможны пропуски):\n").append(text);
            return out.toString();
        } catch (Exception ignored) { return ""; }
    }


    private String extractPageTitle(String html) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?is)<title\\b[^>]*>(.*?)</title>").matcher(html);
        return matcher.find() ? cleanSearchText(matcher.group(1), 180) : "";
    }

    private String extractPageDate(String html) {
        java.util.regex.Pattern metaPattern = java.util.regex.Pattern.compile("(?is)<meta\\b([^>]+)>");
        java.util.regex.Matcher meta = metaPattern.matcher(html);
        while (meta.find()) {
            String tag = meta.group(1);
            java.util.regex.Matcher key = java.util.regex.Pattern.compile("(?i)(?:property|name|itemprop)\\s*=\\s*['\\\"](article:published_time|datePublished|dateCreated|dateModified|article:modified_time|pubdate|date|dc.date)\\s*['\\\"]").matcher(tag);
            if (!key.find()) continue;
            java.util.regex.Matcher content = java.util.regex.Pattern.compile("(?i)content\\s*=\\s*['\\\"]([^'\\\"]{1,80})['\\\"]").matcher(tag);
            if (content.find()) return cleanSearchText(content.group(1), 80);
        }
        java.util.regex.Matcher time = java.util.regex.Pattern.compile("(?is)<time\\b[^>]*datetime\\s*=\\s*['\\\"]([^'\\\"]{1,80})['\\\"]").matcher(html);
        return time.find() ? cleanSearchText(time.group(1), 80) : "";
    }

    String htmlToReadableText(String html) {
        if (html == null || html.isEmpty()) return "";
        String clean = html.replaceAll("(?is)<!--.*?-->", " ")
            .replaceAll("(?is)<(script|style|noscript|svg|template|nav|footer|header|aside|form|iframe|object|video|audio)\\b[^>]*>.*?</\\1\\s*>", " ")
            .replaceAll("(?is)<(script|style|noscript|svg|template|nav|footer|header|aside|form|iframe|object|video|audio)\\b[^>]*/\\s*>", " ")
            .replaceAll("(?is)<(br|p|div|li|tr|h[1-6]|article|section|blockquote)\\b[^>]*>", "\\n")
            .replaceAll("(?is)</(p|div|li|tr|h[1-6]|article|section|blockquote)>\\s*", "\\n");
        String text = decodeEntities(clean.replaceAll("(?is)<[^>]+>", " "));
        return text.replace('\u00a0', ' ').replaceAll("[\\r\\t]+", " ").replaceAll("[ ]{2,}", " ")
            .replaceAll(" *\\n *", "\\n").replaceAll("\\n{3,}", "\\n\\n").trim();
    }

    private String buildSearchQuery(String raw) { return WebSearchPolicy.buildSearchQuery(raw); }

    private String getWebFuture(Future<String> future, long deadlineMillis, Cancellation cancellation) {
        if (future == null) return "";
        long remaining = deadlineMillis - System.currentTimeMillis();
        if (remaining <= 0 && !future.isDone()) {
            future.cancel(true);
            // Search runs before model calls start; closing active search sockets enforces the shared deadline.
            webSearchClient.cancelAll();
            webPageFetchClient.cancelAll();
            return "";
        }
        try { return future.get(Math.max(1L, remaining), TimeUnit.MILLISECONDS); }
        catch (Exception e) {
            future.cancel(true);
            if (System.currentTimeMillis() >= deadlineMillis) {
                webSearchClient.cancelAll();
                webPageFetchClient.cancelAll();
            }
            return "";
        }
    }

    private String rankSearchResults(String query, String sourceBlocks, int maxChars) {
        if (sourceBlocks == null || sourceBlocks.trim().isEmpty()) return "";
        final class RankedBlock {
            final String text; final int score; final int order;
            RankedBlock(String text, int score, int order) { this.text = text; this.score = score; this.order = order; }
        }
        java.util.ArrayList<RankedBlock> ranked = new java.util.ArrayList<RankedBlock>();
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<String>();
        String[] blocks = sourceBlocks.split("\\n\\n+");
        int order = 0;
        for (String block : blocks) {
            if (block.trim().isEmpty()) continue;
            java.util.regex.Matcher urlMatcher = java.util.regex.Pattern.compile("(?m)^(?:Источник|URL): (https://[^\\s]+)$").matcher(block);
            if (!urlMatcher.find()) continue;
            String url = urlMatcher.group(1).replaceAll("[),.;]+$", "");
            String canonical = WebSearchPolicy.canonicalizeUrl(url);
            if (canonical.isEmpty() || !seen.add(canonical)) continue;
            String[] lines = block.split("\\n");
            String title = lines.length == 0 ? "" : lines[0].replaceFirst("^\\[[^]]+\\]\\s*", "");
            StringBuilder snippet = new StringBuilder();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty() || line.startsWith("Источник:") || line.startsWith("URL:") || line.startsWith("Дата страницы")) continue;
                if (snippet.length() > 0) snippet.append(' ');
                snippet.append(line);
            }
            String provider = "";
            java.util.regex.Matcher pm = java.util.regex.Pattern.compile("^\\[([^]]+)\\]").matcher(lines.length == 0 ? "" : lines[0]);
            if (pm.find()) provider = pm.group(1);
            int score = WebSearchPolicy.scoreSearchResult(query, title, snippet.toString(), url, provider);
            ranked.add(new RankedBlock(block.trim(), score, order++));
        }
        java.util.Collections.sort(ranked, (a, b) -> {
            int byScore = Integer.compare(b.score, a.score);
            return byScore != 0 ? byScore : Integer.compare(a.order, b.order);
        });
        StringBuilder out = new StringBuilder();
        for (RankedBlock block : ranked) {
            if (out.length() > 0 && out.length() + block.text.length() + 2 > maxChars) continue;
            if (block.text.length() > maxChars && out.length() == 0) out.append(block.text, 0, maxChars);
            else out.append(block.text);
            if (out.length() > 0 && out.charAt(out.length() - 1) != '\n') out.append("\n\n");
            if (out.length() >= maxChars) break;
        }
        return out.toString().trim();
    }

    private String linksOnlySearchResults(String rankedBlocks) {
        if (rankedBlocks == null || rankedBlocks.trim().isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        for (String block : rankedBlocks.split("\\n\\n+")) {
            if (block.trim().isEmpty()) continue;
            java.util.regex.Matcher source = java.util.regex.Pattern.compile("(?m)^Источник: (https://[^\\s]+)$").matcher(block);
            if (!source.find()) continue;
            String[] lines = block.split("\\n");
            String head = lines.length == 0 ? "" : lines[0].trim();
            if (head.isEmpty()) continue;
            if (out.length() > 0) out.append("\n\n");
            out.append(head).append("\nИсточник: ").append(source.group(1));
            if (out.length() >= 9000) break;
        }
        return out.toString();
    }

    private void appendUniqueSearchResults(StringBuilder output, java.util.Set<String> seen, String results) {
        if (results == null || results.trim().isEmpty()) return;
        String[] blocks = results.split("\\n\\n+");
        for (String block : blocks) {
            if (block.trim().isEmpty()) continue;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?m)^(?:Источник|URL): (https://[^\\s]+)$").matcher(block);
            if (!m.find()) continue;
            String url = m.group(1).replaceAll("[),.;]+$", "");
            String canonical = WebSearchPolicy.canonicalizeUrl(url);
            if (!WebSearchPolicy.isPotentiallyPublicHttpsUrl(url) || canonical.isEmpty() || !seen.add(canonical)) continue;
            if (output.length() + block.length() + 2 > 30000) break;
            output.append(block.trim()).append("\n\n");
        }
    }


    private String formatSearchItem(String provider, String title, String snippet, String url) {
        return formatSearchItem(provider, title, snippet, url, "");
    }

    private String formatSearchItem(String provider, String title, String snippet, String url, String pageAge) {
        if (!WebSearchPolicy.isPotentiallyPublicHttpsUrl(url)) return "";
        String safeTitle = cleanSearchText(title, 220);
        String safeSnippet = cleanSearchText(snippet, 550);
        String safeUrl = url.trim().replace("\n", "").replace("\r", "");
        if (safeTitle.isEmpty()) safeTitle = safeUrl;
        StringBuilder b = new StringBuilder("[").append(provider).append("] ").append(safeTitle);
        if (!safeSnippet.isEmpty()) b.append("\n").append(safeSnippet);
        if (pageAge != null && !pageAge.trim().isEmpty()) b.append("\nДата страницы по данным поиска: ").append(cleanSearchText(pageAge, 80));
        b.append("\nИсточник: ").append(safeUrl);
        return b.toString();
    }

    private String cleanSearchText(String raw, int limit) {
        if (raw == null) return "";
        String value = raw.replaceAll("(?is)<script[^>]*>.*?</script>", " ")
            .replaceAll("(?is)<style[^>]*>.*?</style>", " ")
            .replaceAll("<[^>]+>", " ")
            .replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"")
            .replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">")
            .replaceAll("[\\r\\n\\t]+", " ").replaceAll("\\s+", " ").trim();
        if (value.length() > limit) value = value.substring(0, limit) + "…";
        return value;
    }

    private String searchBrave(String query, String apiKey, Cancellation cancellation) {
        try {
            String lang = getSearchLanguage(query);
            String address = "https://api.search.brave.com/res/v1/web/search?q=" + URLEncoder.encode(query, "UTF-8")
                + "&count=8&country=UA&search_lang=" + lang + "&ui_lang=" + WebSearchPolicy.uiLanguageForQuery(query) + "&safesearch=moderate&spellcheck=true";
            String freshness = WebSearchPolicy.freshnessForQuery(query);
            if (!freshness.isEmpty()) address += "&freshness=" + URLEncoder.encode(freshness, "UTF-8");
            java.util.Map<String, String> headers = new java.util.HashMap<String, String>();
            headers.put("X-Subscription-Token", apiKey);
            String raw = webSearchClient.get(address, headers, 700000, () -> isCancelled(cancellation));
            if (raw.isEmpty() || isCancelled(cancellation)) return "";
            JSONObject json = new JSONObject(raw);
            JSONObject web = json.optJSONObject("web"); JSONArray arr = web == null ? null : web.optJSONArray("results");
            StringBuilder out = new StringBuilder();
            if (arr != null) for (int i=0; i<arr.length() && i<5; i++) {
                JSONObject item = arr.optJSONObject(i); if (item == null) continue;
                String age = item.optString("page_age", item.optString("page_fetched", ""));
                String formatted = formatSearchItem("Brave Search", item.optString("title", ""), item.optString("description", ""), item.optString("url", ""), age);
                if (!formatted.isEmpty()) out.append(formatted).append("\n\n");
                if (out.length() > 8000) break;
            }
            return out.toString();
        } catch (Exception e) { return ""; }
    }

    private String searchTavily(String query, String apiKey, Cancellation cancellation) {
        try {
            JSONObject request = new JSONObject();
            request.put("query", query);
            request.put("search_depth", "basic");
            request.put("max_results", 5);
            request.put("include_answer", false);
            request.put("include_raw_content", false);
            request.put("include_images", false);
            String freshness = WebSearchPolicy.freshnessForQuery(query);
            if (!freshness.isEmpty()) {
                request.put("topic", "news");
                String range = "pd".equals(freshness) ? "day" : "pw".equals(freshness) ? "week"
                        : "pm".equals(freshness) ? "month" : "year";
                request.put("time_range", range);
            } else request.put("topic", "general");
            java.util.Map<String, String> headers = new java.util.HashMap<String, String>();
            headers.put("Authorization", "Bearer " + apiKey);
            String raw = webSearchClient.postJson("https://api.tavily.com/search", request.toString(),
                    headers, 700000, () -> isCancelled(cancellation));
            if (raw.isEmpty() || isCancelled(cancellation)) return "";
            JSONArray arr = new JSONObject(raw).optJSONArray("results");
            StringBuilder out = new StringBuilder();
            if (arr != null) for (int i=0; i<arr.length() && i<5; i++) {
                JSONObject item = arr.optJSONObject(i); if (item == null) continue;
                String formatted = formatSearchItem("Tavily Search", item.optString("title", ""),
                        item.optString("content", ""), item.optString("url", ""), item.optString("published_date", ""));
                if (!formatted.isEmpty()) out.append(formatted).append("\n\n");
                if (out.length() > 8000) break;
            }
            return out.toString();
        } catch (Exception e) { return ""; }
    }

    private String getSearchLanguage(String query) { return WebSearchPolicy.languageForQuery(query); }

    private String searchWikipedia(String query, Cancellation cancellation) {
        try {
            String lang = getSearchLanguage(query);
            String address = "https://" + lang + ".wikipedia.org/w/api.php?action=query&list=search&srsearch="
                + URLEncoder.encode(query, "UTF-8") + "&srlimit=5&format=json&utf8=1";
            java.util.Map<String, String> headers = new java.util.HashMap<String, String>();
            headers.put("Api-User-Agent", "AI-Council-Android/2.0 (Android app)");
            String raw = webSearchClient.get(address, headers, 450000, () -> isCancelled(cancellation));
            if (raw.isEmpty() || isCancelled(cancellation)) return "";
            JSONObject json = new JSONObject(raw).optJSONObject("query");
            JSONArray arr = json == null ? null : json.optJSONArray("search");
            StringBuilder out = new StringBuilder();
            if (arr != null) for (int i=0; i<arr.length() && i<5; i++) {
                JSONObject item = arr.optJSONObject(i); if (item == null) continue;
                String title = item.optString("title", "");
                String snippet = decodeHtmlSnippet(item.optString("snippet", ""));
                String addressUrl = "https://" + lang + ".wikipedia.org/wiki/" + URLEncoder.encode(title, "UTF-8").replace("+", "_");
                String formatted = formatSearchItem("Wikipedia Search", title, snippet, addressUrl);
                if (!formatted.isEmpty()) out.append(formatted).append("\n\n");
            }
            return out.toString();
        } catch (Exception e) { return ""; }
    }

    private String searchDuckDuckGoInstant(String query, Cancellation cancellation) {
        try {
            String address = "https://api.duckduckgo.com/?q=" + URLEncoder.encode(query, "UTF-8") + "&format=json&no_html=1&skip_disambig=1";
            String raw = webSearchClient.get(address, java.util.Collections.<String, String>emptyMap(), 400000, () -> isCancelled(cancellation));
            if (raw.isEmpty() || isCancelled(cancellation)) return "";
            JSONObject json = new JSONObject(raw); StringBuilder out = new StringBuilder();
            String abstractText = json.optString("AbstractText", "");
            String abstractUrl = json.optString("AbstractURL", "");
            if (!abstractText.isEmpty() && abstractUrl.startsWith("https://")) {
                String formatted = formatSearchItem("DuckDuckGo Instant Answer", json.optString("Heading", "Краткая справка"), abstractText, abstractUrl);
                if (!formatted.isEmpty()) out.append(formatted).append("\n\n");
            }
            JSONArray topics = json.optJSONArray("RelatedTopics");
            if (topics != null) for (int i=0; i<topics.length() && out.length()<5000; i++) {
                JSONObject item = topics.optJSONObject(i);
                if (item != null && item.has("Topics")) {
                    JSONArray nested = item.optJSONArray("Topics");
                    if (nested != null) for (int j=0; j<nested.length() && out.length()<5000; j++) appendDuckTopic(out, nested.optJSONObject(j));
                } else appendDuckTopic(out, item);
            }
            return out.toString();
        } catch (Exception e) { return ""; }
    }

    private void appendDuckTopic(StringBuilder b, JSONObject item) {
        if (item == null || b.length() > 5000) return;
        String formatted = formatSearchItem("DuckDuckGo Instant Answer", item.optString("Text", ""), "", item.optString("FirstURL", ""));
        if (!formatted.isEmpty()) b.append(formatted).append("\n\n");
    }


    private static boolean isCancelled(Cancellation cancellation) {
        return Thread.currentThread().isInterrupted() || cancellation != null && cancellation.isCancelled();
    }

    /** Converts a provider's HTML snippet to plain text without relying on an Activity or SDK. */
    String decodeHtmlSnippet(String html) {
        String text = htmlToReadableText(html == null ? "" : html);
        return text.replaceAll("[\\r\\n\\t]+", " ").replaceAll("\\s+", " ").trim();
    }

    private static String decodeEntities(String value) {
        if (value == null || value.indexOf('&') < 0) return value == null ? "" : value;
        String decoded = value.replace("&nbsp;", " ").replace("&#160;", " ")
                .replace("&amp;", "&").replace("&quot;", "\"")
                .replace("&#39;", "'").replace("&apos;", "'")
                .replace("&lt;", "<").replace("&gt;", ">");
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("&#(x[0-9a-fA-F]{1,6}|[0-9]{1,7});").matcher(decoded);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String token = matcher.group(1);
            try {
                int codePoint = token.startsWith("x") || token.startsWith("X")
                        ? Integer.parseInt(token.substring(1), 16) : Integer.parseInt(token);
                if (!Character.isValidCodePoint(codePoint) || (codePoint >= 0xD800 && codePoint <= 0xDFFF)) codePoint = 0xFFFD;
                matcher.appendReplacement(out, java.util.regex.Matcher.quoteReplacement(new String(Character.toChars(codePoint))));
            } catch (Exception ignored) {
                matcher.appendReplacement(out, java.util.regex.Matcher.quoteReplacement(matcher.group()));
            }
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
