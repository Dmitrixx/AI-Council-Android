package com.aicouncil.app;

/**
 * Pure rules for settings: token limits, spinner indices, and recovery of
 * interrupted provider diagnostic statuses. Persistence stays in the Activity.
 */
public final class SettingsPolicy {
    public static final int[] TOKEN_LIMITS = {512, 1024, 2048, 4096, 8192, 16384};
    public static final int DEFAULT_TOKEN_LIMIT = 2048;
    public static final int DEFAULT_MODE_INDEX = 1;
    public static final int DEFAULT_WEB_CONTEXT_INDEX = 2;
    public static final int MODE_COUNT = 5;

    public static final String CHECKING_MARKER = "Проверяем";
    public static final String FETCHING_MODELS_MARKER = "Получаем список";
    public static final String INTERRUPTED_CONNECTION = "Проверка прервана · запустите снова";
    public static final String INTERRUPTED_MODELS = "Запрос списка прерван · запустите снова";
    public static final String NEED_RECHECK_AFTER_UPDATE = "Нужна повторная проверка после обновления";
    public static final String SETTINGS_SAVED = "Настройки сохранены. API-ключи зашифрованы на устройстве.";

    private SettingsPolicy() {}

    public static int clampIndex(int index, int size) {
        if (size <= 0) return 0;
        if (index < 0) return 0;
        if (index >= size) return size - 1;
        return index;
    }

    public static int modeIndexFromPrefs(int saved) {
        return clampIndex(saved, MODE_COUNT);
    }

    public static int webContextIndexFromPrefs(int saved, int modeCount) {
        return clampIndex(saved, modeCount);
    }

    public static int tokenLimitIndex(int savedLimit) {
        for (int i = 0; i < TOKEN_LIMITS.length; i++) {
            if (TOKEN_LIMITS[i] == savedLimit) return i;
        }
        for (int i = 0; i < TOKEN_LIMITS.length; i++) {
            if (TOKEN_LIMITS[i] == DEFAULT_TOKEN_LIMIT) return i;
        }
        return clampIndex(2, TOKEN_LIMITS.length);
    }

    public static int tokenLimitAt(int spinnerIndex) {
        int i = clampIndex(spinnerIndex, TOKEN_LIMITS.length);
        return TOKEN_LIMITS[i];
    }

    public static String resolveConnectionStatusOnLoad(String lastStatus, String testedFingerprint,
                                                       String currentFingerprint) {
        String status = lastStatus == null ? "" : lastStatus;
        if (status.contains(CHECKING_MARKER)) {
            return INTERRUPTED_CONNECTION;
        }
        String tested = testedFingerprint == null ? "" : testedFingerprint;
        if (tested.isEmpty()) {
            if (!ProviderDiagnosticsPolicy.isDefaultConnectionMessage(status)
                    && !status.isEmpty()) {
                return NEED_RECHECK_AFTER_UPDATE;
            }
            return null;
        }
        if (!tested.equals(currentFingerprint == null ? "" : currentFingerprint)) {
            return ProviderDiagnosticsPolicy.STALE_CONNECTION;
        }
        return null;
    }

    public static String resolveModelsStatusOnLoad(String modelsStatus, String catalogFingerprint,
                                                   String currentCatalogFingerprint) {
        String status = modelsStatus == null ? "" : modelsStatus;
        if (status.contains(FETCHING_MODELS_MARKER)) {
            return INTERRUPTED_MODELS;
        }
        String catalog = catalogFingerprint == null ? "" : catalogFingerprint;
        if (!catalog.isEmpty()) {
            if (!catalog.equals(currentCatalogFingerprint == null ? "" : currentCatalogFingerprint)) {
                return ProviderDiagnosticsPolicy.STALE_MODELS;
            }
            return null;
        }
        if (!ProviderDiagnosticsPolicy.isDefaultModelsMessage(status) && !status.isEmpty()) {
            return ProviderDiagnosticsPolicy.NEED_MODELS_REFRESH;
        }
        return null;
    }

    public static boolean isLegacyUnencryptedKey(String saved) {
        return saved != null && !saved.isEmpty() && !saved.startsWith("enc:");
    }
}
