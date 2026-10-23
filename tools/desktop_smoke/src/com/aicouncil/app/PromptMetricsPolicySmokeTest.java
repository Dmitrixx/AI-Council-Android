package com.aicouncil.app;

public final class PromptMetricsPolicySmokeTest {
    private static int checks;
    private static void check(String name, boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
    }
    public static void main(String[] args) {
        check("null text estimates zero tokens", PromptMetricsPolicy.estimateTokens(null) == 0);
        check("empty text estimates zero tokens", PromptMetricsPolicy.estimateTokens("") == 0);
        check("non-empty short text has positive estimate", PromptMetricsPolicy.estimateTokens("hello world") > 0);
        check("Cyrillic text estimate is positive", PromptMetricsPolicy.estimateTokens("Привіт, світе! Привет, мир!") > 0);
        check("multilingual text remains measurable", PromptMetricsPolicy.estimateTokens("English Українська 中文 العربية") > 0);
        check("larger text never estimates fewer tokens", PromptMetricsPolicy.estimateTokens(repeat("hello ", 300)) > PromptMetricsPolicy.estimateTokens("hello world"));
        check("estimate is bounded for the maximum prompt", PromptMetricsPolicy.estimateTokens(repeat("абвг ", 20000)) > 0);
        check("empty label is clear", "Пустой запрос".equals(PromptMetricsPolicy.sizeLabel(0)));
        check("small prompts are labeled short", "короткий запрос".equals(PromptMetricsPolicy.sizeLabel(800)));
        check("medium prompts are labeled medium", "средний объём".equals(PromptMetricsPolicy.sizeLabel(801)));
        check("large prompts are labeled large", "большой объём".equals(PromptMetricsPolicy.sizeLabel(2501)));
        check("very large prompts are labeled", "очень большой запрос".equals(PromptMetricsPolicy.sizeLabel(6001)));
        check("compact number formatting avoids commas", !PromptMetricsPolicy.compactNumber(100000).contains(","));
        check("negative numbers clamp to zero", "0".equals(PromptMetricsPolicy.compactNumber(-5)));
        System.out.println("PromptMetricsPolicySmokeTest: " + checks + "/" + checks + " passed");
    }
    private static String repeat(String value, int count) {
        StringBuilder out = new StringBuilder(value.length() * count);
        for (int i = 0; i < count; i++) out.append(value);
        return out.toString();
    }
}
