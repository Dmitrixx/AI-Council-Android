#!/usr/bin/env python3
"""Wire MainActivity settings load/save to SettingsPolicy (idempotent)."""
from pathlib import Path
import sys


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found under " + str(root))
    src = activity.read_text(encoding="utf-8")
    original = src

    old = 'int[] limits={512,1024,2048,4096,8192,16384}; ed.putInt("maxTokens", limits[Math.max(0,Math.min(limits.length-1,tokenLimitSpinner.getSelectedItemPosition()))]);'
    new = 'ed.putInt("maxTokens", SettingsPolicy.tokenLimitAt(tokenLimitSpinner.getSelectedItemPosition()));'
    if "SettingsPolicy.tokenLimitAt" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'int[] limits={512,1024,2048,4096,8192,16384}; int savedLimit=prefs.getInt("maxTokens",2048), limitIndex=2; for(int i=0;i<limits.length;i++)if(limits[i]==savedLimit)limitIndex=i; tokenLimitSpinner.setSelection(limitIndex);'
    new = 'tokenLimitSpinner.setSelection(SettingsPolicy.tokenLimitIndex(prefs.getInt("maxTokens", SettingsPolicy.DEFAULT_TOKEN_LIMIT)));'
    if "SettingsPolicy.tokenLimitIndex" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'modeSpinner.setSelection(Math.min(4, Math.max(0, prefs.getInt("mode", 1))));'
    new = 'modeSpinner.setSelection(SettingsPolicy.modeIndexFromPrefs(prefs.getInt("mode", SettingsPolicy.DEFAULT_MODE_INDEX)));'
    if "SettingsPolicy.modeIndexFromPrefs" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'if (webContextModeSpinner != null) webContextModeSpinner.setSelection(Math.max(0, Math.min(WEB_CONTEXT_MODES.length - 1, prefs.getInt("webContextMode", 2))));'
    new = 'if (webContextModeSpinner != null) webContextModeSpinner.setSelection(SettingsPolicy.webContextIndexFromPrefs(prefs.getInt("webContextMode", SettingsPolicy.DEFAULT_WEB_CONTEXT_INDEX), WEB_CONTEXT_MODES.length));'
    if "SettingsPolicy.webContextIndexFromPrefs" not in src and old in src:
        src = src.replace(old, new, 1)

    old = (
        'boolean interrupted = lastStatus.contains("Проверяем");\n'
        '            if (interrupted) {\n'
        '                setProviderStatus(i, "Проверка прервана · запустите снова", MUTED, true);\n'
        '            } else {\n'
        '                String testedFingerprint = prefs.getString("providerTestFingerprint" + i, "");\n'
        '                if (testedFingerprint.isEmpty() && !"Не проверено".equals(lastStatus)) {\n'
        '                    setProviderStatus(i, "Нужна повторная проверка после обновления", MUTED, false);\n'
        '                } else if (!testedFingerprint.isEmpty() && !testedFingerprint.equals(providerConfigFingerprint(i))) {\n'
        '                    setProviderStatus(i, "Параметры изменены · нужна перепроверка", MUTED, false);\n'
        '                } else {\n'
        '                    setProviderStatus(i, lastStatus, lastColor, false);\n'
        '                }\n'
        '            }'
    )
    new = (
        'String testedFingerprint = prefs.getString("providerTestFingerprint" + i, "");\n'
        '            String resolved = SettingsPolicy.resolveConnectionStatusOnLoad(lastStatus, testedFingerprint, providerConfigFingerprint(i));\n'
        '            if (resolved != null) {\n'
        '                setProviderStatus(i, resolved, MUTED, resolved.equals(SettingsPolicy.INTERRUPTED_CONNECTION));\n'
        '            } else {\n'
        '                setProviderStatus(i, lastStatus, lastColor, false);\n'
        '            }'
    )
    if "SettingsPolicy.resolveConnectionStatusOnLoad" not in src and old in src:
        src = src.replace(old, new, 1)

    old_m = (
        'if (modelsStatus.contains("Получаем список")) {\n'
        '                setProviderModelsStatus(i, "Запрос списка прерван · запустите снова", MUTED, true);\n'
        '            } else {\n'
        '                String catalogFingerprint = prefs.getString("providerModelsConfigFingerprint" + i, "");\n'
        '                if (!catalogFingerprint.isEmpty() && !catalogFingerprint.equals(providerModelsConfigFingerprint(i))) {\n'
        '                    setProviderModelsStatus(i, "URL изменён · загрузите список заново", MUTED, false);\n'
        '                } else if (catalogFingerprint.isEmpty() && !"Список моделей не проверен".equals(modelsStatus)) {\n'
        '                    setProviderModelsStatus(i, "Нужно заново получить список моделей", MUTED, false);\n'
        '                } else {\n'
        '                    setProviderModelsStatus(i, modelsStatus, modelsColor, false);\n'
        '                }\n'
        '            }'
    )
    new_m = (
        'String catalogFingerprint = prefs.getString("providerModelsConfigFingerprint" + i, "");\n'
        '            String resolvedModels = SettingsPolicy.resolveModelsStatusOnLoad(modelsStatus, catalogFingerprint, providerModelsConfigFingerprint(i));\n'
        '            if (resolvedModels != null) {\n'
        '                setProviderModelsStatus(i, resolvedModels, MUTED, resolvedModels.equals(SettingsPolicy.INTERRUPTED_MODELS));\n'
        '            } else {\n'
        '                setProviderModelsStatus(i, modelsStatus, modelsColor, false);\n'
        '            }'
    )
    if "SettingsPolicy.resolveModelsStatusOnLoad" not in src and old_m in src:
        src = src.replace(old_m, new_m, 1)

    old = 'if (!saved.isEmpty() && !saved.startsWith("enc:")) legacy = true;'
    new = 'if (SettingsPolicy.isLegacyUnencryptedKey(saved)) legacy = true;'
    if "SettingsPolicy.isLegacyUnencryptedKey" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'ed.apply(); setStatus("Настройки сохранены. API-ключи зашифрованы на устройстве.", TEAL); return true;'
    new = 'ed.apply(); setStatus(SettingsPolicy.SETTINGS_SAVED, TEAL); return true;'
    if "SettingsPolicy.SETTINGS_SAVED" not in src and old in src:
        src = src.replace(old, new, 1)

    if src == original:
        if "SettingsPolicy.tokenLimitAt" in src:
            print("MainActivity extract v5: already applied")
            return
        raise SystemExit("Could not apply MainActivity extract v5")
    activity.write_text(src, encoding="utf-8")
    print("MainActivity extract v5: applied")


if __name__ == "__main__":
    main()
