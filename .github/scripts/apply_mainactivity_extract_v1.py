#!/usr/bin/env python3
"""Wire MainActivity to ResultExportPolicy / HistoryProjectPolicy (idempotent)."""
from pathlib import Path
import re
import sys


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found under " + str(root))
    src = activity.read_text(encoding="utf-8")
    original = src

    old_rfb = (
        "private String resultFileBase(String task) {\n"
        '        String base = task == null || task.trim().isEmpty() ? "AI_Council_result"\n'
        '                : task.replaceAll("[^a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ0-9_-]+", "_");\n'
        "        if (base.length() > 48) base = base.substring(0, 48);\n"
        '        if (base.isEmpty()) base = "AI_Council_result";\n'
        "        return base;\n"
        "    }"
    )
    new_rfb = (
        "private String resultFileBase(String task) {\n"
        "        return ResultExportPolicy.resultFileBase(task);\n"
        "    }"
    )
    if "ResultExportPolicy.resultFileBase" not in src and old_rfb in src:
        src = src.replace(old_rfb, new_rfb, 1)

    old_hcr = (
        "private boolean hasCurrentResult(String answer) {\n"
        "        return answer != null && !answer.trim().isEmpty()\n"
        '                && !answer.trim().startsWith("Ваш итоговый ответ появится здесь");\n'
        "    }"
    )
    new_hcr = (
        "private boolean hasCurrentResult(String answer) {\n"
        "        return ResultExportPolicy.hasCurrentResult(answer);\n"
        "    }"
    )
    if "ResultExportPolicy.hasCurrentResult" not in src and old_hcr in src:
        src = src.replace(old_hcr, new_hcr, 1)

    if "ResultExportPolicy.toPlainText" not in src:
        pattern = (
            r'pendingExportText = "AI Council · результат\\n"\s*\+.*?'
            r'Создано в AI Council для Android\.\\n";'
        )
        replacement = (
            "pendingExportText = ResultExportPolicy.toPlainText(task, getCurrentResultStrategy(), answer,\n"
            "                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, "
            "java.util.Locale.getDefault()).format(new Date()));"
        )
        src2, n = re.subn(pattern, replacement, src, count=1, flags=re.S)
        if n == 1:
            src = src2
        else:
            print("WARN: plain-text export assignment not rewritten (shape differs)")

    if "HistoryProjectPolicy.resolveProjectName" not in src:
        old_name = 'if(name.isEmpty()) name=task.length()>42?task.substring(0,42)+"…":task;'
        new_name = 'if(name.isEmpty()) name=HistoryProjectPolicy.resolveProjectName("", task);'
        if old_name in src:
            src = src.replace(old_name, new_name, 1)

    if src == original:
        if "ResultExportPolicy.resultFileBase" in src:
            print("MainActivity extract v1: already applied")
            return
        raise SystemExit("Could not apply MainActivity extract v1")
    activity.write_text(src, encoding="utf-8")
    print("MainActivity extract v1: applied")


if __name__ == "__main__":
    main()
