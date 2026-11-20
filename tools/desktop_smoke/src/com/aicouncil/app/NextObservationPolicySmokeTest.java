package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class NextObservationPolicySmokeTest {
    private static int checks;
    private static final String TASK =
            "Стоит ли запускать новый тариф в следующем квартале? Ставка 2 млн руб. "
                    + "Успех — удержание выше 40%. Кто несет убыток — компания. "
                    + "Похожих случаев 30 из 100. Альтернативная стоимость — отказ от найма. "
                    + "Премортем: если отток выше 4%, стоп.";

    public static void main(String[] args) {
        String block = NextObservationPolicy.block(TASK);
        check(block.contains("СЛЕДУЮЩЕЕ НАБЛЮДЕНИЕ"), "block title");
        check(block.contains("лишнее действие"), "error-cost is the highest-leverage ask");
        check(block.contains("не список всех дыр"), "one observation, not a dump");
        check(NextObservationPolicy.block("коротко").isEmpty(), "tiny tasks stay quiet");
        check(NextObservationPolicy.leverage("downside", "разорит весь капитал") > NextObservationPolicy.leverage("downside", "обычный выбор"),
                "ruin raises downside leverage");

        flags("Поэтому делаем и закрываем выбор.", "Заметка без рекомендации.");
        both("Therefore we should proceed. The choice is clear.", "Поэтому делаем. Рекомендую делать запуск.");
        quiet("Какая ошибка дороже — неизвестно, это гипотеза.", "Пока рано решать.");
        if (checks < 8) fail("expected at least 8 checks, got " + checks);
        System.out.println("NextObservationPolicySmokeTest OK " + checks);
    }

    private static void flags(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        NextObservationPolicy.detect(TASK, a, b, conflicts);
        check(!conflicts.isEmpty(), "expected a next-observation signal");
        check(conflicts.get(0).contains("possible signal"), "signal stays a hypothesis");
    }

    private static void both(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        NextObservationPolicy.detect(TASK, a, b, conflicts);
        check(conflicts.size() >= 2, "both experts should add a second signal, got " + conflicts.size());
    }

    private static void quiet(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        NextObservationPolicy.detect(TASK, a, b, conflicts);
        check(conflicts.isEmpty(), "naming the gap is not a signal, got " + conflicts);
    }

    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) fail(message);
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }
}
