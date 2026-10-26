# AI Council 2.14.0

Современная оболочка: чернильные поверхности, мятный акцент, плавающая навигация и контент от края до края на Android 11+. Debug APK собирается в GitHub Actions и публикуется в Releases.

AI Council — Android-приложение для работы с несколькими ИИ-провайдерами: независимые ответы, аудит, глубокий анализ, локальные серверы, поиск источников, импорт документов и экспорт отчётов.

## 2.14.0 — умнее локальный мозг совета

- versionName `2.14.0`, versionCode `81`.
- Перед синтезом координатор получает карту: причина или корреляция, краткосрок или долгосрок, случай или base rate, конфликт годов, разные или общие предпосылки, решение без плана отката.
- Необратимые решения автовыбор уводит в глубокий разбор.
- Сигналы 2.13 (направление и уверенность) теперь в исходниках, а не только в CI-патче.

## 2.2.13 — permanent policy extraction on main

- versionName `2.2.13`, versionCode `62`.
- MainActivity на main уже содержит делегирование во все policy-классы (ResultExport, ProviderDiagnostics, HistoryProject, StreamingResult, Settings, CouncilLaunch, FollowUpDialogue, InputImport, BackupExport).
- CI: один шаг `apply_mainactivity_extract_all.py` (idempotent / no-op), без девяти последовательных extract-скриптов.
- Сборка и smoke без изменения поведения для пользователя.

## 2.2.11 — policy-рефакторинг и стабильный CI

- versionName `2.2.11`, versionCode `60`.
- MainActivity: policies (ResultExport, ProviderDiagnostics, HistoryProject, StreamingResult, Settings, CouncilLaunch, FollowUpDialogue, InputImport, BackupExport).
- CI: longest-first version metadata, static verify markers, debug APK release.
- Режим «Только локальные ИИ» **не выключает веб-поиск**: галочка интернет-поиска сохраняет выбор пользователя независимо от режима ИИ.

## 2.0.0 — основа миграции Android 16

- `versionName` — `2.0.0`, `versionCode` — `47`.
- Android Gradle Plugin 9.4.0, Gradle 9.6.0, `compileSdk 36`, `targetSdk 36`.
- Потоковый клиент OkHttp для OpenAI-совместимого `POST /v1/chat/completions`.
- История, проекты и черновик в SQLite (`LocalDataStore`). Ключи API в миграцию не входят.
- `CouncilExecutionEngine`, `WebSearchService`, `ocr/OcrDocumentProcessor`.
- Глобальный `usesCleartextTraffic=true` удалён. Network Security Config блокирует cleartext по умолчанию.

## Возможности

- Несколько OpenAI-совместимых провайдеров и локальные endpoint’ы (Ollama/LM Studio).
- Режимы: быстрый ответ, совет, проверка, разбор, автовыбор.
- Потоковые ответы, follow-up диалог, история и проекты.
- Импорт текста, документов и OCR; предпросмотр перед вставкой с возможностью редактировать фрагмент.
- Веб-поиск как независимый переключатель; поисковая фраза отправляется внешним поисковым сервисам. Режим «Только локальные ИИ» не выключает веб-поиск.
- Экспорт результата (текст/Markdown), резервная копия JSON без API-ключей: ключи **не включаются** в backup.
- Для сканированных PDF: для каждой из первых пяти страниц сначала текстовый слой, затем OCR кириллицы при необходимости.

## Безопасность и ограничения

- API-ключи шифруются на устройстве; в резервную копию **не включаются**.
- В APK **не встроены веса языковой модели и on-device inference runtime**. Вычисления выполняются на локальном сервере (Ollama/LM Studio) или в облаке по выбору пользователя.
- HTTP в LAN не шифрует трафик; используйте доверенную сеть или HTTPS.
- Внешний web search отправляет запрос поисковым сервисам — не включайте поиск для конфиденциальных задач.

## Сборка

Debug APK: GitHub Actions → Releases (`AI-Council-2.14.0-debug.apk`).
Статическая проверка: `python3 tools/verify_project.py`.
Desktop smoke: `sh tools/desktop_smoke/run.sh`.
