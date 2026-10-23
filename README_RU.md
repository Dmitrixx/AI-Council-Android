# AI Council 2.2.11

Современная оболочка: чернильные поверхности, мятный акцент, плавающая навигация и контент от края до края на Android 11+. Debug APK собирается в GitHub Actions и публикуется в Releases.

AI Council — Android-приложение для работы с несколькими ИИ-провайдерами: независимые ответы, аудит, глубокий анализ, локальные серверы, поиск источников, импорт документов и экспорт отчётов.

## 2.2.x — policy-рефакторинг MainActivity

- ResultExport, ProviderDiagnostics, HistoryProject, StreamingResult, Settings, CouncilLaunch, FollowUpDialogue, InputImport, BackupExport policies.
- Static verify markers после extract’ов; безопасное обновление version metadata в CI.
- versionName `2.2.11`, versionCode `60`.

## 2.0.0 — основа миграции Android 16

- Локальные и облачные OpenAI-совместимые endpoint’ы, режим «только локальные ИИ».
- Веб-поиск как независимый выбор пользователя (не отключается локальным режимом).
- OCR PDF с лимитом 5/20 страниц, история, проекты, backup без API-ключей.
