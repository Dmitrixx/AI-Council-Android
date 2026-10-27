#!/usr/bin/env python3
"""Apply robust final-answer recovery to CouncilExecutionEngine (idempotent)."""
from pathlib import Path
import sys
import urllib.request

FALLBACK = (
    "https://raw.githubusercontent.com/Dmitrixx/AI-Council-Android/"
    "b3615dc9b3edd89b91a5191a15ab25f5680a6e87/"
    "app/src/main/java/com/aicouncil/app/CouncilExecutionEngine.java"
)

def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    engine = root / "app/src/main/java/com/aicouncil/app/CouncilExecutionEngine.java"
    if not engine.is_file():
        raise SystemExit("CouncilExecutionEngine.java not found")
    src = engine.read_text(encoding="utf-8", errors="replace")
    if len(src) < 5000 or "class CouncilExecutionEngine" not in src or src.strip() == "PLACEHOLDER":
        print("Engine truncated; restoring known-good…")
        with urllib.request.urlopen(FALLBACK, timeout=60) as resp:
            data = resp.read()
        if len(data) < 5000:
            raise SystemExit("could not restore CouncilExecutionEngine")
        engine.write_bytes(data)
        src = engine.read_text(encoding="utf-8")
        print("Engine restored:", len(src), "bytes")

    # Three experts and a dedicated slot-4 coordinator use a distinct failure path.
    # Never apply the legacy two-expert patch to the structured four-slot engine.
    if ("String review = cb.callApi(3, r.endpoints[3], r.models[3], r.keys[3], reviewerPrompt, r.roles[3]);" in src
            and "Проведи независимый аудит доказательств" in src
            and "StructuredClaimPolicy.addContract" in src):
        print("Robust final answer: four-slot council integration already applied")
        return

    if "partialExpertCoordinatorNote" in src and "Разбор сбоев" in src:
        print("Robust final answer: already applied")
        return

    old_both = (
        '        if (aFailed && bFailed) {\n'
        '            return "Оба независимых эксперта не смогли завершить запрос.\\n\\nИИ №1 · " + r.models[0] + ":\\n" + a\n'
        '                    + "\\n\\nИИ №2 · " + r.models[1] + ":\\n" + b\n'
        '                    + "\\n\\nКоординатор не запускался, чтобы не выдавать ошибки API за содержательные ответы.";\n'
        '        }'
    )
    new_both = (
        '        if (aFailed && bFailed) {\n'
        '            // Robust path: coordinator diagnostics instead of empty failure\n'
        '            cb.onExpertState(2, "◉ Разбор сбоев", Tone.COORDINATOR);\n'
        '            cb.onProgress("Оба эксперта с ошибкой · координатор формирует диагностику", Tone.COORDINATOR, 70);\n'
        '            String recoveryPrompt = "Ты координатор AI Council. Оба независимых эксперта вернули ошибку, а не ответ по задаче.\\n"\n'
        '                    + "1) Кратко разбери тексты ошибок (ключ, URL, модель, лимит, сеть).\\n"\n'
        '                    + "2) Что пользователю проверить в настройках.\\n"\n'
        '                    + "3) Если по формулировке задачи всё же можно дать осторожный частичный совет — дай его отдельным блоком с явной оговоркой о неполноте.\\n"\n'
        '                    + "Не выдавай текст ошибок API за мнение экспертов.\\n\\nЗАДАЧА ПОЛЬЗОВАТЕЛЯ:\\n" + task\n'
        '                    + "\\n\\nОШИБКА ИИ №1 · " + r.models[0] + ":\\n" + a\n'
        '                    + "\\n\\nОШИБКА ИИ №2 · " + r.models[1] + ":\\n" + b;\n'
        '            String recovery = cb.callApi(2, r.endpoints[2], r.models[2], r.keys[2], recoveryPrompt, r.roles[2]);\n'
        '            boolean recoveryFailed = cb.isApiFailure(recovery);\n'
        '            cb.onExpertState(2, recoveryFailed ? "! Диагностика недоступна" : "✓ Диагностика", recoveryFailed ? Tone.ERROR : Tone.COORDINATOR);\n'
        '            cb.onProgress(null, Tone.COORDINATOR, 100);\n'
        '            StringBuilder both = new StringBuilder();\n'
        '            both.append("Оба независимых эксперта не смогли завершить запрос.\\n\\n");\n'
        '            both.append("ИИ №1 · ").append(r.models[0]).append(":\\n").append(a).append("\\n\\n");\n'
        '            both.append("ИИ №2 · ").append(r.models[1]).append(":\\n").append(b).append("\\n\\n");\n'
        '            both.append("────────────────────\\n\\n");\n'
        '            both.append("✦ Координатор · диагностика\\n\\n");\n'
        '            both.append(recoveryFailed\n'
        '                    ? (recovery + "\\n\\n⚠️ Координатор тоже не ответил. Проверьте ключи, URL и доступность моделей вручную.")\n'
        '                    : recovery);\n'
        '            return both.toString();\n'
        '        }'
    )
    if old_both not in src:
        raise SystemExit("both-fail block not found — unexpected engine shape")
    src = src.replace(old_both, new_both, 1)

    call_marker = "String review = cb.callApi(2, r.endpoints[2], r.models[2], r.keys[2], reviewerPrompt, r.roles[2]);"
    insert = (
        "        reviewerPrompt = reviewerPrompt + CouncilLaunchPolicy.partialExpertCoordinatorNote(aFailed, bFailed);\n"
        "        "
    )
    if "partialExpertCoordinatorNote" not in src:
        if call_marker not in src:
            raise SystemExit("review callApi marker not found")
        src = src.replace(call_marker, insert + call_marker, 1)

    engine.write_text(src, encoding="utf-8")
    print("Robust final answer: applied")


if __name__ == "__main__":
    main()
