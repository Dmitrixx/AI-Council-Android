package com.aicouncil.app;

/** Pure Java theme selection and semantic color adaptation used by the native UI. */
public final class UiThemePolicy {
    public static final String SYSTEM = "system";
    public static final String LIGHT = "light";
    public static final String DARK = "dark";

    private UiThemePolicy() {}

    public static String normalizeMode(String mode) {
        if (LIGHT.equalsIgnoreCase(mode)) return LIGHT;
        if (DARK.equalsIgnoreCase(mode)) return DARK;
        return SYSTEM;
    }

    public static boolean useLightTheme(String mode, boolean systemNight) {
        String normalized = normalizeMode(mode);
        if (LIGHT.equals(normalized)) return true;
        if (DARK.equals(normalized)) return false;
        return !systemNight;
    }

    public static int modeIndex(String mode) {
        String normalized = normalizeMode(mode);
        if (LIGHT.equals(normalized)) return 1;
        if (DARK.equals(normalized)) return 2;
        return 0;
    }

    public static String modeForIndex(int index) {
        switch (index) {
            case 1: return LIGHT;
            case 2: return DARK;
            default: return SYSTEM;
        }
    }

    /** Maps legacy dark surfaces into quiet, near-neutral surfaces for the light palette. */
    public static int surfaceColor(int color, boolean light) {
        if (!light || (color >>> 24) == 0) return color;
        if (color == 0xffffffff || color == 0xfff5f7fb || color == 0xffeff3f8 || color == 0xffe9eff7) return color;
        if (color == 0xff5dd3cd || color == 0xff53d3c5) return color;
        if (color == 0xff080b16) return 0xfff5f7fb;
        if (color == 0xff1b2f4b) return 0xfff0f5fd;
        if (color == 0xff161d35) return 0xffe3ecf8;
        if (color == 0xff111728) return 0xffffffff;
        if (color == 0xff182034) return 0xffeff3f8;
        if (color == 0xff0c1221 || color == 0xff0c1222 || color == 0xff0c1223) return 0xfff7f9fc;

        double luminance = luminance(color);
        if (luminance > 0.70) return color; // preserve the bright teal brand and primary CTA
        int rgb = color & 0x00ffffff;
        int r = (rgb >> 16) & 0xff, g = (rgb >> 8) & 0xff, b = rgb & 0xff;
        float[] hsl = rgbToHsl(r, g, b);
        float hue = hsl[0];
        if (luminance < 0.19) {
            if (hue >= 155 && hue < 205) return 0xffe5f5f1;
            if (hue >= 245 && hue < 315) return 0xfff0ecfb;
            if (hue >= 195 && hue < 255) return luminance < 0.055 ? 0xffedf3fc : 0xffe6eef9;
            if (hue >= 330 || hue < 18) return 0xfffaecef;
            return 0xfff7f9fc;
        }
        if (luminance < 0.36) {
            if (hue >= 155 && hue < 205) return 0xffe2f3ef;
            if (hue >= 245 && hue < 315) return 0xffeee9fa;
            if (hue >= 195 && hue < 255) return 0xffe2eaf6;
            return 0xffeef2f7;
        }
        return 0xffe9eff7;
    }

    /** Strokes use a restrained border in light mode so dark-theme outlines don't dominate. */
    public static int strokeColor(int color, boolean light) {
        if (!light || color == 0) return color;
        if (color == 0xff2c5d59 || color == 0xff2c5d5a || color == 0xff2e625b || color == 0xff2c5d59) return 0xffb7dcd5;
        if (luminance(color) < 0.28) {
            float[] hsl = rgbToHsl((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff);
            if (hsl[0] >= 155 && hsl[0] < 205) return 0xffc5e2dc;
            if (hsl[0] >= 245 && hsl[0] < 315) return 0xffd9d0ef;
            return 0xffd6dfeb;
        }
        return color;
    }

    /** Makes dark-theme foregrounds legible on light surfaces while retaining semantic accents. */
    public static int textColor(int color, boolean light) {
        if (!light) return color;
        if (color == 0xff192335 || color == 0xff172033) return 0xff192335;
        if (color == 0xff5f6d84 || color == 0xff5f6c80) return 0xff5f6c80;
        if (color == 0xff007c70 || color == 0xff087f73 || color == 0xff007f73) return 0xff007c70;
        if (color == 0xff6648b7 || color == 0xff6546b8) return 0xff6546b8;
        if (color == 0xffb4233d || color == 0xffb4233c) return 0xffb4233d;
        if (color == 0xff275bab || color == 0xff275dad || color == 0xff2c61b5) return 0xff275bab;
        if (color == 0xfff2f6ff) return 0xff192335;
        if (color == 0xff96a4be || color == 0xffb9c7e0 || color == 0xffa4b7d6 || color == 0xffbac7e0) return 0xff5f6c80;
        if (color == 0xff6ff5d3 || color == 0xff83ffde || color == 0xff5dd3cd) return 0xff007c70;
        if (color == 0xffb197ff) return 0xff6546b8;
        if (color == 0xffff7e97) return 0xffb4233d;
        if (color == 0xff699dff) return 0xff275bab;
        if (luminance(color) > 0.42) return 0xff344158;
        if (luminance(color) < 0.08) return 0xff192335;
        return color;
    }

    public static double contrastRatio(int foreground, int background) {
        double a = luminance(foreground), b = luminance(background);
        double lighter = Math.max(a, b), darker = Math.min(a, b);
        return (lighter + 0.05) / (darker + 0.05);
    }

    private static double luminance(int color) {
        double r = channel((color >> 16) & 0xff), g = channel((color >> 8) & 0xff), b = channel(color & 0xff);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double channel(int value) {
        double v = value / 255.0;
        return v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    private static float[] rgbToHsl(int r, int g, int b) {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf)), min = Math.min(rf, Math.min(gf, bf));
        float h, s, l = (max + min) / 2f;
        if (max == min) { h = s = 0f; }
        else {
            float d = max - min;
            s = l > 0.5f ? d / (2f - max - min) : d / (max + min);
            if (max == rf) h = (gf - bf) / d + (gf < bf ? 6f : 0f);
            else if (max == gf) h = (bf - rf) / d + 2f;
            else h = (rf - gf) / d + 4f;
            h *= 60f;
        }
        return new float[]{h, s, l};
    }
}
