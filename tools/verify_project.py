#!/usr/bin/env python3
"""Offline static verification for AI Council 2.1.0; not a substitute for Android build/device tests."""
from pathlib import Path
import sys
ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'app/src/main/java/com/aicouncil/app/MainActivity.java'
ENDPOINT_POLICY = ROOT / 'app/src/main/java/com/aicouncil/app/EndpointPolicy.java'
LOCAL_AI_POLICY = ROOT / 'app/src/main/java/com/aicouncil/app/LocalAiPolicy.java'
PROVIDER_FINGERPRINT = ROOT / 'app/src/main/java/com/aicouncil/app/ProviderConfigFingerprint.java'
WEB_SEARCH_POLICY = ROOT / 'app/src/main/java/com/aicouncil/app/WebSearchPolicy.java'
EXTRACTOR = ROOT / 'app/src/main/java/com/aicouncil/app/DocumentTextExtractor.java'
MANIFEST = ROOT / 'app/src/main/AndroidManifest.xml'
GRADLE = ROOT / 'app/build.gradle'
README = ROOT / 'README_RU.md'
CHANGELOG = ROOT / 'CHANGELOG_RU.md'
DEPENDENCIES = ROOT / 'DEPENDENCIES_RU.md'
ROLE_POLICY = ROOT / 'app/src/main/java/com/aicouncil/app/ExpertRolePolicy.java'
TRUNCATED_POLICY = ROOT / 'app/src/main/java/com/aicouncil/app/TruncatedAnswerPolicy.java'
CHAT_CLIENT = ROOT / 'app/src/main/java/com/aicouncil/app/ChatCompletionsClient.java'
LOCAL_DATA_STORE = ROOT / 'app/src/main/java/com/aicouncil/app/LocalDataStore.java'
EXECUTION_ENGINE = ROOT / 'app/src/main/java/com/aicouncil/app/CouncilExecutionEngine.java'
CALCULATOR_POLICY = ROOT / 'app/src/main/java/com/aicouncil/app/DeterministicCalculatorPolicy.java'
CALCULATOR_TEST = ROOT / 'tools/desktop_smoke/src/com/aicouncil/app/DeterministicCalculatorPolicySmokeTest.java'
RESULT_MODEL = ROOT / 'app/src/main/java/com/aicouncil/app/CouncilResultModel.java'
OCR_POLICY = ROOT / 'app/src/main/java/com/aicouncil/app/OcrModelIntegrityPolicy.java'
OCR_MANAGER = ROOT / 'app/src/main/java/com/aicouncil/app/OcrModelManager.java'
OCR_POLICY_TEST = ROOT / 'tools/desktop_smoke/src/com/aicouncil/app/OcrModelIntegrityPolicySmokeTest.java'
OCR_MANAGER_TEST = ROOT / 'tools/desktop_smoke/src/com/aicouncil/app/OcrModelManagerSmokeTest.java'
PROVIDER_MODELS_CLIENT = ROOT / 'app/src/main/java/com/aicouncil/app/ProviderModelsClient.java'
PROVIDER_MODELS_TEST = ROOT / 'tools/desktop_smoke/src/com/aicouncil/app/ProviderModelsClientSmokeTest.java'
WEB_SEARCH_SERVICE = ROOT / 'app/src/main/java/com/aicouncil/app/WebSearchService.java'
OCR_PROCESSOR = ROOT / 'app/src/main/java/com/aicouncil/app/ocr/OcrDocumentProcessor.java'
OCR_INPUT_POLICY = ROOT / 'app/src/main/java/com/aicouncil/app/ocr/OcrInputPolicy.java'
OCR_INPUT_TEST = ROOT / 'tools/desktop_smoke/src/com/aicouncil/app/ocr/OcrInputPolicySmokeTest.java'

PRIVACY_POLICY = ROOT / 'app/src/main/java/com/aicouncil/app/PromptPrivacyPolicy.java'
PRIVACY_TEST = ROOT / 'tools/desktop_smoke/src/com/aicouncil/app/PromptPrivacyPolicySmokeTest.java'
THEME_POLICY = ROOT / 'app/src/main/java/com/aicouncil/app/UiThemePolicy.java'
PROVIDER_ERROR_FORMATTER = ROOT / 'app/src/main/java/com/aicouncil/app/ProviderErrorFormatter.java'
SECRET_REDACTOR = ROOT / 'app/src/main/java/com/aicouncil/app/SecretRedactor.java'
GITIGNORE = ROOT / '.gitignore'
STYLES = ROOT / 'app/src/main/res/values/styles.xml'
NIGHT_STYLES = ROOT / 'app/src/main/res/values-night/styles.xml'
LIGHT_SPLASH = ROOT / 'app/src/main/res/drawable/splash_background_light.xml'
TEST_RUNNER = ROOT / 'tools/desktop_smoke/run.sh'
checks=[]
def check(name, condition): checks.append((name, bool(condition)))
try:
    src=JAVA.read_text(encoding='utf-8'); extractor=EXTRACTOR.read_text(encoding='utf-8')
    search_service=WEB_SEARCH_SERVICE.read_text(encoding='utf-8'); search_src=src + '\n' + search_service
    ocr_processor=OCR_PROCESSOR.read_text(encoding='utf-8'); ocr_input_policy=OCR_INPUT_POLICY.read_text(encoding='utf-8'); ocr_src=src + '\n' + ocr_processor
    engine=EXECUTION_ENGINE.read_text(encoding='utf-8')
    endpoint_policy=ENDPOINT_POLICY.read_text(encoding='utf-8'); web_policy=WEB_SEARCH_POLICY.read_text(encoding='utf-8')
    local_ai_policy=LOCAL_AI_POLICY.read_text(encoding='utf-8')
    provider_fingerprint=PROVIDER_FINGERPRINT.read_text(encoding='utf-8')
    secret_redactor=SECRET_REDACTOR.read_text(encoding='utf-8')
    gitignore=GITIGNORE.read_text(encoding='utf-8')
    manifest=MANIFEST.read_text(encoding='utf-8'); gradle=GRADLE.read_text(encoding='utf-8')
    readme=README.read_text(encoding='utf-8'); changelog=CHANGELOG.read_text(encoding='utf-8')
    deps=DEPENDENCIES.read_text(encoding='utf-8'); runner=TEST_RUNNER.read_text(encoding='utf-8')
    role_policy=ROLE_POLICY.read_text(encoding='utf-8'); truncated_policy=TRUNCATED_POLICY.read_text(encoding='utf-8')
    theme_policy=THEME_POLICY.read_text(encoding='utf-8'); error_formatter=PROVIDER_ERROR_FORMATTER.read_text(encoding='utf-8'); styles=STYLES.read_text(encoding='utf-8'); night_styles=NIGHT_STYLES.read_text(encoding='utf-8'); light_splash=LIGHT_SPLASH.read_text(encoding='utf-8')
except Exception as e:
    print('FAIL: Не удалось прочитать проект:', e); sys.exit(2)
