#!/usr/bin/env python3
"""Wire MainActivity follow-up dialogue to FollowUpDialoguePolicy (idempotent)."""
from pathlib import Path
import sys


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found under " + str(root))
    src = activity.read_text(encoding="utf-8")
    original = src

    old = 'if (working) { setStatus("Дождитесь завершения текущего запроса.", RED); return; }'
    new = "if (working) { setStatus(FollowUpDialoguePolicy.BUSY, RED); return; }"
    if "FollowUpDialoguePolicy.BUSY" not in src and old in src:
        src = src.replace(old, new)

    old = 'if (originalQuestion.isEmpty()) { followUpInput.setError("Введите уточняющий вопрос"); return; }'
    new = "if (FollowUpDialoguePolicy.isQuestionEmpty(originalQuestion)) { followUpInput.setError(FollowUpDialoguePolicy.EMPTY_QUESTION); return; }"
    if "FollowUpDialoguePolicy.isQuestionEmpty(originalQuestion)" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'if (question.isEmpty()) { followUpInput.setError("Введите уточняющий вопрос"); return; }'
    new = "if (FollowUpDialoguePolicy.isQuestionEmpty(question)) { followUpInput.setError(FollowUpDialoguePolicy.EMPTY_QUESTION); return; }"
    if "FollowUpDialoguePolicy.isQuestionEmpty(question)" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'setStatus("Возможные секреты замаскированы перед отправкой уточнения.", BLUE);'
    new = "setStatus(FollowUpDialoguePolicy.SECRETS_MASKED, BLUE);"
    if "FollowUpDialoguePolicy.SECRETS_MASKED" not in src and old in src:
        src = src.replace(old, new, 1)

    old = (
        'if (contextAnswer.isEmpty() || contextAnswer.startsWith("Ваш итоговый ответ появится здесь")) {\n'
        '            setStatus("Сначала получите или откройте результат, по которому нужен уточняющий вопрос.", RED); return;\n'
        '        }'
    )
    new = (
        'if (!FollowUpDialoguePolicy.hasValidResultContext(contextAnswer)) {\n'
        '            setStatus(FollowUpDialoguePolicy.NO_RESULT_CONTEXT, RED); return;\n'
        '        }'
    )
    if "FollowUpDialoguePolicy.hasValidResultContext" not in src and old in src:
        src = src.replace(old, new, 1)

    old = "final int expertIndex = Math.max(0, Math.min(2, followUpExpertSpinner.getSelectedItemPosition()));"
    new = "final int expertIndex = FollowUpDialoguePolicy.clampExpertIndex(followUpExpertSpinner.getSelectedItemPosition());"
    if "FollowUpDialoguePolicy.clampExpertIndex" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'setStatus("Режим «Только локальные ИИ» блокирует облачный follow-up. Выберите настроенную локальную модель.", RED); return;'
    new = "setStatus(FollowUpDialoguePolicy.LOCAL_ONLY_BLOCKS_CLOUD, RED); return;"
    if "FollowUpDialoguePolicy.LOCAL_ONLY_BLOCKS_CLOUD" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'setStatus("Настройте ИИ №" + (expertIndex + 1) + " и проверьте его доступность в текущем режиме.", RED); return;'
    new = "setStatus(FollowUpDialoguePolicy.configureExpertMessage(expertIndex), RED); return;"
    if "FollowUpDialoguePolicy.configureExpertMessage" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'catch (Exception e) { setStatus("Не удалось прочитать ключ ИИ №" + (expertIndex + 1) + ". Введите его заново.", RED); return; }'
    new = "catch (Exception e) { setStatus(FollowUpDialoguePolicy.keyReadError(expertIndex), RED); return; }"
    if "FollowUpDialoguePolicy.keyReadError" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'followUpButton.setEnabled(false); followUpButton.setText("Выполняется…");'
    new = "followUpButton.setEnabled(false); followUpButton.setText(FollowUpDialoguePolicy.RUNNING_BUTTON);"
    if "FollowUpDialoguePolicy.RUNNING_BUTTON" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'setExpertState(expertIndex, "◉ Уточняет ответ", TEAL); setStatus("ИИ №" + (expertIndex + 1) + " продолжает диалог с учётом предыдущего результата…", TEAL);'
    new = "setExpertState(expertIndex, FollowUpDialoguePolicy.EXPERT_WORKING, TEAL); setStatus(FollowUpDialoguePolicy.dialogueStatus(expertIndex), TEAL);"
    if "FollowUpDialoguePolicy.EXPERT_WORKING" not in src and old in src:
        src = src.replace(old, new, 1)

    old = 'showHistory(); setStatus("Уточнение добавлено · " + Math.max(0,(System.currentTimeMillis()-started)/1000) + " сек.", TEAL);'
    new = "showHistory(); setStatus(FollowUpDialoguePolicy.successStatus(Math.max(0,(System.currentTimeMillis()-started)/1000)), TEAL);"
    if "FollowUpDialoguePolicy.successStatus" not in src and old in src:
        src = src.replace(old, new, 1)

    old = "while (currentDialogue.length() > 22) currentDialogue.remove(0);"
    new = "while (currentDialogue.length() > FollowUpDialoguePolicy.MAX_STORED_TURNS) currentDialogue.remove(0);"
    if "FollowUpDialoguePolicy.MAX_STORED_TURNS" not in src and old in src:
        src = src.replace(old, new, 1)

    old = "while (copy.length() > 20) { copy.remove(0); copy.remove(0); }"
    new = "while (copy.length() > FollowUpDialoguePolicy.MAX_REQUEST_TURNS) { copy.remove(0); copy.remove(0); }"
    if "FollowUpDialoguePolicy.MAX_REQUEST_TURNS" not in src and old in src:
        src = src.replace(old, new, 1)

    for cand in [
        'JSONObject follow = new JSONObject(); follow.put("role", "user"); follow.put("content", "Продолжи обсуждение, сохраняя контекст. Перепроверь спорные места, не выдумывай источники и явно обозначь ограничения.\\n\\nВопрос пользователя: " + question); copy.put(follow);',
        'JSONObject follow = new JSONObject(); follow.put("role", "user"); follow.put("content", "Продолжи обсуждение, сохраняя контекст. Перепроверь спорные места, не выдумывай источники и явно обозначь ограничения.\n\nВопрос пользователя: " + question); copy.put(follow);',
    ]:
        if cand in src and "FollowUpDialoguePolicy.buildFollowUserContent" not in src:
            src = src.replace(
                cand,
                'JSONObject follow = new JSONObject(); follow.put("role", "user"); follow.put("content", FollowUpDialoguePolicy.buildFollowUserContent(question)); copy.put(follow);',
                1,
            )
            break

    old = "int safeLimit = Math.max(0, Math.min(60000, maxTotalChars));"
    new = "int safeLimit = FollowUpDialoguePolicy.dialogueCharLimit(maxTotalChars);"
    if "FollowUpDialoguePolicy.dialogueCharLimit" not in src and old in src:
        src = src.replace(old, new, 1)

    old = "for (int i = source.length() - 1; i >= 0 && recentTurns.size() < 22 && usedChars < safeLimit; i--) {"
    new = "for (int i = source.length() - 1; i >= 0 && recentTurns.size() < FollowUpDialoguePolicy.maxTurnsInParse() && usedChars < safeLimit; i--) {"
    if "FollowUpDialoguePolicy.maxTurnsInParse" not in src and old in src:
        src = src.replace(old, new, 1)

    if src == original:
        if "FollowUpDialoguePolicy.BUSY" in src:
            print("MainActivity extract v7: already applied")
            return
        raise SystemExit("Could not apply MainActivity extract v7")
    activity.write_text(src, encoding="utf-8")
    print("MainActivity extract v7: applied")


if __name__ == "__main__":
    main()
