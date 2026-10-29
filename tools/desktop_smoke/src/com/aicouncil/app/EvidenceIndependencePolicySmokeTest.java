package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class EvidenceIndependencePolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        List<String> chorus = new ArrayList<>();
        EvidenceIndependencePolicy.detect(
                "Согласно https://example.com/study-42 эффект доказан, значит верно.",
                "Та же ссылка https://example.com/study-42 это подтверждает.",
                chorus);
        check(!chorus.isEmpty(), "shared url treated as proof");
        check(chorus.get(0).startsWith("карта решения"), "shared url stays on the decision map");

        noSignal("two urls are not one chorus",
                "Есть https://example.com/a и это ещё не доказано.",
                "Другое исследование https://example.com/b независимо проверяет эффект.");
        noSignal("same url but explicitly not settled",
                "Ссылка https://example.com/study-42 пока только гипотеза.",
                "По https://example.com/study-42 рано делать вывод.");

        signal("regression", "Рекордный квартал так и останется новой нормой.");
        noSignal("regression named", "Рекордный квартал, но это вернётся к среднему, а не новой нормой.", "");

        signal("goodhart", "Нужно максимизировать KPI любой ценой: число и есть успех.");
        noSignal("goodhart caveat", "Максимизируем показатель, но метрику можно накрутить, поэтому это не успех.", "");

        signal("premortem", "Рекомендую сделать это необратимо и сразу на всех пользователей навсегда.");
        noSignal("premortem present",
                "Рекомендую необратимый запуск, но премортем: остановимся, если провалится удержание.",
                "");

        List<String> many = new ArrayList<>();
        String packed = "Рекордный квартал так и останется новой нормой. "
                + "Нужно максимизировать KPI любой ценой: число и есть успех. "
                + "Рекомендую сделать это необратимо и сразу на всех пользователей навсегда.";
        EvidenceIndependencePolicy.detect(
                packed + " Согласно https://example.com/study-42 эффект доказан.",
                "https://example.com/study-42 это подтверждает.",
                many);
        check(many.size() == 4, "four distinct independence signals");
        check(many.stream().allMatch(x -> x.contains("возможный сигнал") && x.contains("контекста")),
                "signals stay hypotheses");
        int size = many.size();
        EvidenceIndependencePolicy.detect(
                packed + " Согласно https://example.com/study-42 эффект доказан.",
                "https://example.com/study-42 это подтверждает.",
                many);
        check(many.size() == size, "repeat scan does not duplicate");

        System.out.println("EvidenceIndependencePolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String answer) {
        List<String> out = new ArrayList<>();
        EvidenceIndependencePolicy.detect(answer, "", out);
        check(!out.isEmpty(), label + " detected");
        check(out.get(0).startsWith("карта решения"), label + " is a map signal");
    }

    private static void noSignal(String label, String a, String b) {
        List<String> out = new ArrayList<>();
        EvidenceIndependencePolicy.detect(a, b, out);
        check(out.isEmpty(), label);
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            System.err.println("FAIL " + label);
            System.exit(1);
        }
        passed++;
    }
}
