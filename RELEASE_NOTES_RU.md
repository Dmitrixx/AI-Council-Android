# AI Council 2.2.0

Debug APK этой версии собирается workflow `Build AI Council APK` и прикрепляется к релизу `v2.2.0`.

- **versionName** 2.2.0 / **versionCode** 49
- Английский README для международной видимости
- Подтверждено: лимит страниц PDF (5/20) уже в исходниках UI
- Документация и план дальнейших улучшений (разбиение MainActivity)
- Предыдущие заметки 2.1.0 / 2.0.0 сохранены ниже

# AI Council 2.1.0

Debug APK этой версии собирается workflow `Build AI Council APK` и прикрепляется к релизу `v2.1.0`.

- Оболочка: чернила + мята, плавающая навигация, edge-to-edge.
- versionName 2.1.0 / versionCode 48.
- Предыдущие заметки 2.0.0 сохранены ниже.

# AI Council 2.0.0 — рабочие заметки к сдаче

> Статус: промежуточная реализация задания. Актуальная сборочная конфигурация — AGP 9.4.0 / Gradle 9.6.0 / API 36.

## Включено
- OCR изображений и PDF в `OcrDocumentProcessor` (гибрид PDFBox + PdfRenderer + Tesseract/ML Kit)
- OkHttp для chat SSE, поиска, моделей, OCR-загрузок
- LocalDataStore (SQLite), CouncilExecutionEngine, WebSearchService
- Network Security Config без cleartext по умолчанию
- Лимит PDF 5/20 страниц в UI

## Ограничения
- MainActivity всё ещё крупный (~2900 строк)
- llama.cpp/JNI/GGUF не реализованы
- Требуется проверка на физическом устройстве
