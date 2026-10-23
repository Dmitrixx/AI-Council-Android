package com.aicouncil.app;

public final class CouncilResultModelSmokeTest {
    private static int checks;
    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
    }

    public static void main(String[] args) {
        String report = "✦ ИИ №1 · Model A\n\nСовпадает с данными, но это предположение.\n\n────────────────────\n\n"
                + "✦ ИИ №2 · Model B · ОШИБКА\n\nОшибка API HTTP 401\n\n────────────────────\n\n"
                + "🏁 ИТОГ КООРДИНАТОРА · Model C\n\nНеизвестно: источник требует проверки.";
        CouncilResultModel result = CouncilResultModel.parse("Проверь утверждения", 3, report);
        check("result retains task and mode", "Проверь утверждения".equals(result.task) && result.mode == 3);
        check("expert sections are represented separately", result.experts.size() == 2);
        check("coordinator is a separate structured field", result.coordinator != null && result.coordinator.coordinator);
        check("HTTP errors are tagged on the matching expert", "error".equals(result.experts.get(1).status));
        check("report markers only reflect words present in the report", result.experts.get(0).markers.contains("Совпадает")
                && result.experts.get(0).markers.contains("Предположение") && !result.experts.get(0).markers.contains("Есть источник"));
        check("interrupted tasks are marked", CouncilResultModel.parse("t", 0, "Задача прервана системой").interrupted);
        check("unknown phase duration is explicit", result.experts.get(0).durationMs == -1L);
        System.out.println("CouncilResultModelSmokeTest: " + checks + "/" + checks + " passed");
    }
}
