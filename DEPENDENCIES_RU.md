# Зависимости AI Council 2.0.0

- Android Gradle Plugin 9.4.0
- compileSdk 36, minSdk 23, targetSdk 36
- Java 8 для исходного кода приложения
- `com.tom-roush:pdfbox-android:2.0.27.0` — локальное извлечение текстового слоя PDF
- `com.google.mlkit:text-recognition:16.0.1` — OCR латинского письма
- `androidx.exifinterface:exifinterface:1.4.2` — чтение EXIF-ориентации фото перед локальным OCR.
- `cz.adaptech.tesseract4android:tesseract4android:4.9.0` — локальный OCR кириллицы, артефакт подключается через JitPack
- `WebSearchService` — собственная Java-оркестрация поиска и извлечения безопасного ограниченного HTML-контекста вне `MainActivity`; `WebSearchClient`/`WebPageFetchClient` используют OkHttp 4.12.0.
- `com.squareup.okhttp3:okhttp:4.12.0` используется в потоковом клиенте `ChatCompletionsClient` для OpenAI-совместимых `/v1/chat/completions` и в отдельном `WebSearchClient` для Brave Search, Tavily, MediaWiki/Wikipedia и DuckDuckGo Instant Answer JSON API. `WebSearchEndpointPolicy` ограничивает поисковый транспорт фиксированными HTTPS API endpoint-ами, headers для credentials привязаны к своему provider, ответы ограничены по байтам, redirects отключены, вызовы отменяемы. Загрузка выбранных HTML-страниц также вынесена в `WebPageFetchClient` на OkHttp: DNS-ответы проверяются и закрепляются на время запроса, проверяются redirect targets, MIME type и лимит тела; редиректы выполняются вручную и ограничены двумя переходами. Локальный OCR вынесен в `ocr/OcrDocumentProcessor`; сетевой transport остаётся только у отдельно ограниченных клиентов.
- Подключение локального LLM-сервера использует собственный Java HTTP-клиент и OpenAI-совместимые `/v1/chat/completions` и `/v1/models` endpoints; Ollama/LM Studio не включены в APK. `LocalAiPolicy.java` и `WebSearchPolicy.java` содержат тестируемые pure-Java правила без новых внешних зависимостей.

## OCR-процессинг

- `OcrDocumentProcessor` принимает изображение или уже ограниченный PDF payload, возвращает текст, движок, число страниц и явные предупреждения. UI и подтверждение вставки остаются в Activity. Для PDF каждая страница сначала проходит PDFBox text-layer extraction; Android `PdfRenderer` и OCR вызываются только для страниц без текста.
- EXIF-ориентация применяется до распознавания; изображение проверяется на 10 MiB, уменьшается до рабочей геометрии и декодируется в `RGB_565`.
- `OcrInputPolicy` — Java 8 policy без Android API; тестирует язык, текстовый слой PDF, пределы размера/страниц, sampling, имена и лимит текста. Стандартный лимит PDF — 5 страниц; policy допускает конфигурацию до 20, но экран настройки ещё не подключён. `OcrInputPolicySmokeTest` — 18/18; `OcrDocumentProcessorSmokeTest` — 5/5 на compile-time stubs.

## Языковые модели OCR

- `rus.traineddata` и `ukr.traineddata` скачиваются отдельно по выбору пользователя.
- Источник: `https://github.com/tesseract-ocr/tessdata_fast`, версия данных `4.1.0`; языковые модели сохраняются в приватной директории приложения.
- Каждый файл сверяется с точным размером и закреплённым SHA-512 до установки; кэшированная модель тоже проверяется перед использованием. Размер каждой модели дополнительно ограничен общим лимитом 8 МБ; изображение/PDF не загружается для OCR.
- Закреплённые размеры и SHA-512: `rus.traineddata` — 3 861 738 байт; `ukr.traineddata` — 3 825 102 байта. Контрольные суммы взяты из файлового manifest для upstream `tessdata_fast` 4.1.0: https://gitlab.com/liguros/liguros-repo/-/blob/stable/app-text/tessdata_fast/Manifest. Проверка вынесена в `OcrModelIntegrityPolicy`, а загрузка/установка файлов — в `OcrModelManager`. Используется SHA-512 (64 байта digest), то есть более длинная проверка целостности, чем SHA-256. Общий OkHttp transport и расширяемый каталог языков ещё не подключены.

## Поиск и локальные AI-серверы

