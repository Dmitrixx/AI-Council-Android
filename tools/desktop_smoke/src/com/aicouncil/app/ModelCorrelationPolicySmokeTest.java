package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ModelCorrelationPolicySmokeTest {
    private static int passed;

    private static final String SHARED =
            "Я уверен, что локальный запуск очереди на одном узле однозначно лучше облачного брокера: "
                    + "транзакции сохраняют порядок сообщений, резервная копия восстанавливает состояние после сбоя, "
                    + "наблюдаемость покрывает задержку и глубину очереди, а откат схемы остаётся обратимым для оператора. "
                    + "Уверенность 90 процентов при текущих допущениях нагрузки и без отдельного внешнего источника.";

    public static void main(String[] args) {
        signal("near-copy confident answers are not two samples",
                SHARED,
                SHARED.replace("облачного брокера", "облачного брокера в проде")
                        .replace("оператора", "дежурного оператора"));
        check(ModelCorrelationPolicy.overlap(SHARED, SHARED) >= ModelCorrelationPolicy.OVERLAP_AT,
                "identical long answers clear the overlap threshold");

        noSignal("the same claim without confidence is only an echo",
                SHARED.replace("Я уверен, что", "Возможно, ")
                        .replace("однозначно ", "")
                        .replace("Уверенность 90", "Оценка без числа"),
                SHARED.replace("Я уверен, что", "Возможно, ")
                        .replace("однозначно ", "")
                        .replace("Уверенность 90", "Оценка без числа"));
        noSignal("dissent blocks the correlation signal",
                SHARED,
                "Не согласен с этой позицией. " + SHARED);
        noSignal("a short confident duplicate is too thin to score",
                "Я уверен, однозначно запускаем.",
                "Я уверен, однозначно запускаем.");
        noSignal("ordinary divergent answers stay quiet",
                "Рекомендую PostgreSQL: транзакции и запас по росту. Следующий шаг — замерить запись.",
                "Рекомендую SQLite: проще на одном узле, но упрёмся в один файл.");

        signal("commitment plus an unnamed data gap",
                "Рекомендую запускать сейчас, хотя неизвестно, выдержит ли спрос: данных нет и неясно, кто уйдёт.",
                "Нейтральный комментарий без решения.");
        noSignal("a named probe closes the information gap",
                "Рекомендую запускать, хотя неизвестно про спрос: сначала проверить пилот и измерить отток.",
                "");
        noSignal("a negated recommendation is not a commitment",
                "Не рекомендую запускать: неизвестно, выдержит ли спрос, данных нет.",
                "Согласен подождать.");
        noSignal("uncertainty alone is not a decision",
                "Неизвестно, выдержит ли спрос, данных нет.",
                "Неясно, кто уйдёт.");

        List<String> idempotent = new ArrayList<String>();
        ModelCorrelationPolicy.detect(SHARED, SHARED, idempotent);
        int once = idempotent.size();
        ModelCorrelationPolicy.detect(SHARED, SHARED, idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 3, "signal count stays bounded");
        check(idempotent.get(0).contains("возможный сигнал"), "signal is marked as a hypothesis");

        System.out.println("ModelCorrelationPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
        if (!out.isEmpty()) {
            check(out.get(0).contains("возможный сигнал"), label + " is marked as a hypothesis");
        }
    }

    private static void noSignal(String label, String a, String b) {
        check(run(a, b).isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        ModelCorrelationPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
