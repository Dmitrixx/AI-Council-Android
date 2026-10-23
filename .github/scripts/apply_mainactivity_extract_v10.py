#!/usr/bin/env python3
"""Deeper HistoryProject / FollowUp / modeIndex wiring in MainActivity (idempotent)."""
from pathlib import Path
import sys


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found under " + str(root))
    src = activity.read_text(encoding="utf-8")
    original = src

    # history counter: arr.length() + "/20" -> HistoryProjectPolicy.historyCounterLabel
    old = 'historyCount.setText(arr.length() + "/20");'
    new = "historyCount.setText(HistoryProjectPolicy.historyCounterLabel(arr.length()));"
    if "HistoryProjectPolicy.historyCounterLabel" not in src and old in src:
        src = src.replace(old, new, 1)

    # empty history label
    old = 'historyContainer.addView(text("История пока пуста. Выполненные задачи появятся здесь.", 12, MUTED, false));'
    new = "historyContainer.addView(text(HistoryProjectPolicy.EMPTY_HISTORY, 12, MUTED, false));"
    if "HistoryProjectPolicy.EMPTY_HISTORY" not in src and old in src:
        src = src.replace(old, new, 1)

    # history hint in meta
    old = (
        '" · " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(item.optLong("time", 0))) + "\\nНажмите, чтобы восстановить ответ"'
    )
    new = (
        '" · " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(item.optLong("time", 0))) + "\\n" + HistoryProjectPolicy.HISTORY_HINT'
    )
    if "HistoryProjectPolicy.HISTORY_HINT" not in src and old in src:
        src = src.replace(old, new, 1)

    # saveToHistory keep previous items via policy
    old = "for (int i=0;i<arr.length() && i<19;i++) next.put(arr.getJSONObject(i));"
    new = "for (int i=0;i<arr.length() && i<HistoryProjectPolicy.maxPreviousHistoryItems();i++) next.put(arr.getJSONObject(i));"
    if "HistoryProjectPolicy.maxPreviousHistoryItems()" not in src and old in src:
        src = src.replace(old, new, 1)

    # empty projects filter
    old = 'projectContainer.addView(text("Нет проектов по этому запросу. Сохраните текущую задачу выше.", 12, MUTED, false));'
    new = "projectContainer.addView(text(HistoryProjectPolicy.EMPTY_PROJECTS_FILTER, 12, MUTED, false));"
    if "HistoryProjectPolicy.EMPTY_PROJECTS_FILTER" not in src and old in src:
        src = src.replace(old, new, 1)

    # projects read error
    old = 'projectContainer.addView(text("Не удалось прочитать проекты.", 12, RED, false));'
    new = "projectContainer.addView(text(HistoryProjectPolicy.PROJECTS_READ_ERROR, 12, RED, false));"
    if "HistoryProjectPolicy.PROJECTS_READ_ERROR" not in src and old in src:
        src = src.replace(old, new, 1)

    # modeIndex method body -> policy
    old = (
        "    private int modeIndex(String storedMode) {\n"
        "        for (int i = 0; i < MODES.length; i++) if (MODES[i].equals(storedMode)) return i;\n"
        "        return 0;\n"
        "    }"
    )
    new = (
        "    private int modeIndex(String storedMode) {\n"
        "        return HistoryProjectPolicy.modeIndex(storedMode, MODES);\n"
        "    }"
    )
    if "HistoryProjectPolicy.modeIndex(storedMode, MODES)" not in src and old in src:
        src = src.replace(old, new, 1)

    if src == original:
        print("MainActivity extract v10: already applied")
        return
    activity.write_text(src, encoding="utf-8")
    print("MainActivity extract v10: applied")


if __name__ == "__main__":
    main()
