# 2.3.4 — First principles, atomic claims, sensitivity analysis и Bayesian-обновление

- В `ExpertRolePolicy` default-роли усилены:
  - Явный first principles и декомпозиция на атомарные утверждения.
  - Sensitivity analysis ключевых допущений (±30%).
  - Bayesian-стиль обновления убеждений.
  - Усиленный multi-hop и adversarial self-check.
- В `CouncilAnalysisPolicy`: критика и координатор требуют sensitivity analysis, атомарных claims и Bayesian-обновления.
- В `TaskRoutingPolicy`: снижены пороги длины (deep analysis с 120 символов, council с 60), добавлены триггеры для sensitivity/first principles/bayesian.
- Версия: versionName 2.3.4, versionCode 68.
- Цель: сделать внутреннее рассуждение ещё более строгим, чувствительным к допущениям и лучше калиброванным без изменения архитектуры.

# 2.3.3 — Self-consistency, Chain-of-Verification, reference class forecasting

- В `ExpertRolePolicy` default-роли усилены:
  - Обязательная self-consistency (2 независимых внутренних пути разными методами с последующим согласованием).
  - Chain-of-Verification (проверочные вопросы к собственным утверждениям).
  - Reference class forecasting для прогнозов и оценок.
  - Expected utility / expected value язык для решений.
- В `CouncilAnalysisPolicy`: усилены требования к верификации и остаточному риску в критике и координаторе.
- В `TaskRoutingPolicy`: снижены пороги длины и усилена агрессивность deep-analysis для reasoning-heavy задач.
- Версия: versionName 2.3.3, versionCode 67.
- Цель: сделать внутреннее рассуждение ещё более устойчивым к галлюцинациям и лучше калиброванным без изменения архитектуры.

# 2.3.2 — Tree-of-Thoughts, Fermi-оценки и усиленная калибровка неопределённости

- В `ExpertRolePolicy` default-роли усилены:
  - Явный Tree-of-Thoughts (несколько ветвей рассуждения внутри процесса).
  - Fermi-оценки для количественных вопросов.
  - Разделение эпистемической и алеаторной неопределённости.
  - Явный учёт сикофантии и других модельных байасов.
- Версия: versionName 2.3.2, versionCode 66.
- Цель: ещё глубже внутреннее рассуждение и более точная калибровка без изменения архитектуры.

(полный чейнджлог сохранён в git history; это версия с новой записью наверху.)
