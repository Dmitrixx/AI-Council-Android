# Сторонние компоненты, сервисы и лицензии

## Tesseract4Android

- Компонент: `cz.adaptech.tesseract4android:tesseract4android:4.9.0`
- Назначение: локальный OCR движок Android.
- Репозиторий: https://github.com/adaptech-cz/Tesseract4Android
- Лицензия: Apache License 2.0. Полный текст находится в `LICENSE-APACHE-2.0.txt`.

## Языковые модели Tesseract

- Репозиторий: https://github.com/tesseract-ocr/tessdata_fast
- Файлы `rus.traineddata` и `ukr.traineddata` из закреплённой версии данных `4.1.0`, лицензия Apache License 2.0 согласно репозиторию.
- Модели загружаются только по запросу пользователя и не включены в ZIP/APK.

## Google ML Kit Text Recognition

- Компонент: `com.google.mlkit:text-recognition:16.0.1`
- Назначение: OCR латинского письма и прежний OCR fallback.
- Документация: https://developers.google.com/ml-kit/vision/text-recognition/v2/languages

## AndroidX ExifInterface

- Компонент: `androidx.exifinterface:exifinterface:1.4.2`
- Назначение: чтение EXIF orientation фото перед локальным OCR.
- Документация и выпуск: https://developer.android.com/jetpack/androidx/releases/exifinterface
- Лицензия: Apache License 2.0.

## PDFBox-Android

- Компонент: `com.tom-roush:pdfbox-android:2.0.27.0`
- Назначение: извлечение текстового слоя PDF локально.
- Лицензия: Apache License 2.0.

## Внешние поисковые сервисы

- Brave Search API: https://api-dashboard.search.brave.com/api-reference/web/search/get — используется только после настройки токена пользователем. Запрос поиска и токен передаются Brave Search.
- MediaWiki Action API: https://www.mediawiki.org/wiki/API:Search/en — поиск статей; запрос отправляется соответствующему языковому домену Wikipedia.
- DuckDuckGo HTML Search: https://html.duckduckgo.com/html/ — упоминание для истории прежних версий; в версии 2.0.0 HTML scraping удалён и этот endpoint больше не вызывается.
- Tavily Search API: https://app.tavily.com/ — необязательный внешний поисковый API; ключ вводится пользователем, сохраняется шифрованным и отправляется заголовком `Authorization: Bearer …` только на `https://api.tavily.com/search`.
- DuckDuckGo Instant Answer API: https://api.duckduckgo.com/ — открытые справочные ответы; запрос отправляется DuckDuckGo.
- Для двух верхних уникальных результатов возможна прямая загрузка HTML первоисточника, если URL, DNS-адрес и HTTP-ответ проходят ограничения безопасности. Содержимое остаётся недоверенным; часть сайтов не позволяет извлечение.

Это внешние сетевые сервисы, а не включённые SDK/библиотеки. Приложение показывает источники, полученные поиском, но не гарантирует независимое подтверждение их достоверности.

## Локальные LLM endpoints

- Ollama OpenAI compatibility: https://github.com/ollama/ollama/blob/main/docs/api/openai-compatibility.mdx
- LM Studio local server endpoint должен быть настроен пользователем. Серверы и веса моделей не входят в APK и имеют собственные условия использования/лицензирования.
