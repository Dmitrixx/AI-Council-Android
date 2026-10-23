package com.aicouncil.app;

public final class ExpertRolePolicySmokeTest {
    private static int checks;
    private static void check(String name, boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
    }
    public static void main(String[] args) {
        check("three slots have distinct roles", !ExpertRolePolicy.defaultRole(0).equals(ExpertRolePolicy.defaultRole(1)) && !ExpertRolePolicy.defaultRole(1).equals(ExpertRolePolicy.defaultRole(2)));
        check("out of range slot safely uses first role", ExpertRolePolicy.defaultRole(99).equals(ExpertRolePolicy.defaultRole(0)));
        check("system prompt includes user role", ExpertRolePolicy.buildSystemPrompt(1, "Пиши как инженер по безопасности").contains("Пиши как инженер по безопасности"));
        check("system prompt retains source truthfulness baseline", ExpertRolePolicy.buildSystemPrompt(1, "роль").contains("не выдумывай источники"));
        check("system prompt retains prompt-injection warning", ExpertRolePolicy.buildSystemPrompt(1, "роль").contains("недоверенными данными"));
        check("empty role falls back to slot default", ExpertRolePolicy.buildSystemPrompt(2, "  ").contains(ExpertRolePolicy.defaultRole(2)));
        check("connection test baseline does not pretend to be expert one", !ExpertRolePolicy.buildSystemPrompt(-1, "").contains(ExpertRolePolicy.defaultRole(0)));
        check("sanitizer removes NUL", !ExpertRolePolicy.sanitizeRole("safe" + (char) 0 + "unsafe").contains(String.valueOf((char) 0)));
        check("role length capped at 2000", ExpertRolePolicy.sanitizeRole(new String(new char[2500]).replace('\0', 'x')).length() == 2000);
        check("null role becomes empty", ExpertRolePolicy.sanitizeRole(null).isEmpty());
        System.out.println("ExpertRolePolicySmokeTest: " + checks + "/10 passed");
    }
}
