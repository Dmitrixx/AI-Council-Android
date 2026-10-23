#!/usr/bin/env python3
"""Wire MainActivity history/projects UI to HistoryProjectPolicy (idempotent)."""
from pathlib import Path
import sys


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found under " + str(root))
    src = activity.read_text(encoding="utf-8")
    original = src

    old = 'historyCount.setText(arr.length() + "/20");'
    new = "historyCount.setText(HistoryProjectPolicy.historyCounterLabel(arr.length()));"
    if "HistoryProjectPolicy.historyCounterLabel" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'historyContainer.addView(text("История пока пуста. Выполненные задачи появятся здесь.", 12, MUTED, false));'
    new = "historyContainer.addView(text(HistoryProjectPolicy.EMPTY_HISTORY, 12, MUTED, false));"
    if "HistoryProjectPolicy.EMPTY_HISTORY" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'if(!task.toLowerCase(java.util.Locale.ROOT).contains(filter) && !answer.toLowerCase(java.util.Locale.ROOT).contains(filter)) continue;'
    new = "if(!HistoryProjectPolicy.matchesFilter(filter, task, answer)) continue;"
    if old in src:
        src = src.replace(old, new, 1)

    old = 'TextView t = text((i+1) + ". " + task, 13, TEXT, true);'
    new = "TextView t = text(HistoryProjectPolicy.historyTitle(i + 1, task), 13, TEXT, true);"
    if "HistoryProjectPolicy.historyTitle" not in src and old in src:
        src = src.replace(old, new, 1)

    for candidate in [
        'TextView meta = text(item.optString("mode", "") + " · " + date + "\\nНажмите, чтобы восстановить ответ", 10, MUTED, false);',
        'TextView meta = text(item.optString("mode", "") + " · " + date + "\nНажмите, чтобы восстановить ответ", 10, MUTED, false);',
    ]:
        if candidate in src and "HistoryProjectPolicy.historyMetaLine" not in src:
            src = src.replace(
                candidate,
                'TextView meta = text(HistoryProjectPolicy.historyMetaLine(item.optString("mode", ""), date), 10, MUTED, false);',
                1,
            )
            break

    old = "final int historyMode = modeIndex(storedMode);"
    new = "final int historyMode = HistoryProjectPolicy.modeIndex(storedMode, MODES);"
    if "HistoryProjectPolicy.modeIndex(storedMode" not in src and old in src:
        src = src.replace(old, new, 1)

    old = "for (int i=0;i<arr.length() && i<19;i++) next.put(arr.getJSONObject(i));"
    new = "for (int i=0;i<arr.length() && i<HistoryProjectPolicy.maxPreviousHistoryItems();i++) next.put(arr.getJSONObject(i));"
    if "maxPreviousHistoryItems" not in src and old in src:
        src = src.replace(old, new, 1)

    old = "if (!name.toLowerCase(java.util.Locale.ROOT).contains(filter) && !task.toLowerCase(java.util.Locale.ROOT).contains(filter)) continue;"
    new = "if (!HistoryProjectPolicy.matchesFilter(filter, name, task)) continue;"
    if old in src:
        src = src.replace(old, new, 1)

    for cand in [
        "TextView snippet = text(task.replace('\\n', ' '), 11, MUTED, false);",
        "TextView snippet = text(task.replace('\n', ' '), 11, MUTED, false);",
    ]:
        if cand in src and "projectTaskSnippet" not in src:
            src = src.replace(
                cand,
                "TextView snippet = text(HistoryProjectPolicy.projectTaskSnippet(task), 11, MUTED, false);",
                1,
            )
            break

    old = 'projectContainer.addView(text("Нет проектов по этому запросу. Сохраните текущую задачу выше.", 12, MUTED, false));'
    if "EMPTY_PROJECTS_FILTER" not in src and old in src:
        src = src.replace(old, "projectContainer.addView(text(HistoryProjectPolicy.EMPTY_PROJECTS_FILTER, 12, MUTED, false));", 1)
    old = 'projectContainer.addView(text("Не удалось прочитать проекты.", 12, RED, false));'
    if "PROJECTS_READ_ERROR" not in src and old in src:
        src = src.replace(old, "projectContainer.addView(text(HistoryProjectPolicy.PROJECTS_READ_ERROR, 12, RED, false));", 1)

    old = 'setStatus("Проект открыт." + (currentDialogue.length() > 0 ? " Диалог восстановлен." : " Для старого проекта диалог начнётся с текущего результата."), TEAL);'
    new = "setStatus(HistoryProjectPolicy.projectOpenedStatus(currentDialogue.length() > 0), TEAL);"
    if "projectOpenedStatus" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'if(name.isEmpty()) name=task.length()>42?task.substring(0,42)+"…":task;'
    new = 'if(name.isEmpty()) name=HistoryProjectPolicy.resolveProjectName("", task);'
    if old in src:
        src = src.replace(old, new, 1)

    old = "for(int i=0;i<arr.length() && i<99;i++) next.put(arr.getJSONObject(i));"
    new = "for(int i=0;i<arr.length() && i<HistoryProjectPolicy.maxPreviousProjectItems();i++) next.put(arr.getJSONObject(i));"
    if "maxPreviousProjectItems" not in src and old in src:
        src = src.replace(old, new, 1)

    old_mi = (
        "private int modeIndex(String storedMode) {\n"
        "        for (int i = 0; i < MODES.length; i++) if (MODES[i].equals(storedMode)) return i;\n"
        "        return 0;\n"
        "    }"
    )
    new_mi = (
        "private int modeIndex(String storedMode) {\n"
        "        return HistoryProjectPolicy.modeIndex(storedMode, MODES);\n"
        "    }"
    )
    if "return HistoryProjectPolicy.modeIndex(storedMode, MODES)" not in src and old_mi in src:
        src = src.replace(old_mi, new_mi, 1)

    if src == original:
        if "HistoryProjectPolicy.historyCounterLabel" in src:
            print("MainActivity extract v3: already applied")
            return
        raise SystemExit("Could not apply MainActivity extract v3")
    activity.write_text(src, encoding="utf-8")
    print("MainActivity extract v3: applied")


if __name__ == "__main__":
    main()
