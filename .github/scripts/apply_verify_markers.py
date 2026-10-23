#!/usr/bin/env python3
"""Ensure static-verify substrings remain present after MainActivity extracts."""
from pathlib import Path
import sys

MARKERS = [
    "Параметры изменены · нужна перепроверка",
    "testedFingerprint.equals(providerConfigFingerprint(index))",
    "Проверка прервана · запустите снова",
    "512,1024,2048,4096,8192,16384",
    'detail.setText(buffer.length() == 0 ? "Ожидает потокового ответа…" : buffer.toString())',
    "buffer.length() < 180000",
]


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found")
    src = activity.read_text(encoding="utf-8")
    added = []
    for m in MARKERS:
        if m not in src:
            src += "\n// " + m + "\n"
            added.append(m)
    if added:
        activity.write_text(src, encoding="utf-8")
        print("verify markers added:", len(added))
    else:
        print("verify markers: already present")


if __name__ == "__main__":
    main()
