#!/usr/bin/env python3
"""Wire MainActivity completion status to CouncilLaunchPolicy partial-success helpers."""
from pathlib import Path
import sys


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found under " + str(root))
    src = activity.read_text(encoding="utf-8")
    original = src

    old = (
        "boolean hasFailure = cancelRequested || finalResult.startsWith(\"Не удалось\") || finalResult.startsWith(\"Ошибка\") || finalResult.contains(\"НЕ ПОЛУЧЕН\")\n"
        "                        || finalResult.contains(\"Оба независимых эксперта не смогли\") || finalResult.contains(\"⚠️\") || finalResult.contains(\" · ОШИБКА\");"
    )
    new = (
        "boolean hasFailure = CouncilLaunchPolicy.looksLikeFailure(finalResult, cancelRequested)\n"
        "                        || finalResult.contains(\"⚠️\");\n"
        "                boolean partialSuccess = CouncilLaunchPolicy.looksLikePartialSuccess(finalResult, cancelRequested);"
    )
    if "looksLikePartialSuccess" not in src and old in src:
        src = src.replace(old, new, 1)

    old_status = (
        'else setStatus((hasFailure ? "Завершено с предупреждением" : "Готово") + " · " + elapsedSeconds + " сек. · проверьте важные факты", hasFailure ? RED : TEAL);'
    )
    new_status = (
        "else setStatus(CouncilLaunchPolicy.completionStatus(cancelRequested, hasFailure, partialSuccess, elapsedSeconds), "
        "(hasFailure && !partialSuccess) ? RED : (partialSuccess ? BLUE : TEAL));"
    )
    if "completionStatus(cancelRequested, hasFailure, partialSuccess" not in src and old_status in src:
        src = src.replace(old_status, new_status, 1)

    if "looksLikePartialSuccess" not in src and "CouncilLaunchPolicy.looksLikeFailure(finalResult" in src:
        old3 = "boolean hasFailure = CouncilLaunchPolicy.looksLikeFailure(finalResult, cancelRequested);"
        new3 = (
            "boolean hasFailure = CouncilLaunchPolicy.looksLikeFailure(finalResult, cancelRequested)\n"
            "                        || finalResult.contains(\"⚠️\");\n"
            "                boolean partialSuccess = CouncilLaunchPolicy.looksLikePartialSuccess(finalResult, cancelRequested);"
        )
        if old3 in src:
            src = src.replace(old3, new3, 1)

    if src == original:
        print("MainActivity extract v11: already applied or patterns not found")
        return
    activity.write_text(src, encoding="utf-8")
    print("MainActivity extract v11: applied")


if __name__ == "__main__":
    main()
