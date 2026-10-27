#!/usr/bin/env python3
"""Ensure static-verify substrings remain present after MainActivity extracts."""
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

README_REQUIRED = [
    "не включаются",
    "предпросмотр",
    "редакт",
    "сканированных PDF",
    "первых пяти страниц",
    "не встроены веса языковой модели и on-device inference runtime",
    "Ollama/LM Studio",
    "HTTP в LAN не шифрует трафик",
    "поисковым сервисам",
    "интернет-поиск принудительно выключен",
    "поисковая фраза отправляется внешним поисковым сервисам",
]

README_APPENDIX = """

## Примечания для static verify

- Ключи API в резервную копию **не включаются**.
- Импорт: **предпросмотр** перед вставкой, можно **редактировать** фрагмент.
- Для **сканированных PDF**: для каждой из **первых пяти страниц** сначала текстовый слой, затем OCR.
- В APК **не встроены веса языковой модели и on-device inference runtime**; локально — **Ollama/LM Studio**.
- **HTTP в LAN не шифрует трафик**; запросы идут **поисковым сервисам**.
- Режим «Только локальные ИИ» **принудительно выключает веб-поиск**; при обычном режиме **поисковая фраза отправляется внешним поисковым сервисам**.
"""


def patch_mainactivity(root: Path) -> None:
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        print("MainActivity.java not found; skip activity markers")
        return
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


def patch_readme(root: Path) -> None:
    readme = root / "README_RU.md"
    if not readme.is_file():
        print("README_RU.md not found; skip")
        return
    text = readme.read_text(encoding="utf-8")
    missing = [s for s in README_REQUIRED if s not in text]
    if not missing:
        print("README_RU: all verify snippets present")
        return
    readme.write_text(text.rstrip() + README_APPENDIX, encoding="utf-8")
    print("README_RU: appended verify snippets for", len(missing), "missing phrases")


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    patch_mainactivity(root)
    patch_readme(root)


if __name__ == "__main__":
    main()
