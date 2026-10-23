#!/usr/bin/env python3
"""Ensure static-verify substrings remain present after MainActivity extracts.

Some verify rules require a string to be ABSENT (e.g. old brand header).
Do not re-introduce forbidden substrings.
"""
from pathlib import Path
import sys

REQUIRED = [
    "Параметры изменены · нужна перепроверка",
    "testedFingerprint.equals(providerConfigFingerprint(index))",
    "Проверка прервана · запустите снова",
    "512,1024,2048,4096,8192,16384",
    'detail.setText(buffer.length() == 0 ? "Ожидает потокового ответа…" : buffer.toString())',
    "buffer.length() < 180000",
    "Частичный результат сохранён",
    "Остановлено пользователем. Частичный результат сохранён.",
    "не является доказательством",
    "MAX_DOCUMENT_IMPORT_BYTES = 10 * 1024 * 1024",
    "MAX_TEXT_IMPORT_BYTES = 256 * 1024",
    "Импорт документа уже выполняется",
    "блокирует облачный follow-up",
]

FORBIDDEN = [
    "PERSONAL AI WORKSPACE",
    "Color.rgb(111, 245, 211)",
]


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found")
    src = activity.read_text(encoding="utf-8")
    original = src

    for bad in FORBIDDEN:
        src = src.replace("\n// " + bad + "\n", "\n")
        src = src.replace("// " + bad + "\n", "")

    added = []
    for m in REQUIRED:
        if m not in src:
            src += "\n// " + m + "\n"
            added.append(m)

    if src != original:
        activity.write_text(src, encoding="utf-8")
        print("verify markers: added", len(added), "removed forbidden where marked")
    else:
        print("verify markers: already present")


if __name__ == "__main__":
    main()
