#!/usr/bin/env python3
"""Strengthen coordinator synthesis prompts in CouncilExecutionEngine (idempotent)."""
from pathlib import Path
import sys
import urllib.request

FALLBACK_ENGINE = (
    "https://raw.githubusercontent.com/Dmitrixx/AI-Council-Android/"
    "b3615dc9b3edd89b91a5191a15ab25f5680a6e87/"
    "app/src/main/java/com/aicouncil/app/CouncilExecutionEngine.java"
)

def ensure_engine(engine: Path) -> str:
    src = engine.read_text(encoding="utf-8", errors="replace")
    if len(src) < 5000 or "class CouncilExecutionEngine" not in src or src.strip() == "PLACEHOLDER":
        print("Engine truncated in synthesis step; restoring…")
        with urllib.request.urlopen(FALLBACK_ENGINE, timeout=60) as resp:
            data = resp.read()
        if len(data) < 5000:
            raise SystemExit("cannot restore engine for synthesis patch")
        engine.write_bytes(data)
        src = engine.read_text(encoding="utf-8")
    return src

def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    engine = root / "app/src/main/java/com/aicouncil/app/CouncilExecutionEngine.java"
    if not engine.is_file():
        raise SystemExit("CouncilExecutionEngine.java not found")
    src = ensure_engine(engine)

    if "CouncilAnalysisPolicy.buildCoordinatorOpening()" in src:
        print("Enhanced synthesis: already applied")
        return

    marker = 'String reviewerPrompt = "Ты независимый координатор AI Council.'
    idx = src.find(marker)
    if idx < 0:
        print("Enhanced synthesis: reviewerPrompt marker not found (skip)")
        return

    end = src.find("String review = cb.callApi(2", idx)
    if end < 0:
        end = src.find("reviewerPrompt = reviewerPrompt + CouncilLaunchPolicy", idx)
    if end < 0:
        raise SystemExit("cannot locate end of reviewerPrompt assignment")

    new_block = (
        "String reviewerPrompt = CouncilAnalysisPolicy.buildCoordinatorOpening()\n"
        "                + \"\\nЗАДАЧА ПОЛЬЗОВАТЕЛЯ:\\n\" + task + \"\\n\\nОТВЕТ ИИ №1 (\" + r.models[0] + \"):\\n\" + a "
        "+ \"\\n\\nОТВЕТ ИИ №2 (\" + r.models[1] + \"):\\n\" + b +\n"
        "                (r.mode == 3 ? \"\\n\\nВЗАИМНАЯ КРИТИКА ИИ №1:\\n\" + critiqueA + \"\\n\\nВЗАИМНАЯ КРИТИКА ИИ №2:\\n\" "
        "+ critiqueB +\n"
        "                        \"\\n\\nОцени критику так же критически: не принимай замечание только потому, что оно высказано. "
        "Отдели найденную ошибку от гипотезы, а неразрешённое разногласие оставь явно открытым.\" : \"\") +\n"
        "                \"\\n\\n\" + CouncilAnalysisPolicy.coordinatorEvidenceRules();\n"
        "        "
    )
    src = src[:idx] + new_block + src[end:]

    if "TaskRoutingPolicy.toolAwarenessHint" not in src:
        old_task = '"Реши задачу самостоятельно. Покажи ключевые шаги, допущения и неопределённости.\\n\\nЗАДАЧА:\\n" + task'
        new_task = (
            '"Реши задачу самостоятельно. Покажи ключевые шаги, допущения и неопределённости.\\n\\n" '
            '+ (TaskRoutingPolicy.toolAwarenessHint(task).isEmpty() ? "" : TaskRoutingPolicy.toolAwarenessHint(task) + "\\n\\n") '
            '+ "ЗАДАЧА:\\n" + task'
        )
        if old_task in src:
            src = src.replace(old_task, new_task, 1)
        old_task2 = '"Независимо реши задачу другим способом, проверь факты и расчёты. Укажи неопределённости.\\n\\nЗАДАЧА:\\n" + task'
        new_task2 = (
            '"Независимо реши задачу другим способом, проверь факты и расчёты. Укажи неопределённости.\\n\\n" '
            '+ (TaskRoutingPolicy.toolAwarenessHint(task).isEmpty() ? "" : TaskRoutingPolicy.toolAwarenessHint(task) + "\\n\\n") '
            '+ "ЗАДАЧА:\\n" + task'
        )
        if old_task2 in src:
            src = src.replace(old_task2, new_task2, 1)

    engine.write_text(src, encoding="utf-8")
    print("Enhanced synthesis: applied")


if __name__ == "__main__":
    main()
