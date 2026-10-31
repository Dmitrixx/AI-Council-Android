# 2.59.0 — Сигналы ожидаемой полезности (Expected Utility)

- Добавлен `ExpectedUtilityPolicy`: консервативные локальные детекторы для решений с вероятностями и исходами.
  - Пропущенный расчёт EV / взвешенного итога при наличии вероятностей и payoffs.
  - Асимметрия: один эксперт фокусируется на upside, другой только на downside без сравнения.
  - Качественный риск («высокий/низкий») без диапазона или вероятности.
  - Иллюзия определённости при одновременном наличии неопределённостей.
- Все сигналы помечены как «ожидаемая полезность (возможный сигнал, требует контекста)». Ограничение MAX_SIGNALS = 6, идемпотентность.
- Модуль подключён в `DeliberationSignals.detectAll`. Координатор не должен усреднять эти сигналы.
- Gradle / APP_VERSION остаются 2.36.0 / versionCode 103 согласно CI verify.

# 2.58.0 — Полное восстановление ReasoningFallacyPolicy и DecisionBriefPolicy

- `ReasoningFallacyPolicy` восстановлен после частичного wipe: полный набор консервативных multi-marker детекторов (RU/UK/EN) — апелляция к незнанию, бандвагон, post hoc, special pleading, скользкий склон, поспешное обобщение, соломенное чучело, ad hominem, круговая аргументация, авторитет, традиция, tu quoque, ложная дихотомия, red herring, эмоции, ложная эквивалентность, no true scotsman, природа, генетическая ошибка, сдвиг бремени, moving goalposts, ложный компромисс, якорение, ошибка игрока, texas sharpshooter, relative privation, nirvana, континуум + расходящиеся рекомендации.
- Все сигналы помечены как «возможный сигнал логического риска (требует контекста, не вердикт)». Ограничение MAX_SIGNALS = 8, идемпотентность.
- `DecisionBriefPolicy` восстановлен из PLACEHOLDER2: sunk cost, survivorship, novelty, consequences, cherry-pick, base-rate neglect, nirvana, motivated reasoning, status-quo, zero-risk, scope neglect, anchoring, planning fallacy, availability, confirmation, opportunity cost, affirming the consequent, denying the antecedent, outcome bias, present bias, omission bias.
- Сигналы карты решения помечены как «карта решения (возможный сигнал, требует контекста)». Ограничение и идемпотентность.
- Оба модуля подключены в `DeliberationSignals.detectAll` и покрыты существующими smoke-тестами.
- Gradle / APP_VERSION остаются 2.36.0 / versionCode 103 согласно CI verify.
