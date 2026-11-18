package com.aicouncil.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Converts local reasoning-risk hypotheses into short, actionable coordinator
 * instructions. Signals are triage hints, not diagnoses or automatic verdicts.
 *
 * Unknown conflicts are deliberately omitted: a numeric disagreement, by
 * itself, is not evidence of a reasoning fallacy.
 */
public final class CoordinatorCue {
    private static final int MAX_NOTES = 4;
    private static final int MAX_SIGNAL_CHARS = 180;

    private static final Rule[] RULES = {
            rule("disagreement-diagnosis", 0,
                    "disagreement diagnosis",
                    "Это тип разногласия, не вердикт: не усредняй причины. Оставь один несущий механизм и назови наблюдение, которое переключит вывод."),
            rule("hidden-profile", 0,
                    "hidden profile", "скрытый профиль",
                    "Повтор общего текста — не вторая выборка. Ответь на наблюдение только одной стороны или прямо напиши, что его нет."),
            rule("unit-bias", 1,
                    "unit bias", "эффект единицы",
                    "Граница единицы не закрывает выбор: оцени предельную ценность частичной дозы или другого размера."),
            rule("anecdotal-evidence", 1,
                    "anecdotal evidence",
                    "Один рассказ не задаёт частоту: назови базовую частоту, выборку или прямо напиши, что истории недостаточно."),
            rule("survivorship-bias", 5,
                    "survivorship bias", "смещение выжившего", "видимые успехи",
                    "Видимые успехи не вся выборка: назови долю провалов, отсев или прямо укажи, что базовые неудачи неизвестны."),
            rule("seen-and-unseen", 1,
                    "seen and unseen", "видимая выгода", "невидимая цена", "бастиа",
                    "Видимая выгода не весь счёт: назови невидимую цену или прямо укажи, что она неизвестна."),
            rule("streetlight", 2,
                    "streetlight", "эффект фонаря", "макнамар", "удобная метрика",
                    "Удобно посчитать не значит важно: назови ключевой исход или риск, который остался неизмеренным."),
            rule("appeal-to-novelty", 3,
                    "appeal to novelty", "апелляция к новизне",
                    "Новизна — не доказательство качества: сравни с базовым вариантом и назови измеримое преимущество."),
            rule("appeal-to-pity", 4,
                    "appeal to pity", "апелляция к жалости",
                    "Сочувствие важно для ценностей, но не подтверждает фактический тезис: проверь основание отдельно."),
            rule("denominator-neglect", 10,
                    "denominator neglect", "пренебрежение знаменателем", "относительное снижение",
                    "Относительный процент не закрывает выбор без абсолютного риска, базовой частоты или NNT."),
            rule("is-ought", 11,
                    "is-ought", "гильотина юма", "факт не делает норму",
                    "Из «так есть» не следует «так должно»: назови отдельную нормативную посылку."),
            rule("fallacy-fallacy", 12,
                    "fallacy fallacy", "ошибка аргумента как опровержение", "софизм доказывает",
                    "Ошибка довода не доказывает обратное: проверь сам тезис независимо от качества аргумента."),
            rule("gish-gallop", 13,
                    "gish gallop", "поток утверждений", "поток пунктов",
                    "пунктов много — не доказательство: сведи их к нескольким проверяемым тезисам и проверь источники."),
            rule("identifiable-victim", 14,
                    "identifiable victim", "эффект идентифицируемой жертвы", "яркая история важнее",
                    "Яркая история не отменяет частоту: сравни с базовой частотой и учти безликий вред."),
            rule("woozle", 15,
                    "woozle", "цепочка цитат", "эффект узла",
                    "Цепочка цитат не независимая выборка: проследи первоисточник и найди независимые наблюдения."),
            rule("moral-licensing", 16,
                    "moral licensing", "моральное лицензирование", "прошлые заслуги",
                    "Прошлые заслуги не дают лицензии: оцени текущий шаг по его собственным последствиям."),
            rule("escalation-of-commitment", 17,
                    "escalation of commitment", "эскалация обязательств", "sunk cost", "невозвратные затраты",
                    "Прошлые вложения не оправдывают продолжение: сравни будущую ценность, альтернативы и критерии остановки."),
            rule("chesterton-fence", 18,
                    "chesterton fence", "забор честертона", "снос правила",
                    "Не сноси правило или процесс без проверки исходной функции: выясни, зачем он появился и какой риск закрывает."),
            rule("crux", 19,
                    "crux", "переключатель вывода", "ключевой вопрос",
                    "Назови наблюдение, которое переключит вывод; без проверяемого crux разногласие остаётся открытым."),
            rule("reversibility", 20,
                    "reversibility", "обратимость",
                    "Перед необратимым шагом назови дешёвую обратимую проверку и условие, при котором решение будет отменено."),
            rule("catastrophic-risk", 20,
                    "catastrophic risk", "катастрофический риск", "fat tail", "асимметричный downside",
                    "Средняя ожидаемая ценность не закрывает выбор при тяжёлом или необратимом downside: назови величину наихудшего правдоподобного исхода и страховку от разорения."),
            rule("moral-hazard", 20,
                    "moral hazard", "моральный риск", "перенос риска",
                    "Чужая компенсация убытка не закрывает выбор: назови, кто несёт downside и станет ли защищённая сторона осторожнее или рискованнее."),
            rule("adverse-selection", 20,
                    "adverse selection", "неблагоприятный отбор",
                    "Кто сам входит в сделку — не случайная выборка: назови, чем скрытая информация смещает пул до контракта, отдельно от поведения после защиты."),
            rule("winners-curse", 20,
                    "winner's curse", "проклятие победителя",
                    "Победа в аукционе или конкурсе не доказывает справедливую цену: назови, условно на победу, не переоценена ли ставка относительно общей ценности."),
            rule("information-cascade", 20,
                    "information cascade", "информационный каскад",
                    "Массовое следование не равно независимым сигналам: назови, сохраняют ли поздние агенты частную информацию или копируют ранних."),
            rule("free-rider", 20,
                    "free rider", "безбилетник", "трагедия общин",
                    "Общее благо не закрывается добровольным вкладом по умолчанию: назови вклад, исключение или принуждение, иначе устойчивость гипотеза."),
            rule("path-dependence", 20,
                    "path dependence", "зависимость от пути", "lock-in",
                    "Доминирование из-за истории не равно превосходству: назови стоимость переключения и альтернативу, которую путь мог заблокировать."),
            rule("lucas-critique", 20,
                    "lucas critique", "критика лукаса",
                    "Прошлое соотношение не обязано удержать после смены правил: назови, переоптимизируют ли агенты поведение под новый режим."),
            rule("verdict-closure", 4,
                    "verdict closure",
                    "Метка ДЕЛАТЬ или НЕ ДЕЛАТЬ без наблюдения-переключателя и пометки обратимости не закрывает выбор: допиши слот или поставь СНАЧАЛА ПРОВЕРИТЬ."),
            rule("choice-overload", 19,
                    "choice overload", "перегрузка выбором",
                    "Количество вариантов не решает задачу: задай критерии и сравни качество доступных вариантов."),
            rule("duration-neglect", 20,
                    "duration neglect", "пренебрежение длительности",
                    "Не оценивай опыт только по пику или финалу: учти длительность и совокупный эффект."),
            rule("sample-size-neglect", 21,
                    "sample size neglect", "пренебрежение размером выборки",
                    "Проверь размер и способ отбора выборки: малое или смещённое наблюдение не задаёт частоту."),
            rule("base-rate-neglect", 22,
                    "base rate neglect", "игнорирование базовой частоты", "базовая частота",
                    "Сначала задай reference class и базовую частоту; яркий случай сам по себе не меняет априорную оценку."),
            rule("reference-class", 22,
                    "reference class forecasting", "референс-класс", "outside view for forecast",
                    "Прогноз без референс-класса похожих случаев не закрывает выбор: назови базовую частоту или прямо укажи, что используется только внутренний взгляд."),
            rule("collider-bias", 23,
                    "collider bias", "коллайдер", "берксон",
                    "Связь внутри отобранной группы не равна связи в популяции: проверь, не создал ли отбор коллайдер."),
            rule("law-of-small-numbers", 21,
                    "law of small numbers",
                    "Малая выборка не задаёт частоту: назови размер, разброс или больший референс-класс, иначе не закрывай."),
            rule("this-time-is-different", 22,
                    "this time is different",
                    "«В этот раз иначе» не отменяет базовую частоту без механизма, который сильнее внешнего взгляда."),
            rule("complexity-bias", 23,
                    "complexity bias",
                    "Сложность не доказательство: оставь простое объяснение, пока лишний фактор не улучшает предсказание."),
            rule("knightian-uncertainty", 20,
                    "knightian uncertainty",
                    "Неизвестные вероятности — не риск с точными шансами: признай неопределённость и ищи робастный вариант."),
            rule("anticipated-regret", 34,
                    "anticipated regret",
                    "Страх будущего сожаления — цена чувства, не решающий аргумент: сравни ценность альтернатив."),
            rule("simpson-paradox", 23,
                    "simpson paradox", "общий тренд не равен подгруппам",
                    "Общий тренд не переносится на каждую группу: проверь, не разворачивается ли связь внутри."),
            rule("goodhart", 26,
                    "goodhart",
                    "Метрика, ставшая целью, перестаёт быть прокси: назови, как её накрутят без улучшения исхода."),
            rule("ergodicity", 20,
                    "эргодичность", "путь против ансамбля",
                    "Среднее по ансамблю не спасает один путь: назови долю банка и сценарий разорения."),
            rule("causation", 23,
                    "causal inference", "causal oversimplification", "post hoc", "причина против корреляции",
                    "Корреляция не устанавливает причину: назови механизм, смешивающие факторы и проверку причинности."),
            rule("confirmation-bias", 24,
                    "confirmation bias", "подтверждающее смещение",
                    "Ищи не только подтверждения: сформулируй сильнейшее опровержение и наблюдение, которое изменит вывод."),
            rule("cherry-picking", 25,
                    "cherry picking", "выборочное представление",
                    "Покажи полный знаменатель, критерий отбора и неудобные результаты, а не только поддерживающие примеры."),
            rule("external-validity", 26,
                    "external validity", "внешняя валидность",
                    "Чужой результат не переносится автоматически: объясни переносимость популяции, условий и периода."),
            rule("publication-bias", 27,
                    "publication bias", "смещение публикаций",
                    "Учитывай неопубликованные неудачи и нулевые результаты; опубликованные успехи не вся выборка."),
            rule("groupthink", 28,
                    "groupthink", "групповое мышление",
                    "Согласие моделей не равно независимому подтверждению: запроси сильнейшее альтернативное объяснение."),
            rule("ad-hominem", 29,
                    "ad hominem", "переход на личность",
                    "Оцени тезис и доказательства, а не личность автора; критика источника требует конкретного основания."),
            rule("false-dilemma", 30,
                    "false dilemma", "ложная дилемма",
                    "Проверь третий вариант, отсрочку и статус-кво; два названных варианта не обязательно исчерпывают выбор."),
            rule("false-equivalence", 31,
                    "false equivalence", "ложная эквивалентность",
                    "Не приравнивай позиции без проверки масштаба, качества доказательств и существенных различий."),
            rule("middle-ground", 32,
                    "middle ground", "золотая середина", "середина двух позиций",
                    "Середина двух позиций не становится истинной автоматически: сравни основания каждого варианта."),
            rule("opportunity-cost", 33,
                    "opportunity cost", "альтернативная стоимость",
                    "Назови лучший отвергнутый вариант и цену отказа от него."),
            rule("missing-counterfactual", 33,
                    "missing counterfactual", "отсутствие контрфактуала",
                    "Назови, что произошло бы под следующим лучшим вариантом, и прирост относительно него; без контрфактуала преимущество не закрыто."),
            rule("premortem", 34,
                    "premortem", "премортем", "kill criterion",
                    "Опиши правдоподобный сценарий провала, ранний индикатор и заранее заданный критерий остановки."),
            rule("planning-fallacy", 35,
                    "planning fallacy", "ошибка планирования",
                    "Сверь срок и бюджет с reference class завершённых проектов, а не только с внутренним планом."),
            rule("overconfidence", 36,
                    "overconfidence", "чрезмерная уверенность",
                    "Калибруй уверенность по проверяемым данным и прошлым прогнозам; гладкость ответа не повышает точность."),
            rule("probability-neglect", 37,
                    "probability neglect", "игнорирование вероятности",
                    "Тяжесть исхода не заменяет его вероятность: оцени риск и ожидаемый ущерб вместе."),
            rule("appeal-to-emotion", 38,
                    "appeal to emotion", "апелляция к эмоциям", "appeal to fear",
                    "Эмоция может обозначать важную ценность, но не доказывает факт: отдели ценность от вероятности и данных."),
            rule("red-herring", 80,
                    "red herring", "увод в сторону",
                    "Увод в сторону не отвечает на исходный вопрос: вернись к проверяемому тезису."),
            rule("tu-quoque", 90,
                    "tu quoque", "ты тоже", "tu quoque",
                    "Чужое лицемерие не опровергает тезис: проверь само утверждение и его доказательства.")
    };

