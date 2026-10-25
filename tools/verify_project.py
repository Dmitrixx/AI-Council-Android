#!/usr/bin/env python3
# verify_project.py — static project checks for AI Council Android
# Version pins: 2.5.0 / code 72 (aligned by CI as well)
# Minimal stub so CI alignment can expand; full check list lives in the real file on branch.

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
    changelog_ru = (ROOT / 'CHANGELOG_RU.md').read_text(encoding='utf-8') if (ROOT / 'CHANGELOG_RU.md').exists() else ''
    build_root = (ROOT / 'build.gradle').read_text(encoding='utf-8') if (ROOT / 'build.gradle').exists() else ''
except Exception as e:
    print('FAIL: Не удалось прочитать проект:', e)
    sys.exit(2)

check('Версия Android-приложения — 2.5.0 / code 72 / API 36',
      "versionCode 72" in gradle and "versionName '2.5.0'" in gradle and 'compileSdk 36' in gradle and 'targetSdk 36' in gradle)
check('Android Gradle Plugin и Java-only модуль без Kotlin compiler task',
      "version '9.4.0'" in build_root and 'enableKotlin = false' in gradle)
check('Версия Java — 2.5.0', 'APP_VERSION = "2.5.0"' in src)
check('README отражает 2.5.0', readme.startswith('# AI Council 2.5.0'))
check('CHANGELOG_RU содержит запись 2.5.0', '2.5.0' in changelog_ru)
check('Версия приложения обновлена до 2.5.0',
      "versionName '2.5.0'" in gradle and 'APP_VERSION = "2.5.0"' in src)

failed = []
for name, ok in checks:
    print(('PASS' if ok else 'FAIL') + ': ' + name)
    if not ok:
        failed.append(name)
print(f'\nИтого: {len(checks)-len(failed)}/{len(checks)} статических проверок пройдено.')
if failed:
    sys.exit(1)
print('ПРЕДУПРЕЖДЕНИЕ: статические проверки не заменяют Gradle-сборку и тесты на физическом устройстве.')
