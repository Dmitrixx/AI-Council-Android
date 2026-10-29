package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Offline decision-quality signals (v2.44+).
 * Flags opportunity-cost neglect, status-quo bias, consensus without independent
 * evidence, single-metric fixation and false precision. Each note is a hypothesis
 * for the coordinator, not a verdict.
 */
public final class DecisionQualityPolicy {
    private static final int MAX_SIGNALS = 6;

    private DecisionQualityPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, opportunityCost(a) || opportunityCost(b),
                "упущенная выгода: выбран один путь без явного сравнения с отвергнутыми альтернативами");
        add(conflicts, initial, statusQuo(a) || statusQuo(b),
                "status quo: сохранение текущего порядка рекомендуется без сравнения издержек перемен и бездействия");
        add(conflicts, initial, consensusWithoutEvidence(a, b),
                "консенсус без опоры: ответы совпадают по выводу, но оба слабы по источникам/данным");
        add(conflicts, initial, metricFixation(a) || metricFixation(b),
                "фиксация на одной метрике: один показатель объявлен достаточным критерием успеха");
        add(conflicts, initial, falsePrecision(a) || falsePrecision(b),
                "ложная точность: точечная цифра без диапазона, погрешности или reference class");
        add(conflicts, initial, actionBias(a) || actionBias(b),
                "bias к действию: «надо что-то сделать» без критерия, когда бездействие было бы лучше");
    }

    private static void add(List<String> out, int initial, boolean condition, String message) {
        String formatted = "качество решения — возможный сигнал, требуется проверка контекста: " + message;
        if (!condition || out.size() - initial >= MAX_SIGNALS || out.contains(formatted)) return;
        out.add(formatted);
    }

    /** Recommends a path while never naming what is given up. */
    private static boolean opportunityCost(String t) {
        boolean chooses = has(t, "рекомендую выбрать", "следует выбрать", "нужно выбрать",
                "we should choose", "recommend choosing", "оптимальный вариант");
        boolean costNamed = has(t, "упущен", "opportunity cost", "вместо этого потеряем",
                "отказываемся от", "цена отказа", "what we forgo", "trade-off", "компромисс между");
        boolean alternatives = has(t, "альтернатив", "другие варианты", "compared to",
                "по сравнению с", "versus", "vs ");
        return chooses && !costNamed && !alternatives;
    }

    /** Keep current setup because it is current, not because it is better. */
    private static boolean statusQuo(String t) {
        return has(t, "оставить как есть", "не менять ничего", "keep the status quo",
                "leave it as is", "лучше не трогать", "не стоит менять")
                && has(t, "потому что так сейчас", "потому что привычно", "because it already works",
                "потому что всегда так", "we already do this")
                && !has(t, "сравнили издержки", "cost of change", "цена бездействия",
                "cost of inaction", "пересчитали варианты");
    }

    /** Both answers assert a firm conclusion with thin evidentiary language. */
    private static boolean consensusWithoutEvidence(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        boolean firmA = firmConclusion(a);
        boolean firmB = firmConclusion(b);
        if (!(firmA && firmB)) return false;
        boolean weakA = weakEvidence(a);
        boolean weakB = weakEvidence(b);
        return weakA && weakB;
    }

    private static boolean firmConclusion(String t) {
        return has(t, "однозначно", "безусловно верно", "definitely", "certainly true",
                "нет сомнений", "очевидно что", "it is clear that", "доказано что");
    }

    private static boolean weakEvidence(String t) {
        boolean hasSource = has(t, "http", "источник", "source", "исследован", "study",
                "meta-analysis", "rct", "данные за", "n=", "выборка");
        boolean hasNumber = t.matches(".*\\d+.*");
        return !hasSource && !hasNumber;
    }

    /** One KPI is treated as the whole success definition. */
    private static boolean metricFixation(String t) {
        return has(t, "единственный критерий", "только эта метрика", "the only metric that matters",
                "успех = ", "success equals", "достаточно смотреть на")
                && has(t, "kpi", "метрик", "показател", "metric", "ctr", "roi")
                && !has(t, "набор метрик", "несколько показателей", "dashboard",
                "баланс метрик", "multi-metric");
    }

    /** Exact point estimate presented as known truth. */
    private static boolean falsePrecision(String t) {
        boolean exact = has(t, "точно составит", "ровно будет", "will be exactly",
                "precisely", "с точностью до", "точно равен");
        boolean noRange = !has(t, "диапазон", "±", "плюс-минус", "confidence interval",
                "погрешност", "от и до", "between", "примерно", "около", "approximately");
        boolean hasDigit = t.matches(".*\\d+.*");
        return exact && noRange && hasDigit;
    }

    /** Pressure to act without a decision rule vs waiting. */
    private static boolean actionBias(String t) {
        return has(t, "нужно что-то сделать", "нельзя просто ждать", "must do something",
                "inaction is unacceptable", "бездействие недопустимо")
                && has(t, "поэтому действуем", "therefore act", "запускаем сейчас")
                && !has(t, "критерий остановки", "value of information", "цена ошибки действия",
                "when waiting is better", "когда ждать лучше");
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
