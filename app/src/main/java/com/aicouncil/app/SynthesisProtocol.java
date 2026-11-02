package com.aicouncil.app;

import java.util.Locale;

/**
 * Local method for how the council should think about a task.
 * A checklist and a decision rule, not a fact, a source, or a verdict.
 */
public final class SynthesisProtocol {
    private SynthesisProtocol() {}

    public enum Kind { FORECAST, DECISION, FACT, DESIGN, EXPLANATION, GENERAL }

    /** Empty for very short tasks so unrelated prompts stay untouched. */
    public static String expertBlock(String task) {
        if (task == null || task.trim().length() < 40) return "";
        Kind kind = classify(task);
        StringBuilder sb = new StringBuilder();
        sb.append("ПРОТОКОЛ СИНТЕЗА (метод приложения, не источник фактов):\n");
        sb.append("Режим разбора: ").append(label(kind)).append(".\n");
        switch (kind) {
            case FORECAST:
                sb.append("Сначала outside view: назови класс похожих случаев и базовую частоту либо прямо напиши, что её нет.\n");
                sb.append("Дай вероятность диапазоном, не одной точкой. Успех и провал должны дополнять друг друга.\n");
                sb.append("Назови одно наблюдение, которое сдвинуло бы оценку хотя бы на 20 пунктов.\n");
                break;
            case DECISION:
                sb.append("Перечисли варианты, включая «оставить как есть» и «сначала собрать информацию».\n");
                sb.append("Отдели обратимый шаг от необратимого. Одной строкой скажи, от чего отказываются.\n");
                sb.append("Назови один правдоподобный провал. Уже потраченное не является причиной продолжать.\n");
                sb.append("Итог — одно из: делать, не делать, или сначала проверить конкретный факт.\n");
                break;
            case FACT:
                sb.append("Раздели: известно из задачи, выведено, неизвестно. Не выдумывай цитаты и URL.\n");
                sb.append("Если стороны спорят, сохрани обе и напиши, какое наблюдение их различит.\n");
                break;
            case DESIGN:
                sb.append("Назови ограничения и самый дешёвый прототип, который проверяет самое рискованное допущение.\n");
                sb.append("Опиши один режим отказа и как его заметить до полного запуска.\n");
                break;
            case EXPLANATION:
                sb.append("Дай минимум два механизма. Скажи, какое наблюдение их различит.\n");
                sb.append("Не объявляй корреляцию причиной без отдельного основания.\n");
                break;
            default:
                sb.append("Отдели факты, выводы и допущения. Не поднимай уверенность только из-за гладкой формулировки.\n");
                sb.append("Назови, что изменило бы вывод, и один дешёвый следующий шаг проверки.\n");
                break;
        }
        if (ruinShaped(task)) {
            sb.append("Неэргодичный риск: положительное среднее по параллельным исходам не равно пути одного капитала. ");
            sb.append("Если один проигрыш обнуляет ставку, назови долю банка или прямо напиши, что предел позиции не задан. ");
            sb.append("All-in не следует из положительного среднего.\n");
        }
        sb.append("Несовместимые тезисы не усредняй: выбери сторону, оставь развилку открытой или предложи тест.");
        return sb.toString();
    }

    /**
     * Coordinator addendum. Starts with a blank line so it can be concatenated
     * onto the opening prompt. Empty for very short tasks.
     */
    public static String coordinatorBlock(String task, String localMap) {
        if (task == null || task.trim().length() < 40) return "";
        Kind kind = classify(task);
        boolean mapped = localMap != null && localMap.trim().length() > 0;
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nПРОТОКОЛ КООРДИНАТОРА (посчитан приложением, не моделью):\n");
        sb.append("Режим: ").append(label(kind)).append(".\n");
        if (!mapped) {
            sb.append("Локальная карта пуста. Похожие формулировки не повышают уверенность: проверь, есть ли независимые числа, источники или reference class.\n");
        } else {
            sb.append("По карте есть расхождения. Не сглаживай их серединой. Для верхнего конфликта: выбери сторону, оставь открытым или назови тест, который его закроет.\n");
        }
        if (kind == Kind.DECISION) {
            sb.append("Заверши рекомендацию ровно одной меткой: ДЕЛАТЬ, НЕ ДЕЛАТЬ или СНАЧАЛА ПРОВЕРИТЬ — и одним предложением почему.\n");
        } else if (kind == Kind.FORECAST) {
            sb.append("Итоговая вероятность — диапазон. Точечные шансы экспертов не заменяй средним, пока не сверены класс случаев, выигрыш и потеря.\n");
        } else {
            sb.append("В конце: что изменило бы вывод, и самый дешёвый следующий шаг проверки.\n");
        }
        if (ruinShaped(task)) {
            sb.append("Не заменяй путь капитала средним ожиданием и не усредняй экспертов в рекомендацию all-in.\n");
        }
        sb.append("Меньшинство с более сильной количественной опорой важнее большинства с общей формулировкой.");
        return sb.toString();
    }

    public static Kind classify(String task) {
        String text = task == null ? "" : task.toLowerCase(Locale.ROOT);
        int forecast = score(text, "прогноз", "forecast", "вероятност", "шанс", "probability",
                "likelihood", "через год", "к какому году", "оцени шанс");
        int decision = score(text, "стоит ли", "чи варто", "should we", "should i", "выбрать",
                "выбор между", "запускать", "запускати", "решени", "decide", "инвестир", "інвест",
                "купить или", "отказаться", "go/no-go");
        int fact = score(text, "правда ли", "верно ли", "fact-check", "проверь факт", "перевір",
                "это правда", "is it true", "опровергни");
        int design = score(text, "спроектир", "архитектур", "как сделать", "how to build",
                "реализуй", "напиши код", "дизайн систем");
        int explanation = score(text, "почему", "чому", "объясни", "поясни", "why ", "why?",
                "как работает");
        int best = 0;
        Kind kind = Kind.GENERAL;
        if (forecast > best) { best = forecast; kind = Kind.FORECAST; }
        if (decision >= best && decision > 0) { best = decision; kind = Kind.DECISION; }
        if (fact > best) { best = fact; kind = Kind.FACT; }
        if (design > best) { best = design; kind = Kind.DESIGN; }
        if (explanation > best) { best = explanation; kind = Kind.EXPLANATION; }
        return kind;
    }

    private static int score(String text, String... cues) {
        int hits = 0;
        for (String cue : cues) {
            if (text.contains(cue)) hits++;
        }
        return hits;
    }

    /** Levered, all-in or ruin-shaped questions. Ordinary profit "margin" does not qualify. */
    private static boolean ruinShaped(String task) {
        String text = task == null ? "" : task.toLowerCase(Locale.ROOT);
        return text.contains("кредитное плечо") || text.contains("кредитным плечом")
                || text.contains("с плечом") || text.contains("leverage")
                || text.contains("all-in") || text.contains("весь капитал")
                || text.contains("все деньги") || text.contains("всё или ничего")
                || text.contains("все или ничего") || text.contains("обнулить капитал")
                || text.contains("разорит") || text.contains("маржин-колл")
                || text.contains("margin call") || text.contains("кредитне плече")
                || text.contains("увесь капітал");
    }

    private static String label(Kind kind) {
        switch (kind) {
            case FORECAST: return "прогноз";
            case DECISION: return "решение";
            case FACT: return "проверка факта";
            case DESIGN: return "проектирование";
            case EXPLANATION: return "объяснение";
            default: return "общий разбор";
        }
    }
}
