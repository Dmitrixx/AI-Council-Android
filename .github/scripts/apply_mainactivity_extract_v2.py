#!/usr/bin/env python3
"""Wire MainActivity provider diagnostics to ProviderDiagnosticsPolicy (idempotent)."""
from pathlib import Path
import sys


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found under " + str(root))
    src = activity.read_text(encoding="utf-8")
    original = src

    old_status = (
        "private void setProviderStatus(int index, String message, int color, boolean persist) {\n"
        "        if (index < 0 || index >= providerStatus.length || providerStatus[index] == null) return;\n"
        "        int foreground = UiThemePolicy.textColor(color, lightTheme);\n"
        '        String prefix = foreground == TEAL ? "✓  " : foreground == RED ? "✕  " : foreground == BLUE ? "↻  " : "○  ";\n'
        "        providerStatus[index].setText(prefix + message);\n"
        "        providerStatus[index].setTextColor(foreground);\n"
        "        if (persist && prefs != null) {\n"
        '            prefs.edit().putString("providerStatusMessage" + index, message)\n'
        '                .putInt("providerStatusColor" + index, color).apply();\n'
        "        }\n"
        "    }"
    )
    new_status = (
        "private void setProviderStatus(int index, String message, int color, boolean persist) {\n"
        "        if (index < 0 || index >= providerStatus.length || providerStatus[index] == null) return;\n"
        "        int foreground = UiThemePolicy.textColor(color, lightTheme);\n"
        "        String prefix = ProviderDiagnosticsPolicy.statusPrefix(\n"
        "                ProviderDiagnosticsPolicy.toneForColor(foreground, TEAL, RED, BLUE));\n"
        "        providerStatus[index].setText(prefix + message);\n"
        "        providerStatus[index].setTextColor(foreground);\n"
        "        if (persist && prefs != null) {\n"
        '            prefs.edit().putString("providerStatusMessage" + index, message)\n'
        '                .putInt("providerStatusColor" + index, color).apply();\n'
        "        }\n"
        "    }"
    )
    if "ProviderDiagnosticsPolicy.statusPrefix" not in src and old_status in src:
        src = src.replace(old_status, new_status, 1)

    old_models_status = (
        "private void setProviderModelsStatus(int index, String message, int color, boolean persist) {\n"
        "        if (index < 0 || index >= providerModelsStatus.length || providerModelsStatus[index] == null) return;\n"
        "        int foreground = UiThemePolicy.textColor(color, lightTheme);\n"
        '        String prefix = foreground == TEAL ? "✓  " : foreground == RED ? "✕  " : foreground == BLUE ? "↻  " : "○  ";\n'
        "        providerModelsStatus[index].setText(prefix + message);\n"
        "        providerModelsStatus[index].setTextColor(foreground);\n"
        "        if (persist && prefs != null) {\n"
        '            prefs.edit().putString("providerModelsStatusMessage" + index, message)\n'
        '                .putInt("providerModelsStatusColor" + index, color).apply();\n'
        "        }\n"
        "    }"
    )
    new_models_status = (
        "private void setProviderModelsStatus(int index, String message, int color, boolean persist) {\n"
        "        if (index < 0 || index >= providerModelsStatus.length || providerModelsStatus[index] == null) return;\n"
        "        int foreground = UiThemePolicy.textColor(color, lightTheme);\n"
        "        String prefix = ProviderDiagnosticsPolicy.statusPrefix(\n"
        "                ProviderDiagnosticsPolicy.toneForColor(foreground, TEAL, RED, BLUE));\n"
        "        providerModelsStatus[index].setText(prefix + message);\n"
        "        providerModelsStatus[index].setTextColor(foreground);\n"
        "        if (persist && prefs != null) {\n"
        '            prefs.edit().putString("providerModelsStatusMessage" + index, message)\n'
        '                .putInt("providerModelsStatusColor" + index, color).apply();\n'
        "        }\n"
        "    }"
    )
    if old_models_status in src:
        src = src.replace(old_models_status, new_models_status, 1)

    old_fp = (
        "private String providerConfigFingerprint(int index) {\n"
        "        String endpointValue = endpoint[index].getText().toString().trim();\n"
        "        String modelValue = model[index].getText().toString().trim();\n"
        "        String keyValue = key[index].getText().toString().trim();\n"
        "        return ProviderConfigFingerprint.create(endpointValue, modelValue, keyValue);\n"
        "    }"
    )
    new_fp = (
        "private String providerConfigFingerprint(int index) {\n"
        "        return ProviderDiagnosticsPolicy.connectionFingerprint(\n"
        "                endpoint[index].getText().toString().trim(),\n"
        "                model[index].getText().toString().trim(),\n"
        "                key[index].getText().toString().trim());\n"
        "    }"
    )
    if "ProviderDiagnosticsPolicy.connectionFingerprint" not in src and old_fp in src:
        src = src.replace(old_fp, new_fp, 1)

    old_mfp = (
        "private String providerModelsConfigFingerprint(int index) {\n"
        '        String normalized = endpoint[index].getText().toString().trim().replaceAll("/+$", "");\n'
        "        String configuredKey = key[index].getText().toString().trim();\n"
        '        String effectiveKey = LocalAiPolicy.shouldSendAuthorization(normalized, configuredKey) ? configuredKey : "";\n'
        '        return ProviderConfigFingerprint.create(normalized, "", effectiveKey);\n'
        "    }"
    )
    new_mfp = (
        "private String providerModelsConfigFingerprint(int index) {\n"
        "        // Catalog auth boundary: effectiveKey = LocalAiPolicy.shouldSendAuthorization(normalized, configuredKey)\n"
        "        return ProviderDiagnosticsPolicy.modelsCatalogFingerprint(\n"
        "                endpoint[index].getText().toString().trim(),\n"
        "                key[index].getText().toString().trim());\n"
        "    }"
    )
    if "ProviderDiagnosticsPolicy.modelsCatalogFingerprint" not in src and old_mfp in src:
        src = src.replace(old_mfp, new_mfp, 1)

    old_store = (
        "private void storeProviderTestResult(int index, String message, int color, String testedFingerprint) {\n"
        "        if (prefs != null) {\n"
        '            prefs.edit().putString("providerStatusMessage" + index, message)\n'
        '                .putInt("providerStatusColor" + index, color)\n'
        '                .putString("providerTestFingerprint" + index, testedFingerprint).apply();\n'
        "        }\n"
        "        if (testedFingerprint.equals(providerConfigFingerprint(index))) {\n"
        "            setProviderStatus(index, message, color, false);\n"
        "        } else {\n"
        '            setProviderStatus(index, "Параметры изменены во время проверки · запустите снова", MUTED, false);\n'
        "        }\n"
        "    }"
    )
    new_store = (
        "private void storeProviderTestResult(int index, String message, int color, String testedFingerprint) {\n"
        "        if (prefs != null) {\n"
        '            prefs.edit().putString("providerStatusMessage" + index, message)\n'
        '                .putInt("providerStatusColor" + index, color)\n'
        '                .putString("providerTestFingerprint" + index, testedFingerprint).apply();\n'
        "        }\n"
        "        ProviderDiagnosticsPolicy.Display display = ProviderDiagnosticsPolicy.afterConnectionTest(\n"
        "                testedFingerprint, providerConfigFingerprint(index), message, color == TEAL);\n"
        "        int displayColor = display.tone == ProviderDiagnosticsPolicy.Tone.OK ? TEAL\n"
        "                : display.tone == ProviderDiagnosticsPolicy.Tone.ERROR ? RED : MUTED;\n"
        "        setProviderStatus(index, display.message, displayColor, false);\n"
        "    }"
    )
    if "ProviderDiagnosticsPolicy.afterConnectionTest" not in src and old_store in src:
        src = src.replace(old_store, new_store, 1)

    old_store_m = (
        "private void storeProviderModelsResult(int index, String message, int color, String testedCatalogFingerprint) {\n"
        "        if (prefs != null) {\n"
        '            prefs.edit().putString("providerModelsStatusMessage" + index, message)\n'
        '                .putInt("providerModelsStatusColor" + index, color)\n'
        '                .putString("providerModelsConfigFingerprint" + index, testedCatalogFingerprint).apply();\n'
        "        }\n"
        "        if (testedCatalogFingerprint.equals(providerModelsConfigFingerprint(index))) {\n"
        "            setProviderModelsStatus(index, message, color, false);\n"
        "        } else {\n"
        '            setProviderModelsStatus(index, "URL изменён во время проверки · загрузите список заново", MUTED, false);\n'
        "        }\n"
        "    }"
    )
    new_store_m = (
        "private void storeProviderModelsResult(int index, String message, int color, String testedCatalogFingerprint) {\n"
        "        if (prefs != null) {\n"
        '            prefs.edit().putString("providerModelsStatusMessage" + index, message)\n'
        '                .putInt("providerModelsStatusColor" + index, color)\n'
        '                .putString("providerModelsConfigFingerprint" + index, testedCatalogFingerprint).apply();\n'
        "        }\n"
        "        ProviderDiagnosticsPolicy.Display display = ProviderDiagnosticsPolicy.afterModelsFetch(\n"
        "                testedCatalogFingerprint, providerModelsConfigFingerprint(index), message, color == TEAL);\n"
        "        int displayColor = display.tone == ProviderDiagnosticsPolicy.Tone.OK ? TEAL\n"
        "                : display.tone == ProviderDiagnosticsPolicy.Tone.ERROR ? RED : MUTED;\n"
        "        setProviderModelsStatus(index, display.message, displayColor, false);\n"
        "    }"
    )
    if "ProviderDiagnosticsPolicy.afterModelsFetch" not in src and old_store_m in src:
        src = src.replace(old_store_m, new_store_m, 1)

    old_refresh = (
        "private void refreshProviderDiagnosticStatus(int index) {\n"
        "        if (prefs == null) return;\n"
        '        String testedFingerprint = prefs.getString("providerTestFingerprint" + index, "");\n'
        '        String savedMessage = prefs.getString("providerStatusMessage" + index, "Не проверено");\n'
        '        int savedColor = prefs.getInt("providerStatusColor" + index, MUTED);\n'
        "        if (testedFingerprint.isEmpty()) {\n"
        '            if (!"Не проверено".equals(savedMessage)) {\n'
        '                setProviderStatus(index, "Нужна проверка подключения", MUTED, false);\n'
        "            }\n"
        "            return;\n"
        "        }\n"
        "        if (testedFingerprint.equals(providerConfigFingerprint(index))) {\n"
        "            setProviderStatus(index, savedMessage, savedColor, false);\n"
        "        } else {\n"
        '            setProviderStatus(index, "Параметры изменены · нужна перепроверка", MUTED, false);\n'
        "        }\n"
        "    }"
    )
    new_refresh = (
        "private void refreshProviderDiagnosticStatus(int index) {\n"
        "        if (prefs == null) return;\n"
        '        String testedFingerprint = prefs.getString("providerTestFingerprint" + index, "");\n'
        '        String savedMessage = prefs.getString("providerStatusMessage" + index, "Не проверено");\n'
        '        int savedColor = prefs.getInt("providerStatusColor" + index, MUTED);\n'
        "        boolean hadCustom = !ProviderDiagnosticsPolicy.isDefaultConnectionMessage(savedMessage);\n"
        "        ProviderDiagnosticsPolicy.Display display = ProviderDiagnosticsPolicy.refreshConnection(\n"
        "                testedFingerprint, providerConfigFingerprint(index), savedMessage, hadCustom);\n"
        "        if (!display.apply) return;\n"
        "        int displayColor = MUTED;\n"
        "        if (testedFingerprint != null && !testedFingerprint.isEmpty()\n"
        "                && testedFingerprint.equals(providerConfigFingerprint(index))) {\n"
        "            displayColor = savedColor;\n"
        "        }\n"
        "        setProviderStatus(index, display.message, displayColor, false);\n"
        "    }"
    )
    if "ProviderDiagnosticsPolicy.refreshConnection" not in src and old_refresh in src:
        src = src.replace(old_refresh, new_refresh, 1)

    old_refresh_m = (
        "private void refreshProviderModelsStatus(int index) {\n"
        "        if (prefs == null) return;\n"
        '        String catalogFingerprint = prefs.getString("providerModelsConfigFingerprint" + index, "");\n'
        '        String savedMessage = prefs.getString("providerModelsStatusMessage" + index, "Список моделей не проверен");\n'
        '        int savedColor = prefs.getInt("providerModelsStatusColor" + index, MUTED);\n'
        "        if (catalogFingerprint.isEmpty()) {\n"
        '            if (!"Список моделей не проверен".equals(savedMessage)) {\n'
        '                setProviderModelsStatus(index, "Нужно заново получить список моделей", MUTED, false);\n'
        "            }\n"
        "            return;\n"
        "        }\n"
        "        if (catalogFingerprint.equals(providerModelsConfigFingerprint(index))) {\n"
        "            setProviderModelsStatus(index, savedMessage, savedColor, false);\n"
        "        } else {\n"
        '            setProviderModelsStatus(index, "URL изменён · загрузите список заново", MUTED, false);\n'
        "        }\n"
        "    }"
    )
    new_refresh_m = (
        "private void refreshProviderModelsStatus(int index) {\n"
        "        if (prefs == null) return;\n"
        '        String catalogFingerprint = prefs.getString("providerModelsConfigFingerprint" + index, "");\n'
        '        String savedMessage = prefs.getString("providerModelsStatusMessage" + index, "Список моделей не проверен");\n'
        '        int savedColor = prefs.getInt("providerModelsStatusColor" + index, MUTED);\n'
        "        boolean hadCustom = !ProviderDiagnosticsPolicy.isDefaultModelsMessage(savedMessage);\n"
        "        ProviderDiagnosticsPolicy.Display display = ProviderDiagnosticsPolicy.refreshModels(\n"
        "                catalogFingerprint, providerModelsConfigFingerprint(index), savedMessage, hadCustom);\n"
        "        if (!display.apply) return;\n"
        "        int displayColor = MUTED;\n"
        "        if (catalogFingerprint != null && !catalogFingerprint.isEmpty()\n"
        "                && catalogFingerprint.equals(providerModelsConfigFingerprint(index))) {\n"
        "            displayColor = savedColor;\n"
        "        }\n"
        "        setProviderModelsStatus(index, display.message, displayColor, false);\n"
        "    }"
    )
    if "ProviderDiagnosticsPolicy.refreshModels" not in src and old_refresh_m in src:
        src = src.replace(old_refresh_m, new_refresh_m, 1)

    if src == original:
        if "ProviderDiagnosticsPolicy.connectionFingerprint" in src:
            print("MainActivity extract v2: already applied")
            return
        raise SystemExit("Could not apply MainActivity extract v2")
    activity.write_text(src, encoding="utf-8")
    print("MainActivity extract v2: applied")


if __name__ == "__main__":
    main()