- Brave Web Search API: `https://api.search.brave.com/res/v1/web/search`, требуется пользовательский subscription token; токен хранится зашифрованным и передаётся только на этот endpoint.
- Tavily Search API: `https://api.tavily.com/search`, пользовательский ключ хранится зашифрованным и отправляется заголовком `Authorization: Bearer …` только на этот fixed endpoint; свежие запросы переводятся в topic `news` с временным фильтром, обычные — в `general`.
- Wikipedia Search: MediaWiki Action API на локализованном домене `ru.wikipedia.org`, `uk.wikipedia.org` или `en.wikipedia.org`.
- DuckDuckGo HTML scraping удалён в 2.0.0; используется только `https://api.duckduckgo.com/` (Instant Answer JSON), который не является полноценным полнотекстовым индексом.
- DuckDuckGo Instant Answer API: `https://api.duckduckgo.com/`; справочные ответы, не полноценный полнотекстовый индекс.
- Ollama локальный OpenAI-совместимый endpoint обычно `http://<LAN-IP>:11434/v1`; LM Studio обычно `http://<LAN-IP>:1234/v1`. Пользователь обязан заменить пример IP на реальный адрес локального хоста.
- HTTP в Android Network Security Config разрешён только для loopback/эмуляторного адреса/локальных DNS-имён; произвольные private IPv4 пока не входят в статический allowlist, см. ограничение версии 2.0.0. HTTPS используется для облачных endpoints. Распознанные локальные HTTP- и HTTPS-endpoints могут работать без API-ключа, а сохранённый облачный ключ не передаётся в локальный адрес. Незащищённый HTTP в LAN не обеспечивает шифрование трафика.

См. `THIRD_PARTY_NOTICES_RU.md` для ссылок и сведений о внешних сервисах.


## Изменения в 1.26.0

В этом релизе не добавлено сторонних библиотек: форматирование Markdown реализовано чистым Java-классом `ResultReportFormatter`, сохранение файла выполняет системный Android Storage Access Framework, а копирование — системный буфер обмена Android.

## Изменения 1.27.0

- Новых внешних библиотек нет. `CouncilAnalysisPolicy` использует только Java 8 API и проверяется автономным `javac` smoke runner.


## Изменения 1.28.0

- Новые сторонние библиотеки не добавлялись. Настраиваемые роли и маршрутизация продолжения ответа реализованы чистыми Java-классами `ExpertRolePolicy` и `TruncatedAnswerPolicy`, которые входят в desktop smoke runner.


## Изменения 1.29.0

- Новых сторонних библиотек не добавлено.
- Иконка приложения реализована через Android adaptive-icon XML для Android 8.0+ и PNG fallback по плотностям для API 23–25.
- Фирменная тема и окно запуска используют ресурсы Android, без сторонней UI-зависимости.
- Раскрываемые секции и синхронизация нижней навигации реализованы на Android SDK.


## Изменения 1.32.0

- Сторонние зависимости не добавлялись. Три режима темы, семантическое преобразование палитры и подсчёт коэффициента контраста реализованы в `UiThemePolicy` без Android-зависимостей, поэтому класс проверяется обычным Java runner. Нативные ресурсы `AppThemeLight` и `splash_background_light` используют Android SDK.


## Дополнения версии 1.33.0

Новых сторонних библиотек нет. `ProviderErrorFormatter` использует только стандартную Java-библиотеку для классификации HTTP-ошибок и очистки диагностического текста.


## Дополнения версии 1.35.0

Новых сторонних библиотек нет. `SecretRedactor` использует только стандартные регулярные выражения Java и подключён к форматтеру HTTP-ошибок. Фильтрация секретов best-effort и применяется к отображаемым диагностическим деталям.


## Дополнения версии 1.36.0

Новых сторонних библиотек и Android-разрешений не добавлено. `PromptMetricsPolicy` и `TaskRoutingPolicy` работают на стандартных Java API; варианты голосового ввода предоставляет системная служба Android.


## Дополнения версии 1.37.0 (историческая запись)

Новых сторонних библиотек, Gradle-плагинов и Android-разрешений не добавлено. `PromptPrivacyPolicy` повторно использует `SecretRedactor` и работает локально на стандартных Java API. Успешная Gradle-сборка 1.37.0 ещё должна быть подтверждена Android CI.

## Версия 2.0.0

- Android Gradle Plugin 9.4.0, Gradle 9.6.0, Android API 36.
- Глобальный cleartext выключен; исключения задаёт `app/src/main/res/xml/network_security_config.xml`. Из-за ограничений Android NSC arbitrary private IPv4 HTTP hosts пока не поддерживаются этим allowlist.
- OkHttp 4.12.0 подключён для потоковых chat completions. Room не используется: локальные данные сохраняет `LocalDataStore` на базе Android `SQLiteOpenHelper`. llama.cpp/JNI не подключены; встроенная GGUF-модель не входит в APK.
