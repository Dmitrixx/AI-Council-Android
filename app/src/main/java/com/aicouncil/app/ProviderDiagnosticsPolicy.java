package com.aicouncil.app;

/**
 * Pure rules for provider connection / model-catalog diagnostic display.
 * UI and SharedPreferences stay in the Activity; this class decides messages and tones.
 */
public final class ProviderDiagnosticsPolicy {
    public static final String DEFAULT_CONNECTION_MESSAGE = "Не проверено";
    public static final String DEFAULT_MODELS_MESSAGE = "Список моделей не проверен";
    public static final String STALE_CONNECTION = "Параметры изменены · нужна перепроверка";
    public static final String STALE_DURING_TEST = "Параметры изменены во время проверки · запустите снова";
    public static final String NEED_CONNECTION_CHECK = "Нужна проверка подключения";
    public static final String STALE_MODELS = "URL изменён · загрузите список заново";
    public static final String STALE_MODELS_DURING = "URL изменён во время проверки · загрузите список заново";
    public static final String NEED_MODELS_REFRESH = "Нужно заново получить список моделей";

    /** Semantic tone for status prefix (independent of concrete palette ints). */
    public enum Tone {
        OK, ERROR, BUSY, NEUTRAL
    }

    public static final class Display {
        public final String message;
        public final Tone tone;
        public final boolean apply;

        public Display(String message, Tone tone, boolean apply) {
            this.message = message;
            this.tone = tone;
            this.apply = apply;
        }
    }

    private ProviderDiagnosticsPolicy() {}

    public static String statusPrefix(Tone tone) {
        if (tone == Tone.OK) return "✓  ";
        if (tone == Tone.ERROR) return "✕  ";
        if (tone == Tone.BUSY) return "↻  ";
        return "○  ";
    }

    /** Map palette color ints used by the shell to a semantic tone. */
    public static Tone toneForColor(int color, int teal, int red, int blue) {
        if (color == teal) return Tone.OK;
        if (color == red) return Tone.ERROR;
        if (color == blue) return Tone.BUSY;
        return Tone.NEUTRAL;
    }

    public static String connectionFingerprint(String endpoint, String model, String apiKey) {
        return ProviderConfigFingerprint.create(endpoint, model, apiKey);
    }

    /**
     * Catalog fingerprint uses endpoint + effective auth key (empty for local/LAN when
     * authorization must not be sent). Model is intentionally excluded.
     */
    public static String modelsCatalogFingerprint(String endpoint, String apiKey) {
        String normalized = endpoint == null ? "" : endpoint.trim().replaceAll("/+$", "");
        String configuredKey = apiKey == null ? "" : apiKey.trim();
        String effectiveKey = LocalAiPolicy.shouldSendAuthorization(normalized, configuredKey)
                ? configuredKey : "";
        return ProviderConfigFingerprint.create(normalized, "", effectiveKey);
    }

    /**
     * After a connection test finishes: show the result only if config is still the same.
     */
    public static Display afterConnectionTest(String testedFingerprint, String currentFingerprint,
                                              String message, boolean successTone) {
        if (testedFingerprint != null && testedFingerprint.equals(currentFingerprint)) {
            return new Display(message, successTone ? Tone.OK : Tone.ERROR, true);
        }
        return new Display(STALE_DURING_TEST, Tone.NEUTRAL, true);
    }

    public static Display afterModelsFetch(String testedCatalogFingerprint, String currentCatalogFingerprint,
                                           String message, boolean successTone) {
        if (testedCatalogFingerprint != null && testedCatalogFingerprint.equals(currentCatalogFingerprint)) {
            return new Display(message, successTone ? Tone.OK : Tone.ERROR, true);
        }
        return new Display(STALE_MODELS_DURING, Tone.NEUTRAL, true);
    }

    /**
     * Restore connection status from prefs when fields change.
     * {@code apply=false} means leave the UI alone (still default / no fingerprint).
     */
    public static Display refreshConnection(String testedFingerprint, String currentFingerprint,
                                            String savedMessage, boolean hadCustomMessage) {
        String tested = testedFingerprint == null ? "" : testedFingerprint;
        if (tested.isEmpty()) {
            if (hadCustomMessage) {
                return new Display(NEED_CONNECTION_CHECK, Tone.NEUTRAL, true);
            }
            return new Display(DEFAULT_CONNECTION_MESSAGE, Tone.NEUTRAL, false);
        }
        if (tested.equals(currentFingerprint)) {
            String msg = savedMessage == null || savedMessage.isEmpty()
                    ? DEFAULT_CONNECTION_MESSAGE : savedMessage;
            return new Display(msg, Tone.NEUTRAL, true);
        }
        return new Display(STALE_CONNECTION, Tone.NEUTRAL, true);
    }

    public static Display refreshModels(String catalogFingerprint, String currentCatalogFingerprint,
                                        String savedMessage, boolean hadCustomMessage) {
        String catalog = catalogFingerprint == null ? "" : catalogFingerprint;
        if (catalog.isEmpty()) {
            if (hadCustomMessage) {
                return new Display(NEED_MODELS_REFRESH, Tone.NEUTRAL, true);
            }
            return new Display(DEFAULT_MODELS_MESSAGE, Tone.NEUTRAL, false);
        }
        if (catalog.equals(currentCatalogFingerprint)) {
            String msg = savedMessage == null || savedMessage.isEmpty()
                    ? DEFAULT_MODELS_MESSAGE : savedMessage;
            return new Display(msg, Tone.NEUTRAL, true);
        }
        return new Display(STALE_MODELS, Tone.NEUTRAL, true);
    }

    public static boolean isDefaultConnectionMessage(String message) {
        return message == null || DEFAULT_CONNECTION_MESSAGE.equals(message);
    }

    public static boolean isDefaultModelsMessage(String message) {
        return message == null || DEFAULT_MODELS_MESSAGE.equals(message);
    }
}
