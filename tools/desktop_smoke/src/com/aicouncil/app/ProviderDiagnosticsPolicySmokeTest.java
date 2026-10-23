package com.aicouncil.app;

public final class ProviderDiagnosticsPolicySmokeTest {
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
        check("prefix OK", "✓  ".equals(ProviderDiagnosticsPolicy.statusPrefix(ProviderDiagnosticsPolicy.Tone.OK)));
        check("prefix ERROR", "✕  ".equals(ProviderDiagnosticsPolicy.statusPrefix(ProviderDiagnosticsPolicy.Tone.ERROR)));
        check("prefix BUSY", "↻  ".equals(ProviderDiagnosticsPolicy.statusPrefix(ProviderDiagnosticsPolicy.Tone.BUSY)));
        check("prefix NEUTRAL", "○  ".equals(ProviderDiagnosticsPolicy.statusPrefix(ProviderDiagnosticsPolicy.Tone.NEUTRAL)));

        check("toneForColor",
                ProviderDiagnosticsPolicy.toneForColor(1, 1, 2, 3) == ProviderDiagnosticsPolicy.Tone.OK
                        && ProviderDiagnosticsPolicy.toneForColor(2, 1, 2, 3) == ProviderDiagnosticsPolicy.Tone.ERROR
                        && ProviderDiagnosticsPolicy.toneForColor(3, 1, 2, 3) == ProviderDiagnosticsPolicy.Tone.BUSY
                        && ProviderDiagnosticsPolicy.toneForColor(9, 1, 2, 3) == ProviderDiagnosticsPolicy.Tone.NEUTRAL);

        String fp1 = ProviderDiagnosticsPolicy.connectionFingerprint("https://api.x.ai/v1", "grok", "sk-test");
        String fp2 = ProviderDiagnosticsPolicy.connectionFingerprint("https://api.x.ai/v1", "grok", "sk-test");
        String fp3 = ProviderDiagnosticsPolicy.connectionFingerprint("https://api.x.ai/v1", "grok-2", "sk-test");
        check("connection fingerprint stable", fp1.equals(fp2) && !fp1.equals(fp3) && fp1.length() == 64);

        String localA = ProviderDiagnosticsPolicy.modelsCatalogFingerprint("http://127.0.0.1:11434/v1", "secret");
        String localB = ProviderDiagnosticsPolicy.modelsCatalogFingerprint("http://127.0.0.1:11434/v1/", "");
        check("local catalog ignores key and trailing slash", localA.equals(localB));

        ProviderDiagnosticsPolicy.Display ok =
                ProviderDiagnosticsPolicy.afterConnectionTest(fp1, fp1, "Подключено", true);
        check("after test match", ok.message.equals("Подключено") && ok.tone == ProviderDiagnosticsPolicy.Tone.OK);

        ProviderDiagnosticsPolicy.Display stale =
                ProviderDiagnosticsPolicy.afterConnectionTest(fp1, fp3, "Подключено", true);
        check("after test stale",
                ProviderDiagnosticsPolicy.STALE_DURING_TEST.equals(stale.message)
                        && stale.tone == ProviderDiagnosticsPolicy.Tone.NEUTRAL);

        ProviderDiagnosticsPolicy.Display refreshSame =
                ProviderDiagnosticsPolicy.refreshConnection(fp1, fp1, "Ок", true);
        check("refresh same config", "Ок".equals(refreshSame.message) && refreshSame.apply);

        ProviderDiagnosticsPolicy.Display refreshStale =
                ProviderDiagnosticsPolicy.refreshConnection(fp1, fp3, "Ок", true);
        check("refresh stale",
                ProviderDiagnosticsPolicy.STALE_CONNECTION.equals(refreshStale.message));

        ProviderDiagnosticsPolicy.Display refreshEmpty =
                ProviderDiagnosticsPolicy.refreshConnection("", fp1, "Не проверено", false);
        check("refresh empty no apply", !refreshEmpty.apply);

        ProviderDiagnosticsPolicy.Display refreshEmptyCustom =
                ProviderDiagnosticsPolicy.refreshConnection("", fp1, "Старый статус", true);
        check("refresh empty needs check",
                ProviderDiagnosticsPolicy.NEED_CONNECTION_CHECK.equals(refreshEmptyCustom.message)
                        && refreshEmptyCustom.apply);

        ProviderDiagnosticsPolicy.Display modelsStale =
                ProviderDiagnosticsPolicy.refreshModels("aaa", "bbb", "Найдено: 3", true);
        check("models refresh stale",
                ProviderDiagnosticsPolicy.STALE_MODELS.equals(modelsStale.message));

        System.out.println("ProviderDiagnosticsPolicySmokeTest " + passed + "/" + (passed + failed));
        if (failed > 0) System.exit(1);
    }
}
