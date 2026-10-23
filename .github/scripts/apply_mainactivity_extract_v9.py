#!/usr/bin/env python3
"""Wire MainActivity backup/export rules to BackupExportPolicy (idempotent)."""
from pathlib import Path
import sys

def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found")
    src = activity.read_text(encoding="utf-8")
    original = src

    replaces = [
        ("private static final int MAX_BACKUP_BYTES = 5 * 1024 * 1024;",
         "private static final int MAX_BACKUP_BYTES = BackupExportPolicy.MAX_BACKUP_BYTES; // 5 * 1024 * 1024"),
        ("private static final int MAX_TEXT_IMPORT_BYTES = 256 * 1024;",
         "private static final int MAX_TEXT_IMPORT_BYTES = BackupExportPolicy.MAX_TEXT_IMPORT_BYTES; // MAX_TEXT_IMPORT_BYTES = 256 * 1024"),
        ("private static final int MAX_DOCUMENT_IMPORT_BYTES = 10 * 1024 * 1024;",
         "private static final int MAX_DOCUMENT_IMPORT_BYTES = BackupExportPolicy.MAX_DOCUMENT_IMPORT_BYTES; // MAX_DOCUMENT_IMPORT_BYTES = 10 * 1024 * 1024"),
        ("int taskLimit = 30000, answerLimit = 100000, dialogueLimit = 60000;",
         "int taskLimit = BackupExportPolicy.DEFAULT_TASK_LIMIT, answerLimit = BackupExportPolicy.DEFAULT_ANSWER_LIMIT, dialogueLimit = BackupExportPolicy.DEFAULT_DIALOGUE_LIMIT;"),
        ('setStatus("Не удалось подготовить копию до 5 МБ. Удалите лишние большие проекты и повторите экспорт.", RED);',
         "setStatus(BackupExportPolicy.PREPARE_TOO_LARGE, RED);"),
        ('setStatus("Не удалось подготовить резервную копию: " + e.getMessage(), RED);',
         "setStatus(BackupExportPolicy.prepareFailed(e.getMessage()), RED);"),
        ("if (taskLimit <= 750 && answerLimit <= 3000 && dialogueLimit <= 1500) {",
         "if (!BackupExportPolicy.canShrinkFurther(taskLimit, answerLimit, dialogueLimit)) {"),
        ("taskLimit = Math.max(750, (int)(taskLimit * 0.65));",
         "taskLimit = BackupExportPolicy.nextTaskLimit(taskLimit);"),
        ("answerLimit = Math.max(3000, (int)(answerLimit * 0.65));",
         "answerLimit = BackupExportPolicy.nextAnswerLimit(answerLimit);"),
        ("dialogueLimit = Math.max(1500, (int)(dialogueLimit * 0.65));",
         "dialogueLimit = BackupExportPolicy.nextDialogueLimit(dialogueLimit);"),
        ('intent.putExtra(android.content.Intent.EXTRA_TITLE, "AI_Council_backup_" + timestamp + ".json");',
         "intent.putExtra(android.content.Intent.EXTRA_TITLE, BackupExportPolicy.backupFileTitle(timestamp));"),
        ('setStatus("Читаем резервную копию…", BLUE);',
         "setStatus(BackupExportPolicy.READING_BACKUP, BLUE);"),
        ('setStatus("Сохранение отменено.", MUTED);',
         "setStatus(BackupExportPolicy.SAVE_CANCELLED, MUTED);"),
        ('setStatus(pendingBackupTrimmed ? "Резервная копия сохранена до 5 МБ. Часть очень длинных полей сокращена; API-ключи не включены." : "Резервная копия сохранена. API-ключи в неё не включены.", TEAL);',
         "setStatus(BackupExportPolicy.savedMessage(pendingBackupTrimmed), TEAL);"),
        ('setStatus("Сначала получите или откройте результат для сохранения.", RED);',
         "setStatus(BackupExportPolicy.NEED_RESULT_SAVE, RED);"),
        ('setStatus("Сначала получите или откройте результат для экспорта.", RED);',
         "setStatus(BackupExportPolicy.NEED_RESULT_EXPORT, RED);"),
        ('setStatus("●  Результат скопирован", TEAL);',
         "setStatus(BackupExportPolicy.RESULT_COPIED, TEAL);"),
        ('setStatus("Ответ из выбранного блока скопирован.", TEAL);',
         "setStatus(BackupExportPolicy.BLOCK_COPIED, TEAL);"),
        ('setStatus("Откройте приложение для отправки или сохранения результата.", MUTED);',
         "setStatus(BackupExportPolicy.OPEN_SHARE, MUTED);"),
        ("return cleanBackupEntries(source, isHistory, limit, 30000, 100000, 60000);",
         "return cleanBackupEntries(source, isHistory, limit, BackupExportPolicy.DEFAULT_TASK_LIMIT, BackupExportPolicy.DEFAULT_ANSWER_LIMIT, BackupExportPolicy.DEFAULT_DIALOGUE_LIMIT);"),
    ]
    for old, new in replaces:
        if old in src:
            src = src.replace(old, new)

    old_rf = (
        "private String resultFileBase(String task) {\n"
        '        String base = task == null || task.trim().isEmpty() ? "AI_Council_result"\n'
        '                : task.replaceAll("[^a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ0-9_-]+", "_");\n'
        "        if (base.length() > 48) base = base.substring(0, 48);\n"
        '        if (base.isEmpty()) base = "AI_Council_result";\n'
        "        return base;\n"
        "    }"
    )
    new_rf = (
        "private String resultFileBase(String task) {\n"
        "        return BackupExportPolicy.resultFileBase(task);\n"
        "    }"
    )
    if "BackupExportPolicy.resultFileBase" not in src and old_rf in src:
        src = src.replace(old_rf, new_rf, 1)

    if "pendingExportText.getBytes(StandardCharsets.UTF_8).length <= MAX_BACKUP_BYTES" in src:
        src = src.replace(
            "pendingExportText.getBytes(StandardCharsets.UTF_8).length <= MAX_BACKUP_BYTES",
            "BackupExportPolicy.fitsBackupSize(pendingExportText.getBytes(StandardCharsets.UTF_8).length)",
            1,
        )

    if src == original:
        if "BackupExportPolicy.MAX_BACKUP_BYTES" in src:
            print("MainActivity extract v9: already applied")
            return
        raise SystemExit("Could not apply MainActivity extract v9")
    activity.write_text(src, encoding="utf-8")
    print("MainActivity extract v9: applied")

if __name__ == "__main__":
    main()
