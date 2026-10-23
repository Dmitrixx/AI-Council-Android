package com.aicouncil.app;

import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;

/** Pure-Java helpers for privacy-bounded, freshness-aware web research. */
final class WebSearchPolicy {
    private WebSearchPolicy() {}

    static String buildSearchQuery(String raw) {
        if (raw == null) return "";
        String q = raw.replace('\u0000', ' ').replaceAll("[\\r\\n\\t]+", " ").replaceAll("\\s+", " ").trim();
        q = q.replaceAll("(?iu)(?<![\\p{L}])(знайди|знайти|найди|найти|ищи|поискать|исследуй|проанализируй|подготовь|составь|напиши|расскажи|объясни|сравни|проверь|проверить|пожалуйста|знайти в інтернеті|найди в интернете)(?![\\p{L}])", " ");
        q = q.replaceAll("\\s+", " ").replaceAll("^[,;:.!? —-]+|[,;:.!? —-]+$", "").trim();
        if (q.length() > 180) {
            int cut = q.lastIndexOf(' ', 180);
            q = q.substring(0, cut > 80 ? cut : 180).trim();
        }
        return q;
    }

    /** Brave Search freshness filter, deliberately applied only when user wording signals recency. */
    static String freshnessForQuery(String query) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
        if (containsAny(q, "latest", "breaking", "today", "right now", "current news", "last 24 hours", "сегодня", "сейчас", "зараз", "сьогодні", "останні новини", "срочно", "свіж", "актуальн", "на данный момент", "на даний момент", "станом на", "по состоянию на", "за последние 24 часа")) return "pd";
        if (containsAny(q, "this week", "past week", "last week", "за неделю", "на этой неделе", "за тиждень", "цього тижня", "минулий тиждень", "останні 7 днів", "последние 7 дней")) return "pw";
        if (containsAny(q, "this month", "past month", "last month", "за месяц", "за місяць", "в этом месяце", "цього місяця", "останні 30 днів", "последние 30 дней")) return "pm";
        if (containsAny(q, "this year", "past year", "last year", "за год", "за рік", "в этом году", "цього року", "последние 12 месяцев", "останні 12 місяців")) return "py";
        return "";
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) if (text.contains(needle)) return true;
        return false;
    }

    static String languageForQuery(String query) {
        String q = query == null ? "" : query;
        if (q.matches("(?s).*[іїєґІЇЄҐ].*")) return "uk";
        if (q.matches("(?s).*[а-яёА-ЯЁ].*")) return "ru";
        return "en";
    }

    static String uiLanguageForQuery(String query) {
        String language = languageForQuery(query);
        if ("uk".equals(language)) return "uk-UA";
        if ("ru".equals(language)) return "ru-RU";
        return "en-US";
    }

    /** Heuristic rank only: lexical query overlap plus a small domain-type prior; never a truth score. */
    static int scoreSearchResult(String query, String title, String snippet, String url, String provider) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
        String t = title == null ? "" : title.toLowerCase(Locale.ROOT);
        String s = snippet == null ? "" : snippet.toLowerCase(Locale.ROOT);
        int score = 0;
        java.util.LinkedHashSet<String> terms = new java.util.LinkedHashSet<String>();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[\\p{L}\\p{N}]{3,}").matcher(q);
        while (matcher.find()) {
            String term = matcher.group();
            if (!isCommonSearchWord(term)) terms.add(term);
        }
        for (String term : terms) {
            if (hasWholeToken(t, term)) score += 300;
            else if (t.contains(term)) score += 130;
            if (hasWholeToken(s, term)) score += 110;
            else if (s.contains(term)) score += 45;
        }
        String normalizedQuery = q.replaceAll("\\s+", " ").trim();
        if (normalizedQuery.length() >= 5 && t.contains(normalizedQuery)) score += 220;
        String host = "";
        try { host = new URL(url).getHost().toLowerCase(Locale.ROOT); } catch (Exception ignored) { }
        if (host.startsWith("www.")) host = host.substring(4);
        if (host.endsWith(".gov") || host.endsWith(".gov.ua") || host.endsWith(".gov.uk")
                || host.endsWith(".gov.au") || host.endsWith(".gouv.fr")) score += 230;
        else if (host.endsWith(".edu") || host.endsWith(".edu.ua") || host.endsWith(".ac.uk")
                || host.endsWith(".edu.au")) score += 160;
        else if (host.equals("who.int") || host.endsWith(".who.int") || host.equals("un.org")
                || host.endsWith(".un.org") || host.equals("europa.eu") || host.endsWith(".europa.eu")) score += 170;
        if ("Wikipedia Search".equals(provider)) score += 25;
        if ("Brave Search".equals(provider)) score += 10;
        return score;
    }

    private static boolean hasWholeToken(String text, String token) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?iu)(?<![\\p{L}\\p{N}])" + java.util.regex.Pattern.quote(token) + "(?![\\p{L}\\p{N}])").matcher(text);
        return m.find();
    }

    private static boolean isCommonSearchWord(String word) {
        return "the".equals(word) || "and".equals(word) || "for".equals(word) || "with".equals(word)
            || "from".equals(word) || "that".equals(word) || "this".equals(word) || "what".equals(word)
            || "when".equals(word) || "where".equals(word) || "how".equals(word) || "are".equals(word)
            || "was".equals(word) || "were".equals(word) || "the".equals(word) || "про".equals(word)
            || "для".equals(word) || "что".equals(word) || "как".equals(word) || "где".equals(word)
            || "когда".equals(word) || "это".equals(word) || "или".equals(word) || "при".equals(word)
            || "для".equals(word) || "как".equals(word) || "але".equals(word) || "для".equals(word)
            || "що".equals(word) || "коли".equals(word) || "цей".equals(word) || "ця".equals(word);
    }

    /** Syntax-only gate; MainActivity additionally checks resolved DNS addresses before downloading a page. */
    static boolean isPotentiallyPublicHttpsUrl(String value) {
        if (value == null || value.length() > 2048) return false;
        try {
            URL u = new URL(value.trim());
            String host = u.getHost();
            if (!"https".equalsIgnoreCase(u.getProtocol()) || host == null || host.isEmpty()
                    || u.getUserInfo() != null || host.endsWith(".")) return false;
            String h = host.toLowerCase(Locale.ROOT);
            if (h.equals("localhost") || h.endsWith(".localhost") || h.endsWith(".local")
                    || h.endsWith(".internal") || h.endsWith(".lan") || h.endsWith(".home")) return false;
            if (EndpointPolicy.isPrivateLocalHost(h) || isNonPublicIpv4Literal(h)) return false;
            if (h.indexOf(':') >= 0) {
                // URL's host is a literal here; resolve only colon-containing IPv6 literals, never domain names.
                java.net.InetAddress address = java.net.InetAddress.getByName(h.replace("[", "").replace("]", ""));
                if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress() || address.isMulticastAddress()) return false;
                byte[] bytes = address.getAddress();
                if (bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc) return false; // IPv6 unique-local fc00::/7
            }
            return true;
        } catch (Exception e) { return false; }
    }

    /** Rejects special-use IPv4 ranges even when they are not RFC1918 private ranges. */
    static boolean isNonPublicIpv4Literal(String host) {
        if (host == null || !host.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}")) return false;
        String[] p = host.split("\\.");
        int[] n = new int[4];
        try { for (int i=0;i<4;i++) { n[i]=Integer.parseInt(p[i]); if(n[i]>255)return true; } }
        catch (NumberFormatException e) { return true; }
        int a=n[0], b=n[1], c=n[2];
        return a==0 || a==10 || a==127 || (a==100 && b>=64 && b<=127)
            || (a==169 && b==254) || (a==172 && b>=16 && b<=31)
            || (a==192 && (b==168 || (b==0 && (c==0 || c==2))))
            || (a==198 && (b==18 || b==19 || (b==51 && c==100)))
            || (a==203 && b==0 && c==113) || a>=224;
    }

    /** Canonical duplicate key; removes fragments and common tracking parameters without altering the visible URL. */
    static String canonicalizeUrl(String value) {
        if (value == null) return "";
        try {
            URL u = new URL(value.trim());
            if (!"https".equalsIgnoreCase(u.getProtocol()) || u.getHost() == null || u.getHost().isEmpty()
                    || u.getUserInfo() != null) return "";
            String host = u.getHost().toLowerCase(Locale.ROOT);
            if (host.startsWith("[") && host.endsWith("]")) host = host.substring(1, host.length()-1);
            int port = u.getPort();
            String path = u.getPath(); if (path == null || path.isEmpty()) path = "/";
            if (path.length() > 1 && path.endsWith("/")) path = path.substring(0, path.length()-1);
            StringBuilder query = new StringBuilder();
            String rawQuery = u.getQuery();
            if (rawQuery != null && !rawQuery.isEmpty()) {
                for (String pair : rawQuery.split("&")) {
                    if (pair.isEmpty()) continue;
                    String rawName = pair.split("=", 2)[0]; String name;
                    try { name = URLDecoder.decode(rawName, "UTF-8").toLowerCase(Locale.ROOT); }
                    catch (Exception ignored) { name = rawName.toLowerCase(Locale.ROOT); }
                    if (name.startsWith("utm_") || name.equals("gclid") || name.equals("fbclid")
                            || name.equals("dclid") || name.equals("mc_cid") || name.equals("mc_eid")
                            || name.equals("ref") || name.equals("ref_src")) continue;
                    if (query.length() > 0) query.append('&'); query.append(pair);
                }
            }
            String result = "https://" + host + ((port > 0 && port != 443) ? ":" + port : "") + path;
            if (query.length() > 0) result += "?" + query;
            return result;
        } catch (Exception e) { return ""; }
    }
}
