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
                + "🔬 АУДИТ ДОКАЗАТЕЛЬСТВ · ИИ №3 · Model C\n\nПроверь первичный источник.\n\n────────────────────\n\n"
                + "🏁 ИТОГ КООРДИНАТОРА · Model D\n\nНеизвестно: источник требует проверки.";
        CouncilResultModel result = CouncilResultModel.parse("Проверь утверждения", 3, report, new long[]{100L, 200L, 300L, 400L});
        check("result retains task and mode", "Проверь утверждения".equals(result.task) && result.mode == 3);
        check("three independent expert sections are represented separately", result.experts.size() == 3);
        check("coordinator is a separate fourth-slot structured field", result.coordinator != null && result.coordinator.coordinator && result.coordinator.durationMs == 400L);
        check("evidence expert has its own role and duration", "Аудитор доказательств".equals(result.experts.get(2).role) && result.experts.get(2).durationMs == 300L);
        check("HTTP errors are tagged on the matching expert", "error".equals(result.experts.get(1).status));
        check("report markers only reflect words present in the report", result.experts.get(0).markers.contains("Совпадает")
                && result.experts.get(0).markers.contains("Предположение") && !result.experts.get(0).markers.contains("Есть источник"));
        check("interrupted tasks are marked", CouncilResultModel.parse("t", 0, "Задача прервана системой").interrupted);
        check("unknown phase duration is explicit", result.experts.get(0).durationMs == 100L);
        System.out.println("CouncilResultModelSmokeTest: " + checks + "/" + checks + " passed");
    }
}