check('Версия Android-приложения — 2.22.0 / code 89 / API 36', "versionCode 89" in gradle and "versionName '2.22.0'" in gradle and 'compileSdk 36' in gradle and 'targetSdk 36' in gradle)
check('Android Gradle Plugin 9.4.0 и Java-only модуль без Kotlin compiler task', "version '9.4.0'" in (ROOT/'build.gradle').read_text(encoding='utf-8') and 'enableKotlin = false' in gradle)
check('Версия Java — 2.22.0', 'APP_VERSION = "2.22.0"' in src)
check('Облачные chat-запросы используют OkHttp и SSE stream=true вне Activity', CHAT_CLIENT.exists() and 'OkHttpClient' in CHAT_CLIENT.read_text(encoding='utf-8') and 'body.put("stream", true)' in src and 'HttpURLConnection' not in CHAT_CLIENT.read_text(encoding='utf-8'))
check('Получение списка моделей вынесено из Activity в ограниченный OkHttp-клиент', PROVIDER_MODELS_CLIENT.exists() and 'providerModelsClient.fetch(base, apiKey, localOnlyRequested' in src and 'HttpURLConnection' not in src)
check('Оркестрация поиска, ранжирование и HTML извлечение вынесены из Activity', 'private String searchBrave' not in src and 'private String rankSearchResults' not in src and 'public String search(' in search_service and 'android.' not in search_service and 'webSearchService.search(rawQuery, contextMode, braveKey, tavilyKey' in src)
check('Клиент каталога отключает редиректы, ограничивает ответ и поддерживает отмену', all(item in PROVIDER_MODELS_CLIENT.read_text(encoding='utf-8') for item in ['followRedirects(false)', 'followSslRedirects(false)', 'MAX_RESPONSE_BYTES = 250_000', 'activeCalls', 'cancelAll()']))
check('Ключ не отправляется на local/LAN /models, ошибка очищается от секрета', 'LocalAiPolicy.shouldSendAuthorization(baseUrl, apiKey)' in PROVIDER_MODELS_CLIENT.read_text(encoding='utf-8') and 'SecretRedactor.redact' in PROVIDER_MODELS_CLIENT.read_text(encoding='utf-8'))
check('Desktop smoke покрывает транспорт каталога моделей и локальную границу секретов', 'ProviderModelsClient.java' in runner and 'ProviderModelsClientSmokeTest' in runner and PROVIDER_MODELS_TEST.exists())
check('История, проекты и черновик сохраняются в SQLite с миграцией прежних ключей', LOCAL_DATA_STORE.exists() and 'SQLiteOpenHelper' in LOCAL_DATA_STORE.read_text(encoding='utf-8') and 'migrateLegacy(prefs)' in src and 'history' in LOCAL_DATA_STORE.read_text(encoding='utf-8') and 'projects' in LOCAL_DATA_STORE.read_text(encoding='utf-8'))
check('Ключи и роли экспертов не входят в миграционный allow-list базы', 'String[] keys = {"history", "projects", "draftPrompt", "draftFollowUp", "draftMode"}' in LOCAL_DATA_STORE.read_text(encoding='utf-8') and 'expertRole' not in LOCAL_DATA_STORE.read_text(encoding='utf-8'))
check('Потоковые фрагменты сохраняются и восстанавливаются как прерванная задача', all(x in src for x in ['activePartial', 'recoverInterruptedTaskIfNeeded()', 'Задача прервана системой', 'Частичный результат сохранён']))
check('Результат формируется через отдельную структурированную модель и имеет автономные Java-тесты', RESULT_MODEL.exists() and 'CouncilResultModel.parse' in src and 'CouncilResultModelSmokeTest' in runner and (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/CouncilResultModelSmokeTest.java').exists())
check('Явный арифметический запрос проверяется локальным калькулятором до передачи совету', CALCULATOR_POLICY.exists() and 'DeterministicCalculatorPolicy.evaluateExplicitRequest(request.task)' in engine and 'DeterministicCalculatorPolicySmokeTest' in runner and CALCULATOR_TEST.exists())
check('Стриминг имеет per-expert колонки до завершения генерации', 'initializeStreamingCards(mode, true)' in src and 'onApiToken(expertSlot, token)' in src and 'detail.setText(buffer.length() == 0 ? "Ожидает потокового ответа…" : buffer.toString())' in src)
check('Документация отражает 2.22.0 и содержит предыдущие версии', readme.startswith('# AI Council 2.22.0') and '# 2.21.0' in changelog and '# 2.20.0' in changelog)
check('Git игнорирует Android build output и локальные ключи подписи', all(x in gitignore for x in ['**/build/', 'local.properties', '*.jks', '*.keystore', '.env']))
check('Настройка темы сохраняет system/light/dark и безопасно нормализует неизвестные значения', all(x in theme_policy for x in ['SYSTEM = "system"', 'LIGHT = "light"', 'DARK = "dark"', 'normalizeMode', 'useLightTheme']))
check('Выбор темы доступен в настройках и сохраняется локально', 'Цветовая тема AI Council' in src and 'putString("appTheme", selectedMode)' in src and 'modeIndex(activeTheme)' in src)
theme_listener_chunk = src.split('themeSpinner.setOnItemSelectedListener', 1)[1].split('settingsCard.addView(text("Подключения ИИ', 1)[0]
check('Смена темы выполняется в той же Activity и не блокируется во время запроса', 'applyThemeInPlace(selectedMode, true)' in theme_listener_chunk and 'android:configChanges="orientation|screenSize|keyboardHidden|uiMode"' in manifest and 'private void applyThemeInPlace' in src)
check('Черновик и scroll position сохраняются перед пересозданием темы', 'persistDraftNow();' in src and 'ui_restore_scroll_y' in src and 'restoreY >= 0' in src)
check('Есть отдельные фиксированные светлая/тёмная темы и системные variants без UI-библиотек', 'name="AppThemeLight"' in styles and 'name="AppThemeDark"' in styles and 'Theme.Material.Light.NoActionBar' in styles and 'com.google.android.material' not in gradle)
check('Системная тема задаётся до старта Activity через values и values-night ресурсы', 'name="AppTheme"' in styles and 'name="AppTheme"' in night_styles and 'UiThemePolicy.SYSTEM.equals(savedTheme)' in src)
check('Светлая тема использует светлый launch window background', 'splash_background_light' in styles and 'app_splash_background_light' in light_splash)
check('Светлая тема адаптирует legacy surfaces, strokes и text colors', all(x in theme_policy for x in ['surfaceColor', 'strokeColor', 'textColor', 'contrastRatio']))
check('Прозрачные поверхности сохраняются, а сохранённые статусы ИИ приводятся к новой палитре', '(color >>> 24) == 0' in theme_policy and 'int foreground = UiThemePolicy.textColor(color, lightTheme)' in src)
check('Статусная и навигационная панели переключают контраст системных иконок', 'View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR' in src and 'View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR' in src)
check('Theme policy имеет изолированный desktop smoke test', 'UiThemePolicy.java' in runner and 'UiThemePolicySmokeTest' in runner and (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/UiThemePolicySmokeTest.java').exists())

check('Статус каждого ИИ отображается отдельной диагностической строкой', 'providerStatus = new TextView[4]' in src and 'providerStatus[i] = text("○  Не проверено"' in src)
check('Каталог моделей и проверка chat endpoint имеют раздельные статусы', 'providerModelsStatus = new TextView[4]' in src and 'storeProviderModelsResult(index' in src and 'storeProviderTestResult(index' in src)
check('Статус каталога моделей восстанавливается отдельно', 'providerModelsStatusMessage' in src and 'providerModelsStatusColor' in src and 'Список моделей не проверен' in src)
check('Результат каталога моделей привязан к URL и отправляемому API-ключу', 'providerModelsConfigFingerprint' in src and 'storeProviderModelsResult(index' in src and 'refreshProviderModelsStatus(index)' in src and 'effectiveKey = LocalAiPolicy.shouldSendAuthorization(normalized, configuredKey)' in src)
check('Диагностика привязана к конфигурации, которая действительно тестировалась', 'providerTestFingerprint' in src and 'testedFingerprint.equals(providerConfigFingerprint(index))' in src and 'Параметры изменены · нужна перепроверка' in src)
check('Изменения полей проверяются при вводе, не по потере фокуса', 'addTextChangedListener(changed)' in src and 'setOnFocusChangeListener(changed)' not in src)
check('Fingerprint не хранит API-ключ в открытом виде', 'MessageDigest.getInstance("SHA-256")' in provider_fingerprint and 'return k;' not in provider_fingerprint and 'secret-key' not in provider_fingerprint)
check('Fingerprint utility входит в desktop smoke runner', 'ProviderConfigFingerprint.java' in runner and 'ProviderConfigFingerprintSmokeTest' in runner)
check('HTTP ошибки провайдера преобразуются в понятные рекомендации', all(x in error_formatter for x in ['case 401:', 'case 403:', 'case 404:', 'case 429:', 'case 504:', 'sanitizeDetail']))
check('Детали ошибок очищаются от секретов и ограничиваются по длине', 'SecretRedactor.redact(responseBody, apiKey)' in error_formatter and 'MAX_DETAIL_CHARS = 1200' in error_formatter)
check('Секреты разных форматов проходят через централизованный redactor', all(x in secret_redactor for x in ['OPENAI_STYLE_KEY', 'GOOGLE_API_KEY', 'GROQ_API_KEY', 'XAI_API_KEY', 'HUGGINGFACE_TOKEN', 'GITHUB_TOKEN', 'PRIVATE_KEY_BLOCK']))
check('SecretRedactor включён в Android-модуль и desktop smoke runner', 'SecretRedactor.java' in runner and 'SecretRedactorSmokeTest' in runner and (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/SecretRedactorSmokeTest.java').exists())
check('Диагностика скрывает поля credentials и URL query secrets', all(x in secret_redactor for x in ['LABELED_SECRET', 'QUERY_KEY', 'BEARER']))
check('HTTP ошибки форматируются выделенным streaming-клиентом через ProviderErrorFormatter', 'ProviderErrorFormatter.formatHttpError(code, errorBody, model, apiKey)' in (ROOT/'app/src/main/java/com/aicouncil/app/ChatCompletionsClient.java').read_text(encoding='utf-8'))
api_chunk = src.split('private String callApiInternal(String endpoint, String model, String key, JSONArray conversation, String userPrompt, boolean useCompletionTokenField, int expertSlot, String roleInstruction)', 1)[1].split('private void renderResultCards', 1)[0]
check('Отмена stream-запроса закрывает OkHttp Call и возвращает частичный текст отдельно от сетевой ошибки', 'if (isCancelled(cancellation) || call.isCanceled())' in (ROOT/'app/src/main/java/com/aicouncil/app/ChatCompletionsClient.java').read_text(encoding='utf-8') and 'chatClient.cancelAll()' in src and 'streamed.cancelled' in api_chunk)
check('Обработка Future различает interruption, timeout и cancellation вне Activity', all(x in engine for x in ['catch (InterruptedException interrupted)', 'catch (TimeoutException timeout)', 'catch (CancellationException cancelled)', 'catch (ExecutionException error)']))
check('ProviderErrorFormatter имеет отдельный desktop smoke test', 'ProviderErrorFormatter.java' in runner and 'ProviderErrorFormatterSmokeTest' in runner and (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/ProviderErrorFormatterSmokeTest.java').exists())
check('Markdown formatter включён в Android приложение и автономный runner', (ROOT/'app/src/main/java/com/aicouncil/app/ResultReportFormatter.java').exists() and 'ResultReportFormatter.java' in runner and 'ResultReportFormatterSmokeTest' in runner)
check('Глубокий анализ включает второй раунд взаимной критики вне Activity', 'r.mode == 3' in engine and 'CouncilAnalysisPolicy.buildCritiquePrompt' in engine and 'ВЗАИМНАЯ КРИТИКА' in engine and 'CouncilExecutionEngine.java' in runner and 'CouncilExecutionEngineSmokeTest' in runner)
check('Политика взаимной критики протестирована отдельным smoke test', (ROOT/'app/src/main/java/com/aicouncil/app/CouncilAnalysisPolicy.java').exists() and 'CouncilAnalysisPolicy.java' in runner and 'CouncilAnalysisPolicySmokeTest' in runner)
analysis = (ROOT/'app/src/main/java/com/aicouncil/app/CouncilAnalysisPolicy.java').read_text(encoding='utf-8')
council_text = engine + "\n" + analysis
check('Координатор требует обосновывать фактические выводы источниками/расчётами/данными', 'ОСНОВАНИЯ И ИСТОЧНИКИ' in council_text and 'Не придумывай цитаты, URL' in council_text)
check('Критика не считается доказательством и учитывает ошибки этапов', 'не является доказательством' in (ROOT/'app/src/main/java/com/aicouncil/app/CouncilAnalysisPolicy.java').read_text(encoding='utf-8') and 'critiqueAFailed || critiqueBFailed' in engine)
check('Экспорт Markdown запускает системный SAF picker и использует расширение .md', 'exportCurrentResultAsMarkdown()' in src and 'intent.setType("text/markdown")' in src and 'resultFileBase(task) + ".md"' in src and 'REQUEST_SAVE_MARKDOWN' in src)
check('Markdown отчёт включает дату, версию, стратегию, задачу и полный результат', 'ResultReportFormatter.toMarkdown(task, getCurrentResultStrategy(), answer, APP_VERSION, createdAt)' in src and '## Результат совета' in (ROOT/'app/src/main/java/com/aicouncil/app/ResultReportFormatter.java').read_text(encoding='utf-8'))
check('Карточка отдельного ответа имеет действие копирования', 'Копировать этот ответ' in src and 'ClipData.newPlainText("AI Council · ответ", blockText)' in src)
check('Экспорт привязан к фактической задаче, а не к редактируемому черновику', 'private String getCurrentResultTask()' in src and 'lastCompletedTask.trim()' in src and 'String task = getCurrentResultTask();' in src)
check('Экспортная подсказка предупреждает, что параметры подключения не добавляются автоматически', 'Параметры подключения и ключи из настроек автоматически не добавляются' in src)

check('Проверка подключения показывает длительность и результат', 'Подключение работает' in src and 'Ошибка подключения' in src and 'elapsedMs / 1000.0' in src)
check('Изменение URL, модели или ключа помечает результат проверки устаревшим', 'attachProviderStatusListeners()' in src and 'Параметры изменены · нужна перепроверка' in src)
check('Незавершённая проверка сохраняется и восстанавливается как прерванная', 'providerStatusMessage' in src and 'Проверка прервана · запустите снова' in src)
check('GitHub Actions запускает тесты и сборку Android APK', (ROOT/'.github/workflows/android-build.yml').exists() and 'gradle --no-daemon --stacktrace assembleDebug' in (ROOT/'.github/workflows/android-build.yml').read_text(encoding='utf-8'))
check('Android CI сохраняет APK как workflow artifact', 'actions/upload-artifact@v7' in (ROOT/'.github/workflows/android-build.yml').read_text(encoding='utf-8') and 'app-debug.apk' in (ROOT/'.github/workflows/android-build.yml').read_text(encoding='utf-8'))

check('Android CI использует Gradle 9.6.0 и Android SDK 36', "gradle-version: '9.6.0'" in (ROOT/'.github/workflows/android-build.yml').read_text(encoding='utf-8') and 'platforms;android-36' in (ROOT/'.github/workflows/android-build.yml').read_text(encoding='utf-8') and 'build-tools;36.0.0' in (ROOT/'.github/workflows/android-build.yml').read_text(encoding='utf-8'))
check('Android CI uses current major versions of checkout/Java/Gradle/artifact actions', all(x in (ROOT/'.github/workflows/android-build.yml').read_text(encoding='utf-8') for x in ['actions/checkout@v7','actions/setup-java@v6','gradle/actions/setup-gradle@v6','actions/upload-artifact@v7']))
check('Инструкция получения APK через GitHub Actions включена в проект', (ROOT/'GITHUB_ACTIONS_RU.md').exists() and 'Artifacts' in (ROOT/'GITHUB_ACTIONS_RU.md').read_text(encoding='utf-8'))

check('Предварительный сканер не используется', not (ROOT/'app/src/main/java/com/aicouncil/app/PrivacyScanner.java').exists() and 'PrivacyScanner' not in src and 'Предварительная проверка конфиденциальности' not in src and 'проверка возможных секретов' not in src)
check('Запуск задания не вызывает диалог предварительной проверки', 'private void runCouncil()' in src and 'runCouncilRequest();' in src and 'Проверьте конфиденциальность' not in src)
check('Разрешение INTERNET присутствует', 'android.permission.INTERNET' in manifest)
check('Cleartext не разрешён глобально; есть сетевой security config и endpoint policy', 'android:usesCleartextTraffic="true"' not in manifest and 'networkSecurityConfig="@xml/network_security_config"' in manifest and 'cleartextTrafficPermitted="false"' in (ROOT/'app/src/main/res/xml/network_security_config.xml').read_text() and 'return isLocalUnauthenticatedEndpoint(trimmed)' in endpoint_policy and 'https' in endpoint_policy)
check('Приложение имеет адаптивную и совместимую иконки запуска', 'android:icon="@mipmap/ic_launcher"' in manifest and (ROOT/'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml').exists() and (ROOT/'app/src/main/res/mipmap-mdpi/ic_launcher.png').exists())
check('Фирменная тема и splash background заданы ресурсами Android', 'android:theme="@style/AppTheme"' in manifest and (ROOT/'app/src/main/res/values/styles.xml').exists() and (ROOT/'app/src/main/res/drawable/splash_background.xml').exists())
check('Группа импорта документов, файлов и фото видима и раскрывается', 'ui_attachments_expanded' in src and 'attachmentOptions.setVisibility(attachmentsExpanded ? View.VISIBLE : View.GONE)' in src and 'Скрыть инструменты импорта' in src and 'attachmentsToggle.setVisibility(View.GONE)' not in src)
check('Дополнительные настройки веб-поиска свёрнуты отдельно от независимого переключателя поиска', 'ui_web_details_expanded' in src and 'webDetails.setVisibility(webDetailsExpanded ? View.VISIBLE : View.GONE)' in src and 'Искать в Интернете · независимо от режима ИИ' in src)
check('Нижняя навигация управляет отдельными экранами', 'LinearLayout[] screens = {workspaceScreen, historyScreen, connectionsScreen}' in src and 'screens[si].setVisibility(si == initialScreen ? View.VISIBLE : View.GONE)' in src)
check('Главный экран использует современную бренд-шапку и короткую анимацию', 'Ваш ИИ-штаб' in src and 'AI COUNCIL  ·  МУЛЬТИАГЕНТНЫЙ ИИ' in src and 'setDuration(200)' in src and 'PERSONAL AI WORKSPACE' not in src)
check('Режимы показаны чипами, а не видимым Spinner', 'ChipFlowLayout modeChips' in src and 'modeSpinner.setVisibility(View.GONE)' in src)
check('Импорт и веб-поиск открываются отдельными диалогами', 'showImportSheet()' in src and 'showSearchSheet()' in src)
check('Нижняя навигация переключает отдельные экраны', 'screens[j].setVisibility(active ? View.VISIBLE : View.GONE)' in src and 'putInt("selectedScreen", ix)' in src)
check('Основной акцент использует современный мятный teal', 'Color.rgb(110, 231, 205)' in src and 'Color.rgb(111, 245, 211)' not in src)
check('Java 8 compatibility настроена', 'sourceCompatibility JavaVersion.VERSION_1_8' in gradle and 'targetCompatibility JavaVersion.VERSION_1_8' in gradle)
check('PdfBox-Android указан', 'com.tom-roush:pdfbox-android:2.0.27.0' in gradle)
check('ML Kit OCR указан и ограничение кириллицы описано', 'com.google.mlkit:text-recognition:16.0.1' in gradle and 'кириллиц' in deps.lower())
check('Кнопки одиночного и пакетного импорта присутствуют', 'Добавить один файл · TXT / PDF / DOCX / XLSX' in src and 'Выбрать несколько файлов и фото' in src)
check('Storage Access Framework используется и разрешает пакетный выбор', 'Intent.ACTION_OPEN_DOCUMENT' in src and 'REQUEST_OPEN_TEXT' in src and 'REQUEST_OPEN_MATERIALS' in src and 'Intent.EXTRA_ALLOW_MULTIPLE, true' in src)
check('Одиночный и множественный выбор файлов обрабатываются', 'handleTaskTextImport(data.getData())' in src and 'selectedMaterialUris(data)' in src and 'handleTaskMaterialsImport' in src and 'data.getClipData()' in src)
check('Документы ограничены 10 МБ', 'MAX_DOCUMENT_IMPORT_BYTES = 10 * 1024 * 1024' in src)
check('Пакетный импорт ограничен восемью материалами и 70 000 символами', 'MAX_SELECTED_FILES = 8' in (ROOT/'app/src/main/java/com/aicouncil/app/MaterialImportPolicy.java').read_text(encoding='utf-8') and 'MAX_BATCH_TEXT_CHARS = 70_000' in (ROOT/'app/src/main/java/com/aicouncil/app/MaterialImportPolicy.java').read_text(encoding='utf-8') and 'MaterialImportPolicy.MAX_SELECTED_FILES' in src)
check('Пакетный импорт распознаёт фотографии локальным OCR с выбором языка и объединяет материалы в один предпросмотр', 'Язык распознавания текста на фото' in src and 'OcrDocumentProcessor.Input.image(uri, photoOcrLanguage)' in src and 'MaterialImportPolicy.isImageFile(safeName, mimeType)' in src and 'showExtractedTextPreview("BATCH"' in src and 'hasUsableCyrillicModel(language)' in src)
check('Пакетный импорт маркирует источники как недоверенные данные и проверяет общий размер текста', 'MaterialImportPolicy.formatMaterialBlock' in src and 'MaterialImportPolicy.canAppend' in src and 'недоверенными данными' in (ROOT/'app/src/main/java/com/aicouncil/app/MaterialImportPolicy.java').read_text(encoding='utf-8'))
check('MaterialImportPolicy и его smoke-тест включены в desktop runner', 'MaterialImportPolicy.java' in runner and 'MaterialImportPolicySmokeTest' in runner and (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/MaterialImportPolicySmokeTest.java').exists())

check('Обычный текст ограничен 256 КБ', 'MAX_TEXT_IMPORT_BYTES = 256 * 1024' in src)
check('UTF-8 и Windows-1251 fallback поддерживаются', 'StandardCharsets.UTF_8.newDecoder()' in src and 'windows-1251' in src)
check('DocumentTextExtractor подключён', 'DocumentTextExtractor.extract(getApplicationContext(), fileBytes, displayName)' in src)
check('PDF ограничен 25 страницами', 'MAX_PDF_PAGES = 25' in extractor and 'Math.min(pages, MAX_PDF_PAGES)' in extractor)
check('PDFBox resource loader инициализируется', 'PDFBoxResourceLoader.init(context.getApplicationContext())' in extractor)
check('Обычный PDF проходит через гибридный локальный processor с выбранным лимитом, DOCX/XLSX остаются у DocumentTextExtractor', 'OcrDocumentProcessor.Input.pdf(fileBytes, displayName, \"eng\", ocrPdfPageLimit())' in src and 'DocumentTextExtractor.extract(getApplicationContext(), fileBytes, displayName)' in src)
check('OCR image picker и handler присутствуют', 'REQUEST_OPEN_IMAGE' in src and 'handleTaskImageImport(data.getData())' in src)
check('OCR использует ML Kit Latin recognizer вне Activity', 'TextRecognition.getClient(' in ocr_processor and 'TextRecognizerOptions.DEFAULT_OPTIONS' in ocr_processor and 'TextRecognition.getClient(' not in src)
check('Tesseract4Android для OCR кириллицы подключён через отдельный processor', 'cz.adaptech.tesseract4android:tesseract4android:4.9.0' in gradle and 'import com.googlecode.tesseract.android.TessBaseAPI;' in ocr_processor and 'TessBaseAPI' not in src)
check('JitPack repository настроен', 'https://jitpack.io' in (ROOT/'settings.gradle').read_text(encoding='utf-8'))
check('Отдельная кнопка OCR кириллицы доступна в UI', 'OCR кириллицы · русский / українська' in src and 'chooseCyrillicOcrLanguage()' in src)
check('Пользователь выбирает русский или украинский язык', '"Русский", "Українська"' in src and 'which == 1 ? "ukr" : "rus"' in src)
check('Перед первой загрузкой модели показывается согласие', 'Загрузка модели OCR' in src and 'setPositiveButton("Выбрать изображение"' in src and 'изображение останется на устройстве' in src)
ocr_policy_text = OCR_POLICY.read_text(encoding='utf-8') if OCR_POLICY.exists() else ''
ocr_manager_text = OCR_MANAGER.read_text(encoding='utf-8') if OCR_MANAGER.exists() else ''
check('Модели OCR загружаются только по HTTPS из закреплённой версии через OkHttp без редиректов', 'https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/' in ocr_manager_text and 'OcrModelIntegrityPolicy.VERSION' in ocr_manager_text and '.followRedirects(false)' in ocr_manager_text and '.followSslRedirects(false)' in ocr_manager_text and 'http.newCall(request)' in ocr_manager_text)
check('Каталог OCR моделей закрепляет размер и SHA-512 обеих версий языка', OCR_POLICY.exists() and 'RUS_BYTES = 3_861_738L' in ocr_policy_text and 'UKR_BYTES = 3_825_102L' in ocr_policy_text and 'RUS_SHA512' in ocr_policy_text and 'UKR_SHA512' in ocr_policy_text)
check('Проверка OCR модели закрыта по умолчанию для неизвестного языка/хеша', 'expectedLength > 0 && length == expectedLength' in ocr_policy_text and 'expectedDigest.equals' in ocr_policy_text)
check('Существующая OCR модель проверяется по контрольной сумме, не только по размеру', 'OcrModelIntegrityPolicy.sha512(file)' in ocr_manager_text and 'OcrModelIntegrityPolicy.verify(language, file.length()' in ocr_manager_text)
check('Загружаемый OCR пакет проверяется по точному размеру до установки', 'declaredLength >= 0 && declaredLength != expectedLength' in ocr_manager_text and 'total != expectedLength' in ocr_manager_text)
check('Загружаемый OCR пакет проверяется по SHA-512 до замены установленной модели', 'OcrModelIntegrityPolicy.sha512(temp)' in ocr_manager_text and 'OcrModelIntegrityPolicy.verify(language, total, actualSha512)' in ocr_manager_text and 'Контрольная сумма модели OCR не совпала' in ocr_manager_text)
check('OCR integrity policy и SHA-512 vector test запускаются в desktop smoke suite', OCR_POLICY_TEST.exists() and 'OcrModelIntegrityPolicy.java' in runner and 'OcrModelIntegrityPolicySmokeTest' in runner)
check('OCR модельный менеджер вынесен из Activity и имеет отдельные smoke tests', OCR_MANAGER.exists() and 'new OcrModelManager(getFilesDir())' in src and 'OcrModelManagerSmokeTest' in runner and OCR_MANAGER_TEST.exists())
check('Загрузка модели ограничена по размеру и исходная версия сохранена', 'MAX_MODEL_BYTES = 8 * 1024 * 1024' in ocr_manager_text and 'MIN_MODEL_BYTES = 1_000_000' in ocr_manager_text and 'total > MAX_MODEL_BYTES' in ocr_manager_text and '4.1.0' in ocr_policy_text)
check('Файл модели сначала скачивается во временный файл', 'language + ".traineddata.download"' in ocr_manager_text and 'temp.renameTo(model)' in ocr_manager_text and 'if (!installed && temp.exists()) temp.delete()' in ocr_manager_text)
check('Модели хранятся в закрытом каталоге приложения', 'new File(filesDirectory, "tesseract")' in ocr_manager_text and 'tessdata' in ocr_manager_text)
check('OCR image/PDF processing moved to ocr package and Activity keeps only UI dispatch', OCR_PROCESSOR.exists() and 'public Result process(Input input' in ocr_processor and 'private Result recognizeImage' in ocr_processor and 'private Result recognizePdf' in ocr_processor and 'OcrDocumentProcessor' in src and 'TextRecognition.getClient(' not in src and 'PdfRenderer' not in src)
check('Hybrid PDF reads page text layer before OCR and renders only pages without extractable text', 'extractTextLayer(textDocument, pageIndex)' in ocr_processor and 'OcrInputPolicy.hasTextLayer(pageText)' in ocr_processor and 'renderer.openPage(pageIndex)' in ocr_processor)
check('Hybrid PDF processor has an offline smoke integration test', (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/ocr/OcrDocumentProcessorSmokeTest.java').exists() and 'OcrDocumentProcessorSmokeTest' in runner)
check('PDF processor initialises PDFBox once and applies page/result limits', 'PDFBoxResourceLoader.init(context.getApplicationContext())' in ocr_processor and 'DEFAULT_MAX_PAGES' in ocr_processor and 'MAX_EXTRACTED_CHARS' in ocr_processor)
check('OCR result carries engine, page count and explicit warnings', all(token in ocr_processor for token in ['pagesProcessed', 'warnings', 'engine', 'previewText()']))
check('OCR input policy and autonomous tests are wired into desktop smoke', OCR_INPUT_POLICY.exists() and OCR_INPUT_TEST.exists() and 'OcrInputPolicy.java' in runner and 'OcrInputPolicySmokeTest' in runner and 'hasTextLayer' in ocr_input_policy)
check('OCR processor never uploads source images or PDFs; only the separate model manager downloads tessdata', 'http.' not in ocr_processor.lower() and 'newCall(' not in ocr_processor and 'recognizeImage' in ocr_processor)
check('OCR кириллицы использует выбранную локальную модель вне Activity', 'tess.init(modelManager.getTesseractDirectory().getAbsolutePath(), language, TessBaseAPI.OEM_LSTM_ONLY)' in ocr_processor and 'tess.setImage(bitmap)' in ocr_processor and 'tess.getUTF8Text()' in ocr_processor)
check('Bitmap и нативный OCR освобождаются после обработки', 'tess.recycle()' in ocr_processor and 'bitmap.recycle()' in ocr_processor)
check('OCR кириллицы ограничивает размер изображения', 'MAX_IMAGE_SIDE = 3200' in ocr_input_policy and 'MAX_IMAGE_PIXELS = 12_000_000L' in ocr_input_policy and 'MAX_INPUT_BYTES = 10 * 1024 * 1024' in ocr_input_policy)
check('Результат OCR кириллицы требует подтверждения в предпросмотре', 'showExtractedTextPreview("OCR · кириллица"' in src and 'isCyrillicOcr' in src and 'Добавить в задачу' in src)
check('OCR проверяет размеры изображения до декодирования', 'inJustDecodeBounds = true' in ocr_processor and 'BitmapFactory.decodeStream(input, null, bounds)' in ocr_processor)
check('OCR ограничивает декодированный bitmap по пикселям и применяет EXIF orientation', 'OcrInputPolicy.sampleSize(bounds.outWidth, bounds.outHeight)' in ocr_processor and 'new ExifInterface(input)' in ocr_processor and 'orientBitmap(bitmap, orientation)' in ocr_processor and 'androidx.exifinterface:exifinterface:1.4.2' in gradle)
check('OCR освобождает bitmap и recognizer', 'pageBitmap.recycle()' in ocr_processor and 'recognizer.close()' in ocr_processor and 'if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle()' in ocr_processor)
check('OCR ограничен 45 секундами', '45, TimeUnit.SECONDS' in ocr_processor and 'deadline = System.currentTimeMillis() + 45_000L' in ocr_processor)
check('OCR открывает предпросмотр с ручным подтверждением', 'showExtractedTextPreview("OCR", result.sourceName, result.previewText())' in src and 'Проверка OCR:' in src and 'Добавить в задачу' in src)
check('Импорт документов открывает редактируемый предпросмотр', 'showExtractedTextPreview("FILE", importedName, previewContent)' in src and 'Предпросмотр файла:' in src)
check('Импорт не изменяет задание до подтверждения', 'prompt.setText(merged);' in src[src.find('private void showExtractedTextPreview'):src.find('private String displayNameForUri')])
check('DOCX читает основную XML-часть', 'word/document.xml' in extractor and 'extractDocx' in extractor)
check('XLSX читает shared strings и листы', 'xl/sharedStrings.xml' in extractor and 'xl/worksheets/' in extractor and 'extractXlsx' in extractor)
check('Извлечённый текст ограничен', 'MAX_EXTRACTED_CHARS = 90_000' in extractor)
check('Office ZIP имеет ограничения', 'MAX_ARCHIVE_INFLATED_BYTES = 30L * 1024L * 1024L' in extractor and 'MAX_ARCHIVE_ENTRIES = 3_000' in extractor)
check('XLSX имеет ограничения листов/строк/столбцов', 'MAX_XLSX_SHEETS = 20' in extractor and 'MAX_XLSX_ROWS_PER_SHEET = 300' in extractor and 'MAX_XLSX_COLUMNS_PER_ROW = 64' in extractor)
check('XML parser запрещает DTD и внешние сущности', 'disallow-doctype-decl' in extractor and 'external-general-entities' in extractor and 'external-parameter-entities' in extractor)
check('Импорт текста не запускает задачу автоматически', 'prompt.setText(merged)' in src and 'runCouncil()' not in src[src.find('private void handleTaskTextImport'):src.find('private void exportBackup')])
check('Импортируемый текст маркируется как недоверенные данные', 'недоверенные данные для анализа' in src)
check('API URL валидируются централизованной policy', 'EndpointPolicy.isAllowedApiBase(value)' in src and 'parsed.getProtocol()' in endpoint_policy and 'EndpointPolicy.isAllowedApiBase(address)' in PROVIDER_MODELS_CLIENT.read_text(encoding='utf-8') and 'followRedirects(false)' in PROVIDER_MODELS_CLIENT.read_text(encoding='utf-8'))
backup_export=src[src.find('private void exportBackup'):src.find('private void importBackup')]
check('API-ключи не экспортируются в резервную копию', 'key0' not in backup_export and 'не включаются' in readme)
check('Desktop runner запускает extractor smoke tests', 'ExtractorSmokeTest' in runner and 'PrivacyScannerSmokeTest' not in runner)
check('README сообщает о предпросмотре перед вставкой', 'предпросмотр' in readme.lower() and 'редакт' in readme.lower())
check('Сканированный PDF обрабатывается Android PdfRenderer вне Activity', 'android.graphics.pdf.PdfRenderer' in ocr_processor and 'renderer.openPage(pageIndex)' in ocr_processor and 'android.graphics.pdf.PdfRenderer' not in src)
check('Есть отдельная кнопка OCR кириллицы в сканированном PDF', 'OCR кириллицы в сканированном PDF' in src and 'chooseCyrillicPdfLanguage()' in src)
check('PDF OCR использует выбор языка, выбранный лимит и локальную модель', 'requestCyrillicPdfImport' in src and 'process(OcrDocumentProcessor.Input.scannedPdf(pdfBytes, displayName, language, ocrPdfPageLimit())' in src and 'tess.init(modelManager.getTesseractDirectory().getAbsolutePath(), language, TessBaseAPI.OEM_LSTM_ONLY)' in ocr_processor)
check('PDF OCR имеет отдельный SAF picker', 'REQUEST_OPEN_PDF_CYRILLIC' in src and 'intent.setType("application/pdf")' in src and 'handleCyrillicPdfImport(data.getData(), language)' in src)
check('OCR кириллицы для PDF ограничен пятью страницами и временем', 'DEFAULT_MAX_PAGES = 5' in ocr_input_policy and 'deadline = System.currentTimeMillis() + 45_000L' in ocr_processor and '2_000_000L' in ocr_processor)
check('OCR PDF очищает bitmap, Tesseract и временный файл', 'tempPdf.delete()' in ocr_processor and 'tess.recycle()' in ocr_processor and 'pageBitmap.recycle()' in ocr_processor)
check('Результат PDF OCR открывается в редактируемом предпросмотре', 'showExtractedTextPreview("OCR · кириллица · PDF"' in src)
check('Предпросмотр OCR PDF описывает локальную обработку и фактический лимит', 'isCyrillicPdfOcr' in src and 'сам PDF не отправлялся на сервер' in src and 'Текущий лимит — ' in src and 'ocrPdfPageLimit()' in src)
check('OCR scanned-PDF ограничен первыми 5 страницами', 'DEFAULT_MAX_PAGES = 5' in ocr_input_policy and 'pagesToProcess(pageCount, maxPages)' in ocr_processor)
check('Лимит страниц PDF выбирается в UI: 5 по умолчанию или до 20', 'showOcrPdfPageLimitDialog' in src and 'ocr_pdf_max_pages' in src and 'ocrPdfPageLimit()' in src and 'clampPageLimit(requestedPages)' in ocr_input_policy)
check('Оба пути PDF передают выбранный лимит в единый OCR процессор', 'Input.pdf(fileBytes, displayName, \"eng\", ocrPdfPageLimit())' in src and 'Input.scannedPdf(pdfBytes, displayName, language, ocrPdfPageLimit())' in src and 'input.maxPages' in ocr_processor)
check('OCR scanned-PDF имеет тайм-аут 45 секунд', 'deadline = System.currentTimeMillis() + 45_000L' in ocr_processor)
check('OCR scanned-PDF ограничивает размер bitmap', '2_000_000L' in ocr_processor and '1_800_000.0' in ocr_processor)
check('OCR scanned-PDF закрывает recognizer и удаляет временный файл', 'recognizer.close()' in ocr_processor and 'tempPdf.delete()' in ocr_processor)
check('Повторный импорт документа блокируется', 'documentImportInProgress' in src and 'Импорт документа уже выполняется' in src)
check('Версия и ограничения scanned-PDF описаны', 'сканированных PDF' in readme and ('первых 5 страниц' in readme or 'первых пяти страниц' in readme) and 'кириллиц' in deps.lower())
check('Автосохранение черновика с debounce присутствует', 'scheduleDraftSave()' in src and 'postDelayed(pendingDraftSave, 350L)' in src and 'persistDraftNow()' in src)
check('Черновик и follow-up сохраняются локально', 'putString(\"draftPrompt\"' in src and 'putString(\"draftFollowUp\"' in src)
check('Последний режим анализа сохраняется в базе данных', 'dataStore.putString(\"draftMode\"' in src and 'dataStore.getInt(\"draftMode\", 0)' in src)
check('Остаточный текст privacy warning удалён из UI', 'Перед отправкой включена локальная приблизительная проверка' not in src)
check('Координатор имеет структуру общего вывода и согласия', '1. ОБЩИЙ ВЫВОД' in council_text and '2. ГДЕ ЭКСПЕРТЫ СОГЛАСНЫ' in council_text)
check('Координатор сравнивает позиции в разногласиях', '3. РАЗНОГЛАСИЯ' in council_text and 'позицию ИИ №1 и ИИ №2' in council_text)
check('Координатор отдельно отмечает допущения и непроверенные факты', '4. ДОПУЩЕНИЯ И ПРОБЕЛЫ' in council_text and '6. ЧТО НЕ ПРОВЕРЕНО' in council_text)
check('Координатор не считает большинство доказательством', 'Не считай совпадение ответов доказательством и не выбирай вывод по большинству голосов' in council_text)
check('Координатор не принимает ошибки API за позицию эксперта', 'не анализируй его как позицию эксперта' in council_text)
check('История поддерживает удаление отдельной записи долгим нажатием', 'entry.setOnLongClickListener' in src and 'Удалить запись истории?' in src)
check('Удаление истории требует подтверждения', '.setNegativeButton("Отмена", null)' in src and '.setPositiveButton("Удалить"' in src)
check('Удаление сохраняет остальные записи в локальной базе и обновляет историю', 'dataStore.putString("history", kept.toString())' in src and 'showHistory();' in src)
check('Удаление записи сопоставляет время, задачу и ответ', 'candidate.optLong("time", 0L) == historyTime' in src and 'candidate.optString("answer", "").equals(answer)' in src)
check('Сторонние лицензии документированы и включены в Android assets', (ROOT/'THIRD_PARTY_NOTICES_RU.md').exists() and (ROOT/'LICENSE-APACHE-2.0.txt').exists() and (ROOT/'app/src/main/assets/licenses/Apache-2.0.txt').exists() and (ROOT/'app/src/main/assets/licenses/THIRD_PARTY_NOTICES_RU.txt').exists())


check('Пресеты локальных моделей Ollama и LM Studio присутствуют', 'Локальная модель · Ollama по Wi-Fi' in src and 'Локальная модель · LM Studio по Wi-Fi' in src and '11434/v1' in src and '1234/v1' in src)
check('Локальный API может работать без ключа', '(!key[i].getText().toString().trim().isEmpty() || isLocalEndpoint(url))' in src and 'LocalAiPolicy.modelsEndpoint(base)' in src)
check('Локальные запросы не отправляют сохранённый Authorization', 'LocalAiPolicy.shouldSendAuthorization(base, key)' in src and 'LocalAiPolicy.shouldSendAuthorization(baseUrl, apiKey)' in PROVIDER_MODELS_CLIENT.read_text(encoding='utf-8') and 'return apiKey != null && !apiKey.trim().isEmpty() && !EndpointPolicy.isLocalEndpoint(baseUrl)' in local_ai_policy)
check('Есть отдельный режим блокировки облачных AI API', 'Только локальные ИИ · блокировать облачные API' in src and 'localOnlyMode' in src)
check('Локальный режим блокирует публичные endpoints перед запуском', 'isLocalEndpoint(endpoint[requiredIndex].getText().toString())' in src and 'Запрос не отправлен' in src)
check('Локальный режим блокирует облачный follow-up и connection test', 'блокирует облачный follow-up' in src and 'блокирует проверку облачного URL' in src)
check('Политика HTTP разрешает только локальные/private адреса', 'isLocalUnauthenticatedEndpoint' in endpoint_policy and 'isPrivateLocalHost(parsed.getHost())' in endpoint_policy and '172' in endpoint_policy and '192' in endpoint_policy)
check('Публичный HTTP и URL с userinfo/query запрещены', 'parsed.getUserInfo() != null' in endpoint_policy and 'parsed.getQuery() != null' in endpoint_policy and 'return isLocalUnauthenticatedEndpoint(trimmed)' in endpoint_policy)
check('Desktop tests проверяют endpoint security boundary', 'EndpointPolicySmokeTest' in runner and 'public HTTP rejected' in (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/EndpointPolicySmokeTest.java').read_text(encoding='utf-8'))
check('Опциональный Brave token настраивается отдельным диалогом', 'showWebSearchSettings()' in src and 'X-Subscription-Token' in search_service)
check('Brave и Tavily keys шифруются перед сохранением и расшифровываются из настроек', 'putString("braveSearchKey", encrypt(' in src and 'putString("tavilySearchKey", encrypt(' in src and 'getStoredSearchKey("tavilySearchKey", "Tavily")' in src)
check('Поиск использует Brave, Wikipedia и DuckDuckGo Instant Answer JSON; HTML scraper удалён', 'searchBrave(query, usableBraveKey, cancellation)' in search_service and 'searchDuckDuckGoHtml' not in search_src and 'html.duckduckgo.com' not in search_src and 'searchWikipedia(query, cancellation)' in search_service and 'searchDuckDuckGoInstant(query, cancellation)' in search_service)
check('Tavily Search добавлен с актуальной Bearer-аутентификацией', 'searchTavily(query, usableTavilyKey, cancellation)' in search_service and 'https://api.tavily.com/search' in search_service and 'tavilySearchKey' in src and 'time_range' in search_service and 'headers.put("Authorization", "Bearer " + apiKey)' in search_service and 'request.put("api_key", apiKey)' not in search_service and '"Authorization"' in (ROOT/'app/src/main/java/com/aicouncil/app/WebSearchEndpointPolicy.java').read_text(encoding='utf-8'))
check('Brave/Wikipedia/DuckDuckGo search API переведены на OkHttp вне Activity', 'webSearchClient.get(address' in search_service and 'private String searchBrave' in search_service and 'private String searchWikipedia' in search_service and 'private String searchDuckDuckGoInstant' in search_service and 'HttpURLConnection' not in search_service and 'webSearchService.search(rawQuery, contextMode, braveKey, tavilyKey' in src)
check('Остановка задачи отменяет поисковые OkHttp-запросы', src.count('webSearchService.cancelAll();') >= 2 and search_service.count('webSearchClient.cancelAll();') >= 2 and 'activeCalls' in (ROOT/'app/src/main/java/com/aicouncil/app/WebSearchClient.java').read_text(encoding='utf-8'))
check('Доступные поисковые источники запрашиваются параллельно вне Activity', 'pool.submit(() -> searchBrave' in search_service and 'pool.submit(() -> searchWikipedia' in search_service and 'pool.submit(() -> searchDuckDuckGoInstant' in search_service and 'searchDuckDuckGoHtml' not in search_src)
check('Поиск ограничивает запрос 180 символами', 'q.length() > 180' in web_policy and 'return WebSearchPolicy.buildSearchQuery(raw)' in search_service)
check('Языковая маршрутизация для веб-поиска включает uk/ru/en', 'languageForQuery' in web_policy and 'return "uk"' in web_policy and 'return "ru"' in web_policy and 'return "en"' in web_policy)
check('Быстрые сценарии используют 6 готовых редактируемых шаблонов', 'addQuickPromptActions(taskCard)' in src and 'String[] templates = {' in src and 'String[] titles = {"Исследовать", "Сравнить", "План действий", "Проверить идею", "Написать текст", "Объяснить проще"}' in src)
check('Сценарии меняют стратегию, но не запускают задачу автоматически', 'modeSpinner.setSelection(modes[index])' in src and 'Сценарий «' in src and 'runCouncil();' not in src[src.find('private void addQuickPromptActions'):src.find('private void showApiPresets')])
check('Быстрые сценарии доступны для горизонтальной прокрутки', 'HorizontalScrollView scroller = new HorizontalScrollView(this)' in src and 'scroller.setHorizontalScrollBarEnabled(false)' in src)
check('Быстрые сценарии имеют ripple и доступные подписи', 'tile.setContentDescription(titles[i] + ". " + descriptions[i]' in src and 'new android.graphics.drawable.RippleDrawable' in src)
check('Кнопки имеют ripple-обратную связь без внешних UI-зависимостей', 'b.setBackground(new android.graphics.drawable.RippleDrawable(' in src and 'com.google.android.material' not in gradle)
check('Нижняя навигация сохраняет выбранный экран', 'LinearLayout[] navItems = new LinearLayout[navTitles.length]' in src and 'putInt("selectedScreen", ix)' in src and 'screens[j].setVisibility(active ? View.VISIBLE : View.GONE)' in src)
check('Поисковый транспорт OkHttp ограничен точными HTTPS API endpoints', (ROOT/'app/src/main/java/com/aicouncil/app/WebSearchClient.java').exists() and 'OkHttpClient' in (ROOT/'app/src/main/java/com/aicouncil/app/WebSearchClient.java').read_text(encoding='utf-8') and 'isAllowedGetEndpoint(address)' in (ROOT/'app/src/main/java/com/aicouncil/app/WebSearchClient.java').read_text(encoding='utf-8') and 'https' in (ROOT/'app/src/main/java/com/aicouncil/app/WebSearchEndpointPolicy.java').read_text(encoding='utf-8'))
check('Ответы API и HTML-страниц ограничены по байтам в OkHttp-клиентах', 'readBounded(ResponseBody body, int maxBytes)' in (ROOT/'app/src/main/java/com/aicouncil/app/WebSearchClient.java').read_text(encoding='utf-8') and 'isResponseLimitAllowed(maxBytes)' in (ROOT/'app/src/main/java/com/aicouncil/app/WebSearchClient.java').read_text(encoding='utf-8') and 'readBounded(ResponseBody body, int limit' in (ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchClient.java').read_text(encoding='utf-8') and 'length > MAX_BYTES' in (ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchClient.java').read_text(encoding='utf-8'))
check('Поиск объединяет canonical HTTPS URL без повторов и ограничивает вывод', 'LinkedHashSet<String> seenUrls' in search_service and 'seen.add(canonical)' in search_service and '18000' in search_service)
check('Поиск ограничен общим deadline, а извлечение страниц имеет отдельный budget', 'searchDeadline = System.currentTimeMillis() + 12000L' in search_service and 'System.currentTimeMillis() + 7000L' in search_service and 'future.get(Math.max(1L, remaining), TimeUnit.MILLISECONDS)' in search_service)
check('Desktop tests проверяют поиск, endpoint allowlist и очистку строки', 'WebSearchPolicySmokeTest' in runner and 'WebSearchEndpointPolicySmokeTest' in runner and 'query capped at 180 chars' in (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/WebSearchPolicySmokeTest.java').read_text(encoding='utf-8') and 'Brave token can be sent to Brave only' in (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/WebSearchEndpointPolicySmokeTest.java').read_text(encoding='utf-8') and 'Tavily bearer key is scoped to its exact endpoint' in (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/WebSearchEndpointPolicySmokeTest.java').read_text(encoding='utf-8'))
check('README раскрывает ограничение: on-device LLM не встроен в APK', 'не встроены веса языковой модели и on-device inference runtime' in readme and 'Ollama/LM Studio' in readme)
check('README предупреждает о HTTP в LAN и внешнем web search', 'HTTP в LAN не шифрует трафик' in readme and 'поисковым сервисам' in readme)

check('Список моделей отображается для каждого слота ИИ', 'Получить список моделей' in src and 'fetchProviderModels(ix)' in src)
check('Каталог моделей поддерживает OpenAI data[] и Ollama models[]', 'json.optJSONArray("data")' in src and 'json.optJSONArray("models")' in src and 'optString("name"' in src)
check('Строгий локальный режим отключает внешний веб-поиск на уровне политики и запуска', 'return webSearchRequested && !localOnly;' in local_ai_policy and 'final boolean doWebSearch = LocalAiPolicy.shouldUseWebSearch(localOnlyRequested, webSearchEnabled.isChecked())' in src and 'blocks web search even if preference requests it' in (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/LocalAiPolicySmokeTest.java').read_text(encoding='utf-8'))
check('LocalAiPolicy изолирована и протестирована desktop runner', 'LocalAiPolicySmokeTest' in runner and 'LocalAiPolicy.java' in runner and 'static boolean shouldUseWebSearch(boolean localOnly, boolean webSearchRequested)' in local_ai_policy)
check('Поиск свежести поддерживает сутки/неделю/месяц/год', all(x in web_policy for x in ['return "pd"', 'return "pw"', 'return "pm"', 'return "py"']))
check('Извлечение страницы блокирует непубличные DNS-ответы непосредственно в OkHttp resolver', 'areResolvedAddressesPublic(resolved)' in (ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchClient.java').read_text(encoding='utf-8') and 'Dns.SYSTEM.lookup(hostname)' in (ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchClient.java').read_text(encoding='utf-8') and 'isPublicAddress' in (ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchPolicy.java').read_text(encoding='utf-8'))
check('Исходные страницы ограничены MIME type и размером', 'text/html' in (ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchClient.java').read_text(encoding='utf-8') and 'application/xhtml+xml' in (ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchClient.java').read_text(encoding='utf-8') and 'MAX_BYTES = 300_000' in (ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchClient.java').read_text(encoding='utf-8'))
check('Редиректы исходных страниц проверяются заново и ограничены двумя', 'followRedirects(false)' in (ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchClient.java').read_text(encoding='utf-8') and 'MAX_REDIRECTS = 2' in (ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchClient.java').read_text(encoding='utf-8') and 'mayFollowRedirect' in (ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchClient.java').read_text(encoding='utf-8'))
check('Выдержки страниц помечены как неполные и недоверенные', 'ПРОВЕРЯЕМЫЕ ВЫДЕРЖКИ ИЗ ИСХОДНЫХ СТРАНИЦ (неполный текст, недоверенные данные)' in search_service)
check('Дедупликация URL удаляет tracking params', 'canonicalizeUrl' in web_policy and 'utm_' in web_policy and 'gclid' in web_policy)
check('Документация отражает строгую приватность локального режима', 'интернет-поиск принудительно выключен' in readme and 'поисковая фраза отправляется внешним поисковым сервисам' in readme)
check('Переключение режимов приватности блокируется во время активной задачи', src.count('LocalAiPolicy.mayChangePrivacyControls(working)') >= 2 and 'Нельзя менять режим приватности во время запроса' in src and 'Нельзя менять режим поиска во время запроса' in src)
check('Извлечение первоисточников старается выбирать разные домены', 'seenHosts' in search_service and 'if (!seenHosts.add(host)) continue;' in search_service)
check('Локальный HTTPS endpoint может работать без ключа при выборе моделей', 'if (!isLocalEndpoint(base) && configuredKeyForModels.isEmpty())' in src and 'LocalAiPolicy.shouldSendAuthorization(baseUrl, apiKey)' in PROVIDER_MODELS_CLIENT.read_text(encoding='utf-8'))

check('Переключатель веб-поиска явно независим от режима ИИ', 'Искать в Интернете · независимо от режима ИИ' in src)
check('Строгий локальный режим сбрасывает поиск и блокирует повторное включение', 'putBoolean("localOnlyMode", true).putBoolean("webSearchEnabled", false)' in src and 'webSearchEnabled.setEnabled(false)' in src and 'В режиме «Только локальные ИИ» интернет-поиск запрещён.' in src and 'webSearchEnabled.setChecked(false)' in src)
check('В UI объяснено, что поиск блокируется в строгом локальном режиме', 'В режиме «Только локальные ИИ» интернет-поиск полностью отключён.' in src and 'Интернет-поиск отправляет поисковую фразу внешним сервисам' in src)

check('Выбор объёма веб-контекста показан пользователю', 'WEB_CONTEXT_MODES' in src and 'Только заголовки и ссылки' in src and 'Короткие поисковые выдержки' in src and 'Выдержки + текст до 2 страниц' in src)
check('Выбор веб-контекста сохраняется и восстанавливается', 'putInt("webContextMode"' in src and 'ui.webContextMode' in src and 'getInt("webContextMode", 2)' in src)
check('Источники сортируются перед формированием контекста', 'rankSearchResults(query, rawResults.toString(), 10000)' in search_service and 'WebSearchPolicy.scoreSearchResult(query, title, snippet.toString(), url, provider)' in search_service)
check('Детальный режим один выполняет загрузку исходных страниц', 'if (contextMode >= 2 && !isCancelled(cancellation))' in search_service and 'fetchTopPageContents(ranked, cancellation)' in search_service)
check('Режим заголовков/ссылок отбрасывает сниппеты', 'if (contextMode == 0) ranked = linksOnlySearchResults(ranked)' in search_service and 'linksOnlySearchResults' in search_service)
check('Дата извлечения отделена от даты метаданных', 'Дата публикации/изменения в метаданных (не подтверждена)' in search_service and 'Дата извлечения:' in search_service)
check('Политика ранжирования охватывает заголовок, сниппет и тип домена', 'static int scoreSearchResult' in web_policy and 'host.endsWith(".gov.ua")' in web_policy and 'hasWholeToken(t, term)' in web_policy and 'hasWholeToken(s, term)' in web_policy)
check('Desktop smoke test проверяет ранжирование поисковой выдачи', 'title relevance scores higher than snippet-only match' in (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/WebSearchPolicySmokeTest.java').read_text(encoding='utf-8'))

check('Пользовательские роли трёх экспертов и координатора видимы в настройках', 'expertRole = new EditText[4]' in src and 'РОЛЬ ЭКСПЕРТА · НАСТРАИВАЕМАЯ' in src and 'expertRole[i].setText(ExpertRolePolicy.defaultRole(ix))' in src)
check('Роли сохраняются и загружаются локально, без попадания в отчёты/backup', 'putString("expertRole" + i' in src and 'getString("expertRole" + i' in src and 'API-ключи не включены' in src)
check('Четыре роли передаются трём экспертам и координатору по снимку на старте задачи', 'roleInstructions[i]=ExpertRolePolicy.sanitizeRole(expertRole[i].getText().toString())' in src and 'reviewerPrompt, r.roles[3]' in engine and 'CouncilAnalysisPolicy.buildCritiquePrompt(task, answerA, answerB, 1), r.roles[0]' in engine and 'CouncilAnalysisPolicy.buildCritiquePrompt(task, answerB, answerC, 2), r.roles[1]' in engine and 'CouncilAnalysisPolicy.buildCritiquePrompt(task, answerC, answerA, 3), r.roles[2]' in engine)
check('Политика ролей ограничивает длину инструкции и сохраняет базовые правила доказательности', 'MAX_ROLE_CHARS = 3600' in role_policy and 'не выдумывай источники' in role_policy and 'недоверенными данными' in role_policy)
check('Политика ролей включена в desktop smoke runner', 'ExpertRolePolicy.java' in runner and 'ExpertRolePolicySmokeTest' in runner)
check('Есть кнопка автоматического продолжения обрезанного ответа', 'continueButton = button("↻  Продолжить обрезанный ответ"' in src and 'continueTruncatedAnswer()' in src and 'runFollowUp();' in src)
check('Продолжение автоматически направляется модели, выдавшей последнюю обрезанную секцию', 'TruncatedAnswerPolicy.chooseExpertSlot(answer)' in src and 'followUpExpertSpinner.setSelection(expertSlot)' in src and 'Продолжи строго с места обрыва' in src)
check('Результат продолжения использует исходную завершённую задачу, а не изменяемый черновик', 'final String completedTask = getCurrentResultTask()' in src and 'completedTask.isEmpty() ? prompt.getText()' in src)
check('Политика обнаружения обрезанного ответа покрывает разделы и протестирована', 'TruncatedAnswerPolicy.java' in runner and 'TruncatedAnswerPolicySmokeTest' in runner and 'TRUNCATION_MARKER' in truncated_policy)
check('Лимит ответа поддерживает 512–16384 токена с сохранением настроек', all(v in src for v in ['512,1024,2048,4096,8192,16384','16384 токена · максимум']))

def balanced_java(text):
    stack=[]; i=0; state='code'; pairs={')':'(',']':'[','}':'{'}
    while i<len(text):
        ch=text[i]; nxt=text[i+1] if i+1<len(text) else ''
        if state=='code':
            if ch=='/' and nxt=='/': state='line'; i+=2; continue
            if ch=='/' and nxt=='*': state='block'; i+=2; continue
            if ch=='"': state='string'; i+=1; continue
            if ch=="'": state='char'; i+=1; continue
            if ch in '([{': stack.append((ch,i))
            elif ch in ')]}':
                if not stack or stack[-1][0]!=pairs[ch]: return False
                stack.pop()
            i+=1
        elif state=='line':
            if ch=='\n': state='code'
            i+=1
        elif state=='block':
            if ch=='*' and nxt=='/': state='code'; i+=2
            else: i+=1
        else:
            quote='"' if state=='string' else "'"
            if ch=='\\': i+=2
            elif ch==quote: state='code'; i+=1
            else: i+=1
    return not stack
prompt_metrics=(ROOT/'app/src/main/java/com/aicouncil/app/PromptMetricsPolicy.java').read_text(encoding='utf-8')
task_routing=(ROOT/'app/src/main/java/com/aicouncil/app/TaskRoutingPolicy.java').read_text(encoding='utf-8')
check('Скобки Java сбалансированы: MainActivity', balanced_java(src))
check('Скобки Java сбалансированы: ChatCompletionsClient', balanced_java(CHAT_CLIENT.read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: ProviderModelsClient', balanced_java(PROVIDER_MODELS_CLIENT.read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: LocalDataStore', balanced_java(LOCAL_DATA_STORE.read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: CouncilResultModel', balanced_java(RESULT_MODEL.read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: DeterministicCalculatorPolicy', balanced_java(CALCULATOR_POLICY.read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: EndpointPolicy', balanced_java(endpoint_policy))
check('Скобки Java сбалансированы: WebSearchPolicy', balanced_java(web_policy))
check('Скобки Java сбалансированы: WebSearchEndpointPolicy', balanced_java((ROOT/'app/src/main/java/com/aicouncil/app/WebSearchEndpointPolicy.java').read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: WebSearchClient', balanced_java((ROOT/'app/src/main/java/com/aicouncil/app/WebSearchClient.java').read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: WebSearchService', balanced_java(search_service))
check('Скобки Java сбалансированы: WebPageFetchClient', balanced_java((ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchClient.java').read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: OcrDocumentProcessor', balanced_java(ocr_processor))
check('Скобки Java сбалансированы: OcrInputPolicy', balanced_java(ocr_input_policy))
check('Скобки Java сбалансированы: WebPageFetchPolicy', balanced_java((ROOT/'app/src/main/java/com/aicouncil/app/WebPageFetchPolicy.java').read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: LocalAiPolicy', balanced_java(local_ai_policy))
check('Скобки Java сбалансированы: DocumentTextExtractor', balanced_java(extractor))
check('Скобки Java сбалансированы: MaterialImportPolicy', balanced_java((ROOT/'app/src/main/java/com/aicouncil/app/MaterialImportPolicy.java').read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: ProviderConfigFingerprint', balanced_java(provider_fingerprint))
check('Скобки Java сбалансированы: ResultReportFormatter', balanced_java((ROOT/'app/src/main/java/com/aicouncil/app/ResultReportFormatter.java').read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: CouncilAnalysisPolicy', balanced_java((ROOT/'app/src/main/java/com/aicouncil/app/CouncilAnalysisPolicy.java').read_text(encoding='utf-8')))
check('Скобки Java сбалансированы: ExpertRolePolicy', balanced_java(role_policy))
check('Скобки Java сбалансированы: TruncatedAnswerPolicy', balanced_java(truncated_policy))
check('Скобки Java сбалансированы: UiThemePolicy', balanced_java(theme_policy))
check('Скобки Java сбалансированы: ProviderErrorFormatter', balanced_java(error_formatter))
check('Скобки Java сбалансированы: SecretRedactor', balanced_java(secret_redactor))
check('Скобки Java сбалансированы: PromptMetricsPolicy', balanced_java(prompt_metrics))
check('Скобки Java сбалансированы: TaskRoutingPolicy', balanced_java(task_routing))
voice_policy=(ROOT/'app/src/main/java/com/aicouncil/app/VoicePromptPolicy.java').read_text(encoding='utf-8')
check('Голосовой ввод использует системный recognizer без дополнительного разрешения', 'RecognizerIntent.ACTION_RECOGNIZE_SPEECH' in src and 'REQUEST_VOICE_INPUT' in src and 'startVoiceInput()' in src and 'RECORD_AUDIO' not in manifest)
check('Голосовой ввод добавляет выбранную альтернативу в черновик, а не отправляет задачу', 'VoicePromptPolicy.append(current, spoken)' in src and 'applyVoiceCandidate(candidates.get(which))' in src and 'Запустите' not in src[src.find('if (requestCode == REQUEST_VOICE_INPUT)'):src.find('if (requestCode == REQUEST_OPEN_TEXT)')])
check('Счётчик длины черновика обновляется при вводе и предупреждает о лимите', 'updatePromptCounter()' in src and '100 000 символов' in src and 'MAX_TASK_CHARS * 9 / 10' in src)
check('Голосовой ввод имеет pure-Java policy tests', 'VoicePromptPolicy.java' in runner and 'VoicePromptPolicySmokeTest' in runner and (ROOT/'tools/desktop_smoke/src/com/aicouncil/app/VoicePromptPolicySmokeTest.java').exists())

check('Приблизительная оценка токенов доступна в офлайн-политике', 'PromptMetricsPolicy.estimateTokens(taskText)' in src and 'PromptMetricsPolicy.sizeLabel(estimatedTokens)' in src and 'токенов' in src)
check('Оценка размера явно называется приблизительной', 'приблизительно' in src.lower() and 'Tokenization' not in src)
check('Счётчик сохраняет предупреждение на 90% лимита', 'MAX_TASK_CHARS * 9 / 10' in src and 'Превышен допустимый размер задачи' in src)
check('Автовыбор вынесен в отдельную тестируемую политику', 'TaskRoutingPolicy.chooseMode(task, configuredProviders, freeOnly != null && freeOnly.isChecked(), markedFree)' in src and 'TaskRoutingPolicy.java' in runner)
check('Автовыбор понимает ключевые команды на русском, украинском и английском', all(word in task_routing for word in ['проверь', 'перевір', 'fact-check', 'architecture']))
check('Автовыбор учитывает реальные возможности и отметку бесплатного провайдера', 'usable(1, configured, freeOnly, markedFree)' in task_routing and 'usable(2, configured, freeOnly, markedFree)' in task_routing)
check('Черновик можно очистить с подтверждением, не затрагивая историю и проекты', 'Очистить черновик?' in src and 'История и проекты сохранены.' in src and 'scheduleDraftSave(); updatePromptCounter();' in src)
check('Системы доступности получают ненавязчивые обновления статуса', 'setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE)' in src)
check('API-ключ можно показать/скрыть без отключения шифрования при сохранении', 'Показать' in src and 'TYPE_TEXT_VARIATION_VISIBLE_PASSWORD' in src and 'TYPE_TEXT_VARIATION_PASSWORD' in src and 'encrypt(k)' in src)
check('Кнопка видимости API-ключа имеет доступное описание и сохраняет позицию курсора', 'revealKey.setContentDescription' in src and 'setSelection(Math.max(0, Math.min(cursor, key[keyIndex].length())))' in src)
check('Оболочка 2.1.0 рисуется от края до края на Android 11+', 'setDecorFitsSystemWindows(false)' in src and 'applyEdgeToEdgeInsets' in src)
check('Главная карточка использует обновлённый нейтральный визуальный стиль', 'С чего начнём?' in src and 'Ваш ИИ-штаб' in src and 'Color.rgb(9, 11, 16)' in src)

privacy_policy = PRIVACY_POLICY.read_text(encoding='utf-8')
check('Проверка приватности перед отправкой подключена к задаче и уточнению', 'confirmSensitivePrompt(originalTask, "задаче"' in src and 'confirmSensitivePrompt(originalQuestion, "уточняющем вопросе"' in src)
check('Пользователь выбирает маскировать, отправить исходный текст или отменить', 'Замаскировать и продолжить' in src and 'Отправить как есть' in src and 'Отмена' in src[src.find('private void confirmSensitivePrompt'):src.find('private void runCouncilRequest')])
check('PromptPrivacyPolicy переиспользует общий SecretRedactor без сетевых вызовов', 'SecretRedactor.redact(value, "")' in privacy_policy and 'HttpURLConnection' not in privacy_policy)
check('Проверка учитывает сохранённые ключи провайдеров Brave и Tavily', 'collectConfiguredSecretsForPrivacyScan()' in src and 'braveSearchKey' in src and 'tavilySearchKey' in src and 'PromptPrivacyPolicy.redact(original, knownSecrets)' in src)
check('Автономный smoke test privacy guard добавлен в runner', 'PromptPrivacyPolicy.java' in runner and 'PromptPrivacyPolicySmokeTest' in runner and PRIVACY_TEST.exists())
check('Версия приложения синхронизирована на 2.22.0', "versionName '2.22.0'" in gradle and 'APP_VERSION = "2.22.0"' in src)
failed=[]
for name,ok in checks:
    print(('PASS' if ok else 'FAIL')+': '+name)
    if not ok: failed.append(name)
print(f'\nИтого: {len(checks)-len(failed)}/{len(checks)} статических проверок пройдено.')
if failed: sys.exit(1)
print('ПРЕДУПРЕЖДЕНИЕ: статические проверки не заменяют Gradle-сборку и тесты на физическом устройстве.')
