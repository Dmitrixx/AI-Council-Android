#!/usr/bin/env python3
"""Wire MainActivity streaming cards to StreamingResultPolicy (idempotent)."""
from pathlib import Path
import sys


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found under " + str(root))
    src = activity.read_text(encoding="utf-8")
    original = src

    old = "int count = mode == 0 ? 1 : (mode == 2 ? 2 : 3);"
    if "StreamingResultPolicy.streamingCardCount" not in src and old in src:
        src = src.replace(old, "int count = StreamingResultPolicy.streamingCardCount(mode);", 1)

    old_h = "TextView heading = text(titles[i], 13, accents[i], true);"
    if "StreamingResultPolicy.streamTitle(i)" not in src and old_h in src:
        src = src.replace(old_h, "TextView heading = text(StreamingResultPolicy.streamTitle(i), 13, accents[i], true);", 1)

    old_titles_init = (
        '        String[] titles = {"ИИ №1 · основной эксперт", "ИИ №2 · независимый эксперт", "ИИ №3 · координатор"};\n'
        "        int[] accents = {TEAL, BLUE, PURPLE};"
    )
    if old_titles_init in src:
        src = src.replace(old_titles_init, "        int[] accents = {TEAL, BLUE, PURPLE};", 1)

    old = 'TextView detail = text("Ожидает потокового ответа…", 13, TEXT, false);'
    if "StreamingResultPolicy.WAITING_STREAM" not in src and old in src:
        src = src.replace(old, "TextView detail = text(StreamingResultPolicy.WAITING_STREAM, 13, TEXT, false);", 1)

    old_block = (
        "        synchronized (buffer) {\n"
        "            if (buffer.length() < 180000) {\n"
        "                int available = 180000 - buffer.length();\n"
        "                buffer.append(token.length() <= available ? token : token.substring(0, available));\n"
        "            }\n"
        "        }"
    )
    if "StreamingResultPolicy.appendToken" not in src and old_block in src:
        src = src.replace(old_block, "        synchronized (buffer) {\n            StreamingResultPolicy.appendToken(buffer, token);\n        }", 1)

    old = "refreshUi = token.isEmpty() || now - lastStreamUiUpdateMs[slot] >= 80L;"
    if "StreamingResultPolicy.shouldRefreshUi" not in src and old in src:
        src = src.replace(old, "refreshUi = StreamingResultPolicy.shouldRefreshUi(token, now, lastStreamUiUpdateMs[slot]);", 1)

    old = 'synchronized (buffer) { detail.setText(buffer.length() == 0 ? "Ожидает потокового ответа…" : buffer.toString()); }'
    if "StreamingResultPolicy.streamingBodyDisplay" not in src and old in src:
        src = src.replace(old, "synchronized (buffer) { detail.setText(StreamingResultPolicy.streamingBodyDisplay(buffer)); }", 1)

    old_titles_partial = (
        '        String[] titles = {"ИИ №1 · основной эксперт", "ИИ №2 · независимый эксперт", "ИИ №3 · координатор"};\n'
        "        boolean any = false;"
    )
    if old_titles_partial in src:
        src = src.replace(old_titles_partial, "        boolean any = false;", 1)

    if ".append(titles[i])" in src:
        src = src.replace(".append(titles[i])", ".append(StreamingResultPolicy.streamTitle(i))", 1)

    old = 'resultCards.addView(text("Отдельные ответы экспертов появятся здесь после выполнения задачи.", 12, MUTED, false));'
    if "PLACEHOLDER_RESULT" not in src and old in src:
        src = src.replace(old, "resultCards.addView(text(StreamingResultPolicy.PLACEHOLDER_RESULT, 12, MUTED, false));", 1)

    old = 'TextView heading = text(headingText.length() > 110 ? headingText.substring(0, 110) + "…" : headingText, 13, accent, true);'
    if "StreamingResultPolicy.truncateHeading" not in src and old in src:
        src = src.replace(old, "TextView heading = text(StreamingResultPolicy.truncateHeading(headingText), 13, accent, true);", 1)

    old = 'String statusLabel = "error".equals(section.status) ? "Ошибка этапа" : ("interrupted".equals(section.status) ? "Задача прервана" : "Ответ частичный / возможна обрезка");'
    if "StreamingResultPolicy.stageStatusLabel" not in src and old in src:
        src = src.replace(old, "String statusLabel = StreamingResultPolicy.stageStatusLabel(section.status);", 1)

    old = 'String body = section.text == null || section.text.isEmpty() ? "Текст для этого этапа отсутствует." : section.text;'
    if "StreamingResultPolicy.stageBody" not in src and old in src:
        src = src.replace(old, "String body = StreamingResultPolicy.stageBody(section.text);", 1)

    old = 'TextView durationLabel = text("Время запросов: " + String.format(java.util.Locale.ROOT, "%.1f с", section.durationMs / 1000.0), 10, MUTED, false);'
    if "StreamingResultPolicy.durationLabel" not in src and old in src:
        src = src.replace(old, "TextView durationLabel = text(StreamingResultPolicy.durationLabel(section.durationMs), 10, MUTED, false);", 1)

    for old in [
        'if (full == null || full.trim().isEmpty() || full.trim().startsWith("Ваш итоговый ответ появится здесь")) {',
        'if (full == null || full.isEmpty() || full.startsWith("Ваш итоговый ответ появится здесь")) {',
    ]:
        if old in src and "StreamingResultPolicy.isPlaceholderResult" not in src:
            src = src.replace(old, "if (full == null || full.trim().isEmpty() || StreamingResultPolicy.isPlaceholderResult(full.trim())) {", 1)
            break

    old = 'String headingText = section.heading == null ? "Ответ эксперта" : section.heading;'
    if "StreamingResultPolicy.DEFAULT_HEADING" not in src and old in src:
        src = src.replace(old, "String headingText = section.heading == null ? StreamingResultPolicy.DEFAULT_HEADING : section.heading;", 1)

    if src == original:
        if "StreamingResultPolicy.streamingCardCount" in src:
            print("MainActivity extract v4: already applied")
            return
        raise SystemExit("Could not apply MainActivity extract v4")
    activity.write_text(src, encoding="utf-8")
    print("MainActivity extract v4: applied")


if __name__ == "__main__":
    main()