    private CoordinatorCue() {}

    private static Rule rule(String key, int priority, String... values) {
        if (values == null || values.length < 2) {
            throw new IllegalArgumentException("A coordinator rule needs an alias and an instruction.");
        }
        String instruction = values[values.length - 1];
        String[] aliases = new String[values.length - 1];
        System.arraycopy(values, 0, aliases, 0, aliases.length);
        return new Rule(key, priority, aliases, instruction);
    }

    public static void append(StringBuilder sb, List<String> conflicts) {
        if (sb == null || conflicts == null || conflicts.isEmpty()) return;
        List<String> notes = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        List<String> sorted = new ArrayList<>(conflicts);
        Collections.sort(sorted, Comparator.comparingInt(CoordinatorCue::priorityOf));
        for (String raw : sorted) {
            if (raw == null) continue;
            String signal = raw.trim();
            if (signal.isEmpty()) continue;
            Rule hit = match(signal);
            if (hit == null) continue;
            if (!seen.add(hit.key)) continue;
            String note = hit.instruction;
            if (note.length() > MAX_SIGNAL_CHARS) note = note.substring(0, MAX_SIGNAL_CHARS) + "…";
            notes.add(note);
            if (notes.size() >= MAX_NOTES) break;
        }
        if (notes.isEmpty()) return;
        sb.append("\n\nЛокальные подсказки координатору (не вердикты):\n");
        for (String note : notes) {
            sb.append("- ").append(note).append("\n");
        }
    }


    private static int priorityOf(String signal) {
        Rule hit = match(signal);
        return hit == null ? Integer.MAX_VALUE : hit.priority;
    }

    private static Rule match(String signal) {
        String lower = signal.toLowerCase(Locale.ROOT);
        for (Rule rule : RULES) {
            for (String alias : rule.aliases) {
                if (lower.contains(alias)) return rule;
            }
        }
        return null;
    }

    private static final class Rule {
        final String key;
        final int priority;
        final String[] aliases;
        final String instruction;

        Rule(String key, int priority, String[] aliases, String instruction) {
            this.key = key;
            this.priority = priority;
            this.aliases = aliases;
            this.instruction = instruction;
        }
    }
}
