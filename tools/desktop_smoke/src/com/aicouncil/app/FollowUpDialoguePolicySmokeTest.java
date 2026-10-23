package com.aicouncil.app;

public final class FollowUpDialoguePolicySmokeTest {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("PASS " + name); }
        else { failed++; System.out.println("FAIL " + name); }
    }

    public static void main(String[] args) {
        check("clamp", FollowUpDialoguePolicy.clampExpertIndex(-1) == 0
                && FollowUpDialoguePolicy.clampExpertIndex(9) == 2
                && FollowUpDialoguePolicy.clampExpertIndex(1) == 1);
        check("empty q", FollowUpDialoguePolicy.isQuestionEmpty("  "));
        check("context empty", !FollowUpDialoguePolicy.hasValidResultContext(""));
        check("context placeholder", !FollowUpDialoguePolicy.hasValidResultContext(
                "Ваш итоговый ответ появится здесь. Запустите задачу"));
        check("context ok", FollowUpDialoguePolicy.hasValidResultContext("Итог совета: ..."));
        check("follow content", FollowUpDialoguePolicy.buildFollowUserContent("почему?")
                .startsWith(FollowUpDialoguePolicy.FOLLOW_INSTRUCTION_PREFIX)
                && FollowUpDialoguePolicy.buildFollowUserContent("почему?").endsWith("почему?"));
        check("configure msg", FollowUpDialoguePolicy.configureExpertMessage(0).contains("ИИ №1"));
        check("success", FollowUpDialoguePolicy.successStatus(12).contains("12 сек"));
        check("char limit", FollowUpDialoguePolicy.dialogueCharLimit(100000) == 60000
                && FollowUpDialoguePolicy.dialogueCharLimit(0) == 0);
        System.out.println("FollowUpDialoguePolicySmokeTest " + passed + "/" + (passed + failed));
        if (failed > 0) System.exit(1);
    }
}
