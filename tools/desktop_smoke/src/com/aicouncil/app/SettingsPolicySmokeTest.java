package com.aicouncil.app;

public final class SettingsPolicySmokeTest {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }

    public static void main(String[] args) {
        check("clamp low", SettingsPolicy.clampIndex(-1, 5) == 0);
        check("clamp high", SettingsPolicy.clampIndex(9, 5) == 4);
        check("mode clamp", SettingsPolicy.modeIndexFromPrefs(99) == 4
                && SettingsPolicy.modeIndexFromPrefs(1) == 1);
        check("token index 2048", SettingsPolicy.tokenLimitIndex(2048) == 2);
        check("token index unknown", SettingsPolicy.tokenLimitIndex(999) == 2);
        check("token at spinner", SettingsPolicy.tokenLimitAt(0) == 512
                && SettingsPolicy.tokenLimitAt(5) == 16384
                && SettingsPolicy.tokenLimitAt(99) == 16384);

        check("interrupted check",
                SettingsPolicy.INTERRUPTED_CONNECTION.equals(
                        SettingsPolicy.resolveConnectionStatusOnLoad("Проверяем…", "", "abc")));
        check("stale fingerprint",
                ProviderDiagnosticsPolicy.STALE_CONNECTION.equals(
                        SettingsPolicy.resolveConnectionStatusOnLoad("Ок", "aaa", "bbb")));
        check("keep saved",
                SettingsPolicy.resolveConnectionStatusOnLoad("Ок", "aaa", "aaa") == null);

        check("interrupted models",
                SettingsPolicy.INTERRUPTED_MODELS.equals(
                        SettingsPolicy.resolveModelsStatusOnLoad("Получаем список…", "", "")));
        check("stale models",
                ProviderDiagnosticsPolicy.STALE_MODELS.equals(
                        SettingsPolicy.resolveModelsStatusOnLoad("Найдено: 3", "x", "y")));
        check("legacy key", SettingsPolicy.isLegacyUnencryptedKey("sk-plain")
                && !SettingsPolicy.isLegacyUnencryptedKey("enc:abc")
                && !SettingsPolicy.isLegacyUnencryptedKey(""));

        System.out.println("SettingsPolicySmokeTest " + passed + "/" + (passed + failed));
        if (failed > 0) System.exit(1);
    }
}
