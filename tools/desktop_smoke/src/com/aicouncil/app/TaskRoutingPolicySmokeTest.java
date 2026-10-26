package com.aicouncil.app;

public final class TaskRoutingPolicySmokeTest {
    private static int checks;
    private static void check(String name, boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
    }
    public static void main(String[] args) {
        boolean[] all = {true, true, true};
        boolean[] free = {true, true, true};
        boolean[] notAllFree = {true, false, true};
        check("empty prompt safely defaults to quick answer", TaskRoutingPolicy.chooseMode(" ", all, false, free) == 0);
        check("short ordinary prompt uses quick answer", TaskRoutingPolicy.chooseMode("Explain photosynthesis", all, false, free) == 0);
        check("Russian audit selects independent audit", TaskRoutingPolicy.chooseMode("Проверь ответ на ошибки", all, false, free) == 2);
        check("English fact-check selects audit", TaskRoutingPolicy.chooseMode("Fact-check these claims", all, false, free) == 2);
        check("Ukrainian verification selects audit", TaskRoutingPolicy.chooseMode("Перевір цей розрахунок", all, false, free) == 2);
        check("complex Russian task selects deep analysis", TaskRoutingPolicy.chooseMode("Спроектируй архитектуру и сравни несколько вариантов", all, false, free) == 3);
        check("complex English task selects deep analysis", TaskRoutingPolicy.chooseMode("Compare options and design an architecture step by step", all, false, free) == 3);
        check("complex Ukrainian task selects deep analysis", TaskRoutingPolicy.chooseMode("Порівняй варіанти та спроєктуй архітектуру", all, false, free) == 3);
        check("medium task with full team selects council", TaskRoutingPolicy.chooseMode(repeat("ordinary detail ", 12), all, false, free) == 1);
        check("audit falls back when second expert unavailable", TaskRoutingPolicy.chooseMode("Check this response", new boolean[]{true, false, true}, false, free) == 0);
        check("complex task falls back when coordinator unavailable", TaskRoutingPolicy.chooseMode("Compare options and design architecture", new boolean[]{true, true, false}, false, free) == 0);
        check("free-only policy excludes unmarked second expert", TaskRoutingPolicy.chooseMode("проверь ошибки", all, true, notAllFree) == 0);
        check("free-only policy allows marked audit expert", TaskRoutingPolicy.chooseMode("проверь ошибки", all, true, free) == 2);
        check("missing capability arrays are safe", TaskRoutingPolicy.chooseMode("audit this", null, false, null) == 0);
        check("length threshold selects council when team configured", TaskRoutingPolicy.chooseMode(repeat("detail ", 20), all, false, free) == 1);
        check("irreversible decision selects deep analysis", TaskRoutingPolicy.chooseMode("Стоит ли принять необратимое решение о запуске продукта", all, false, free) == 3);
        System.out.println("TaskRoutingPolicySmokeTest: " + checks + "/" + checks + " passed");
    }
    private static String repeat(String value, int count) {
        StringBuilder out = new StringBuilder(value.length() * count);
        for (int i = 0; i < count; i++) out.append(value);
        return out.toString();
    }
}
