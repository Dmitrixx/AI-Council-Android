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
            rule("choice-overload", 19,
                    "choice overload", "перегрузка выбором",
                    "Количество вариантов не решает задачу: задай критерии и сравни качество доступных вариантов."),
            rule("duration-neglect", 20,
                    "duration neglect", "пренебрежение длительностью",
                    "Не оценивай опыт только по пику или финалу: учти длительность и совокупный эффект."),
            rule("sample-size-neglect", 21,
                    "sample size neglect", "пренебрежение размером выборки",
                    "Проверь размер и способ отбора выборки: малое или смещённое наблюдение не задаёт частоту."),
            rule("base-rate-neglect", 22,
                    "base rate neglect", "игнорирование базовой частоты", "базовая частота",
                    "Сначала задай reference class и базовую частоту; яркий случай сам по себе не меняет априорную оценку."),
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
        String[] aliases = new String[values.length - 1];
        System.arraycopy(values, 0, aliases, 0, aliases.length);
        return new Rule(key, priority, aliases, values[values.length - 1]);
    }

    public static void append(StringBuilder sb, List<String> conflicts) {
        if (sb == null || conflicts == null || conflicts.isEmpty()) return;
        List<Note> notes = collect(conflicts);
        if (notes.isEmpty()) return;

        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            if (i == 0) {
                sb.append("\nЛокальная гипотеза (требует контекста, не автоматический вердикт): ");
            } else {
                sb.append("\n- ");
            }
            sb.append(note.signal).append(" — ").append(note.rule.instruction);
        }
    }

    /** Number of distinct, recognized hypotheses that will actually be shown. */
    public static int noteCount(List<String> conflicts) {
        return conflicts == null ? 0 : collect(conflicts).size();
    }

    private static List<Note> collect(List<String> conflicts) {
        List<Note> notes = new ArrayList<Note>();
        Set<String> seen = new HashSet<String>();
        for (String raw : conflicts) {
            if (raw == null || raw.trim().isEmpty()) continue;
            Rule matched = match(raw.toLowerCase(Locale.ROOT));
            if (matched == null || !seen.add(matched.key)) continue;
            notes.add(new Note(matched, compact(raw)));
        }
        Collections.sort(notes, new Comparator<Note>() {
            @Override public int compare(Note left, Note right) {
                return left.rule.priority - right.rule.priority;
            }
        });
        if (notes.size() > MAX_NOTES) {
            return new ArrayList<Note>(notes.subList(0, MAX_NOTES));
        }
        return notes;
    }

    private static Rule match(String text) {
        for (Rule candidate : RULES) {
            for (String alias : candidate.aliases) {
                if (text.contains(alias)) return candidate;
            }
        }
        return null;
    }

    private static String compact(String value) {
        String result = value.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').trim();
        while (result.contains("  ")) result = result.replace("  ", " ");
        if (result.length() > MAX_SIGNAL_CHARS) {
            result = result.substring(0, MAX_SIGNAL_CHARS - 1).trim() + "…";
        }
        return result;
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

    private static final class Note {
        final Rule rule;
        final String signal;

        Note(Rule rule, String signal) {
            this.rule = rule;
            this.signal = signal;
        }
    }
}
