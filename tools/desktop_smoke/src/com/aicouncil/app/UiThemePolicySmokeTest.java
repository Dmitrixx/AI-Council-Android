package com.aicouncil.app;

public final class UiThemePolicySmokeTest {
    private static int checks;
    private static void check(String name, boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
    }

    public static void main(String[] args) {
        check("system is the default for null", UiThemePolicy.SYSTEM.equals(UiThemePolicy.normalizeMode(null)));
        check("known light mode is normalized without case sensitivity", "light".equals(UiThemePolicy.normalizeMode("LIGHT")));
        check("known dark mode survives normalization", "dark".equals(UiThemePolicy.normalizeMode("dark")));
        check("unknown modes safely fall back to system", "system".equals(UiThemePolicy.normalizeMode("neon")));
        check("light preference overrides a dark system", UiThemePolicy.useLightTheme("light", true));
        check("dark preference overrides a light system", !UiThemePolicy.useLightTheme("dark", false));
        check("system mode follows light OS mode", UiThemePolicy.useLightTheme("system", false));
        check("system mode follows dark OS mode", !UiThemePolicy.useLightTheme("system", true));
        check("system option has index zero", UiThemePolicy.modeIndex("system") == 0);
        check("light option has index one", UiThemePolicy.modeIndex("light") == 1);
        check("dark option has index two", UiThemePolicy.modeIndex("dark") == 2);
        check("index one maps to light", "light".equals(UiThemePolicy.modeForIndex(1)));
        check("invalid index maps to system", "system".equals(UiThemePolicy.modeForIndex(-1)));
        check("dark background becomes a very light neutral surface", UiThemePolicy.surfaceColor(0xff080b16, true) == 0xfff5f7fb);
        check("dark card becomes white in light theme", UiThemePolicy.surfaceColor(0xff111728, true) == 0xffffffff);
        check("dark field becomes a light input surface", UiThemePolicy.surfaceColor(0xff182034, true) == 0xffeff3f8);
        check("brand teal CTA gradient remains bright", UiThemePolicy.surfaceColor(0xff83ffde, true) == 0xff83ffde);
        check("CTA gradient's teal end stop remains branded", UiThemePolicy.surfaceColor(0xff53d3c5, true) == 0xff53d3c5);
        check("brand mark teal end stop remains branded", UiThemePolicy.surfaceColor(0xff5dd3cd, true) == 0xff5dd3cd);
        check("surface mapping leaves dark theme colors unchanged", UiThemePolicy.surfaceColor(0xff080b16, false) == 0xff080b16);
        check("transparent backgrounds remain transparent in light theme", UiThemePolicy.surfaceColor(0x00000000, true) == 0x00000000);
        check("legacy light foreground becomes dark readable text", UiThemePolicy.textColor(0xfff2f6ff, true) == 0xff192335);
        check("teal status text becomes a readable deep teal", UiThemePolicy.textColor(0xff6ff5d3, true) == 0xff007c70);
        check("purple semantic text becomes a readable purple", UiThemePolicy.textColor(0xffb197ff, true) == 0xff6546b8);
        check("legacy light foreground is unchanged in dark theme", UiThemePolicy.textColor(0xfff2f6ff, false) == 0xfff2f6ff);
        check("muted text contrast on white exceeds WCAG AA for normal text", UiThemePolicy.contrastRatio(0xff5f6c80, 0xffffffff) >= 4.5);
        check("teal text contrast on white exceeds WCAG AA for normal text", UiThemePolicy.contrastRatio(0xff007c70, 0xffffffff) >= 4.5);
        check("purple text contrast on white exceeds WCAG AA for normal text", UiThemePolicy.contrastRatio(0xff6546b8, 0xffffffff) >= 4.5);
        check("dark borders become visible light borders", UiThemePolicy.strokeColor(0xff2b3751, true) == 0xffd6dfeb);
        System.out.println("UiThemePolicySmokeTest: " + checks + "/" + checks + " passed");
    }
}
