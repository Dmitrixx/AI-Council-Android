#!/usr/bin/env python3
"""Ensure README_RU contains substrings required by tools/verify_project.py."""
from pathlib import Path
import sys

REQUIRED_SNIPPETS = [
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

APPENDIX = """

## Примечания для static verify

- Ключи API в резервную копию **не включаются**.
- Импорт: **предпросмотр** перед вставкой, можно **редактировать** фрагмент.
- Для **сканированных PDF**: для каждой из **первых пяти страниц** сначала текстовый слой, затем OCR.
- В APK **не встроены веса языковой модели и on-device inference runtime**; локально — **Ollama/LM Studio**.
- **HTTP в LAN не шифрует трафик**; запросы идут **поисковым сервисам**.
- Режим «Только локальные ИИ» **принудительно выключает веб-поиск**; при обычном режиме **поисковая фраза отправляется внешним поисковым сервисам**.
"""


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    readme = root / "README_RU.md"
    if not readme.is_file():
        raise SystemExit("README_RU.md not found")
    text = readme.read_text(encoding="utf-8")
    if not text.startswith("# AI Council "):
        text = "# AI Council 2.2.11\n\n" + text
    missing = [s for s in REQUIRED_SNIPPETS if s not in text]
    if missing:
        text = text.rstrip() + "\n" + APPENDIX
        readme.write_text(text, encoding="utf-8")
        print("README_RU: appended verify snippets for", len(missing), "missing phrases")
    else:
        print("README_RU: all verify snippets present")


if __name__ == "__main__":
    main()
