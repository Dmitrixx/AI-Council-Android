#!/usr/bin/env python3
"""Wire MainActivity council launch/stop to CouncilLaunchPolicy (idempotent)."""
from pathlib import Path
import sys


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found under " + str(root))
    src = activity.read_text(encoding="utf-8")
    original = src

    old = "private static final int MAX_TASK_CHARS = 100000;"
    new = "private static final int MAX_TASK_CHARS = CouncilLaunchPolicy.MAX_TASK_CHARS;"
    if "CouncilLaunchPolicy.MAX_TASK_CHARS" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'setStatus("■  Остановка запрошена…", RED);'
    new = "setStatus(CouncilLaunchPolicy.STOP_REQUESTED, RED);"
    if "CouncilLaunchPolicy.STOP_REQUESTED" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'setStatus("●  Дождитесь завершения текущего запроса.", RED);'
    new = "setStatus(CouncilLaunchPolicy.BUSY, RED);"
    if "CouncilLaunchPolicy.BUSY" not in src and old in src:
        src = src.replace(old, new)

    old = "if (originalTask.isEmpty() || originalTask.length() > MAX_TASK_CHARS) { runCouncilRequest(); return; }"
    new = "if (CouncilLaunchPolicy.skipSensitiveConfirm(originalTask)) { runCouncilRequest(); return; }"
    if "CouncilLaunchPolicy.skipSensitiveConfirm" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'setStatus("Возможные секреты замаскированы перед отправкой задачи.", BLUE);'
    new = "setStatus(CouncilLaunchPolicy.SECRETS_MASKED, BLUE);"
    if "CouncilLaunchPolicy.SECRETS_MASKED" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'if (task.isEmpty()) { prompt.setError("Введите задачу"); return; }'
    new = "if (CouncilLaunchPolicy.isTaskEmpty(task)) { prompt.setError(CouncilLaunchPolicy.EMPTY_TASK); return; }"
    if "CouncilLaunchPolicy.isTaskEmpty" not in src and old in src:
        src = src.replace(old, new, 1)

    old = (
        'if (task.length() > MAX_TASK_CHARS) {\n'
        '            prompt.setError("Сократите задачу до 100 000 символов");\n'
        '            setStatus("Запрос не отправлен: текст задачи превышает лимит 100 000 символов.", RED);\n'
        '            return;\n'
        '        }'
    )
    new = (
        'if (CouncilLaunchPolicy.isTaskTooLong(task)) {\n'
        '            prompt.setError(CouncilLaunchPolicy.TASK_TOO_LONG_ERROR);\n'
        '            setStatus(CouncilLaunchPolicy.TASK_TOO_LONG_STATUS, RED);\n'
        '            return;\n'
        '        }'
    )
    if "CouncilLaunchPolicy.isTaskTooLong" not in src and old in src:
        src = src.replace(old, new, 1)

    old = "int chosenMode = selectedMode == 4 ? chooseAutomaticMode(task) : selectedMode;"
    new = (
        "boolean[] configuredProviders = new boolean[3];\n"
        "        boolean[] markedFree = new boolean[3];\n"
        "        for (int i = 0; i < 3; i++) {\n"
        "            configuredProviders[i] = configured(i);\n"
        "            markedFree[i] = providerFree[i] != null && providerFree[i].isChecked();\n"
        "        }\n"
        "        int chosenMode = CouncilLaunchPolicy.resolveMode(selectedMode, task, configuredProviders,\n"
        "                freeOnly != null && freeOnly.isChecked(), markedFree);"
    )
    if "CouncilLaunchPolicy.resolveMode" not in src and old in src:
        src = src.replace(old, new, 1)

    old = "int[] required = mode == 0 ? new int[]{0} : (mode == 2 ? new int[]{0,1} : new int[]{0,1,2});"
    new = "int[] required = CouncilLaunchPolicy.requiredProviderIndices(mode);"
    if "CouncilLaunchPolicy.requiredProviderIndices" not in src and old in src:
        src = src.replace(old, new)

    old = "final int selectedWebContextMode = webContextModeSpinner == null ? 2 : Math.max(0, Math.min(WEB_CONTEXT_MODES.length - 1, webContextModeSpinner.getSelectedItemPosition()));"
    new = "final int selectedWebContextMode = webContextModeSpinner == null ? 2 : CouncilLaunchPolicy.clampWebContextMode(webContextModeSpinner.getSelectedItemPosition(), WEB_CONTEXT_MODES.length);"
    if "CouncilLaunchPolicy.clampWebContextMode" not in src and old in src:
        src = src.replace(old, new, 1)

    old = (
        'boolean hasFailure = cancelRequested || finalResult.startsWith("Не удалось") || finalResult.startsWith("Ошибка") || finalResult.contains("НЕ ПОЛУЧЕН")\n'
        '                        || finalResult.contains("Оба независимых эксперта не смогли") || finalResult.contains("⚠️") || finalResult.contains(" · ОШИБКА");'
    )
    new = "boolean hasFailure = CouncilLaunchPolicy.looksLikeFailure(finalResult, cancelRequested);"
    if "CouncilLaunchPolicy.looksLikeFailure" not in src and old in src:
        src = src.replace(old, new, 1)

    old = (
        'if (cancelRequested) setStatus("Остановлено пользователем. Частичный результат сохранён.", RED);\n'
        '                else setStatus((hasFailure ? "Завершено с предупреждением" : "Готово") + " · " + elapsedSeconds + " сек. · проверьте важные факты", hasFailure ? RED : TEAL);'
    )
    new = "setStatus(CouncilLaunchPolicy.completionStatus(cancelRequested, hasFailure, elapsedSeconds), cancelRequested || hasFailure ? RED : TEAL);"
    if "CouncilLaunchPolicy.completionStatus" not in src and old in src:
        src = src.replace(old, new, 1)

    if src == original:
        if "CouncilLaunchPolicy.BUSY" in src:
            print("MainActivity extract v6: already applied")
            return
        raise SystemExit("Could not apply MainActivity extract v6")
    activity.write_text(src, encoding="utf-8")
    print("MainActivity extract v6: applied")


if __name__ == "__main__":
    main()
