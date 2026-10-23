#!/usr/bin/env python3
"""Wire MainActivity voice/import messages to InputImportPolicy (idempotent)."""
from pathlib import Path
import sys

REPLACES = [
    ('setStatus("Голосовой ввод доступен после завершения текущего запроса.", RED);',
     'setStatus(InputImportPolicy.VOICE_BUSY, RED);'),
    ('intent.putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Продиктуйте задачу для AI Council");',
     'intent.putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, InputImportPolicy.VOICE_PROMPT);'),
    ('setStatus("На устройстве нет доступного сервиса распознавания речи. Можно установить или включить голосовой ввод Google.", RED);',
     'setStatus(InputImportPolicy.VOICE_NO_SERVICE, RED);'),
    ('setStatus("Не удалось открыть голосовой ввод: " + (e.getMessage() == null ? "неизвестная ошибка" : e.getMessage()), RED);',
     'setStatus(InputImportPolicy.voiceOpenError(e.getMessage()), RED);'),
    ('setStatus("Голосовой ввод отменён.", MUTED);',
     'setStatus(InputImportPolicy.VOICE_CANCELLED, MUTED);'),
    ('setStatus("Не удалось распознать речь. Попробуйте ещё раз или введите текст.", RED);',
     'setStatus(InputImportPolicy.VOICE_EMPTY, RED);'),
    ('.setTitle("Выберите распознанный текст")',
     '.setTitle(InputImportPolicy.VOICE_PICK_TITLE)'),
    ('setStatus("Голосовой ввод отменён, черновик не изменён.", MUTED)',
     'setStatus(InputImportPolicy.VOICE_CANCELLED_UNCHANGED, MUTED)'),
    ('setStatus("Голосовой текст добавлен в задачу. Проверьте его перед отправкой.", TEAL);',
     'setStatus(InputImportPolicy.VOICE_APPLIED, TEAL);'),
    ('setStatus("Распознанный текст превышает лимит задачи 100 000 символов и не добавлен.", RED);',
     'setStatus(InputImportPolicy.VOICE_TOO_LONG, RED);'),
    ('setStatus("Импорт текстового файла отменён.", MUTED);',
     'setStatus(InputImportPolicy.IMPORT_TEXT_CANCELLED, MUTED);'),
    ('setStatus("Импорт изображения отменён.", MUTED);',
     'setStatus(InputImportPolicy.IMPORT_IMAGE_CANCELLED, MUTED);'),
    ('setStatus("Импорт изображения для OCR кириллицы отменён.", MUTED);',
     'setStatus(InputImportPolicy.IMPORT_IMAGE_CYRILLIC_CANCELLED, MUTED);'),
    ('setStatus("Импорт сканированного PDF для OCR кириллицы отменён.", MUTED);',
     'setStatus(InputImportPolicy.IMPORT_PDF_CYRILLIC_CANCELLED, MUTED);'),
    ('setStatus("Импорт резервной копии отменён.", MUTED);',
     'setStatus(InputImportPolicy.IMPORT_BACKUP_CANCELLED, MUTED);'),
    ('setStatus("Импорт документа уже выполняется.", MUTED);',
     'setStatus(InputImportPolicy.IMPORT_ALREADY_RUNNING, MUTED);'),
    ('setStatus("Импорт документа уже выполняется.", RED);',
     'setStatus(InputImportPolicy.IMPORT_ALREADY_RUNNING, RED);'),
    ('setStatus("Загрузка модели или распознавание OCR отменено.", MUTED);',
     'setStatus(InputImportPolicy.OCR_CANCELLED, MUTED);'),
    ('setStatus("Загрузка модели или распознавание OCR PDF отменено.", MUTED);',
     'setStatus(InputImportPolicy.OCR_PDF_CANCELLED, MUTED);'),
    ('setStatus("Не удалось открыть выбор PDF: " + e.getMessage(), RED);',
     'setStatus(InputImportPolicy.pickerOpenError("PDF", e.getMessage()), RED);'),
    ('setStatus("Не удалось открыть выбор изображения: " + e.getMessage(), RED);',
     'setStatus(InputImportPolicy.pickerOpenError("изображения", e.getMessage()), RED);'),
]


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found")
    src = activity.read_text(encoding="utf-8")
    original = src
    for old, new in REPLACES:
        if old in src:
            src = src.replace(old, new)

    old_loop = (
        'String[] labels = new String[candidates.size()];\n'
        '                for (int i = 0; i < candidates.size(); i++) {\n'
        '                    String value = candidates.get(i).replaceAll("\\s+", " ").trim();\n'
        '                    labels[i] = (i == 0 ? "Лучший вариант · " : "Вариант " + (i + 1) + " · ") + value;\n'
        '                }'
    )
    new_loop = 'String[] labels = InputImportPolicy.candidateLabels(candidates);'
    if "InputImportPolicy.candidateLabels" not in src and old_loop in src:
        src = src.replace(old_loop, new_loop, 1)

    old_cancel = 'setStatus("Импорт отменён — задание не изменено.", MUTED)'
    if "InputImportPolicy.IMPORT_CANCELLED_UNCHANGED" not in src and old_cancel in src:
        src = src.replace(old_cancel, "setStatus(InputImportPolicy.IMPORT_CANCELLED_UNCHANGED, MUTED)")

    if src == original:
        if "InputImportPolicy.VOICE_BUSY" in src:
            print("MainActivity extract v8: already applied")
            return
        raise SystemExit("Could not apply MainActivity extract v8")
    activity.write_text(src, encoding="utf-8")
    print("MainActivity extract v8: applied")


if __name__ == "__main__":
    main()
