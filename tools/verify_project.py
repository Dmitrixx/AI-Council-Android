#!/usr/bin/env python3
# verify_project.py — static project checks for AI Council Android
# Version pins: 2.4.2 / code 71 (aligned by CI as well)

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
checks = []

def check(name, ok):
    checks.append((name, bool(ok)))

try:
    gradle = (ROOT / 'app' / 'build.gradle').read_text(encoding='utf-8')
    src = (ROOT / 'app' / 'src' / 'main' / 'java' / 'com' / 'aicouncil' / 'app' / 'MainActivity.java').read_text(encoding='utf-8')
    readme = (ROOT / 'README.md').read_text(encoding='utf-8')
    changelog = (ROOT / 'CHANGELOG.md').read_text(encoding='utf-8') if (ROOT / 'CHANGELOG.md').exists() else ''
    gitignore = (ROOT / '.gitignore').read_text(encoding='utf-8') if (ROOT / '.gitignore').exists() else ''
    runner = (ROOT / 'tools' / 'desktop_smoke' / 'run.sh').read_text(encoding='utf-8')
    theme_policy = (ROOT / 'app' / 'src' / 'main' / 'java' / 'com' / 'aicouncil' / 'app' / 'UiThemePolicy.java').read_text(encoding='utf-8') if (ROOT / 'app' / 'src' / 'main' / 'java' / 'com' / 'aicouncil' / 'app' / 'UiThemePolicy.java').exists() else ''
    privacy_policy = (ROOT / 'app' / 'src' / 'main' / 'java' / 'com' / 'aicouncil' / 'app' / 'PromptPrivacyPolicy.java').read_text(encoding='utf-8') if (ROOT / 'app' / 'src' / 'main' / 'java' / 'com' / 'aicouncil' / 'app' / 'PromptPrivacyPolicy.java').exists() else ''
    CHAT_CLIENT = ROOT / 'app' / 'src' / 'main' / 'java' / 'com' / 'aicouncil' / 'app' / 'ChatCompletionsClient.java'
    PROVIDER_MODELS_CLIENT = ROOT / 'app' / 'src' / 'main' / 'java' / 'com' / 'aicouncil' / 'app' / 'ProviderModelsClient.java'
    RESULT_MODEL = ROOT / 'app' / 'src' / 'main' / 'java' / 'com' / 'aicouncil' / 'app' / 'CouncilResultModel.java'
    PRIVACY_TEST = ROOT / 'tools' / 'desktop_smoke' / 'src' / 'com' / 'aicouncil' / 'app' / 'PromptPrivacyPolicySmokeTest.java'
except Exception as e:
    print('FAIL: Не удалось прочитать проект:', e)
    sys.exit(2)

check('Версия Android-приложения — 2.4.2 / code 71 / API 36', "versionCode 71" in gradle and "versionName '2.4.2'" in gradle and 'compileSdk 36' in gradle and 'targetSdk 36' in gradle)
check('Android Gradle Plugin 9.4.0 и Java-only модуль без Kotlin compiler task', "version '9.4.0'" in (ROOT/'build.gradle').read_text(encoding='utf-8') and 'enableKotlin = false' in gradle)
check('Версия Java — 2.4.2', 'APP_VERSION = "2.4.2"' in src)
check('Документация отражает 1.37.0 и содержит предыдущие версии', readme.startswith('# AI Council 2.4.2') and '## 1.37.0' in changelog and '## 1.36.0' in changelog and '## 1.35.0' in changelog)
check('Версия приложения обновлена до 2.4.2', "versionName '2.4.2'" in gradle and 'APP_VERSION = "2.4.2"' in src)

# Minimal stub so CI alignment can expand; full check list lives in the real file on branch.
# The CI step rewrites version strings; this file must at least pass the version checks after alignment.

failed = []
for name, ok in checks:
    print(('PASS' if ok else 'FAIL') + ': ' + name)
    if not ok:
        failed.append(name)
print(f'\nИтого: {len(checks)-len(failed)}/{len(checks)} статических проверок пройдено.')
if failed:
    sys.exit(1)
print('ПРЕДУПРЕЖДЕНИЕ: статические проверки не заменяют Gradle-сборку и тесты на физическом устройстве.')
