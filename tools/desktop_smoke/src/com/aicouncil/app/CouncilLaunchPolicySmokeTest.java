package com.aicouncil.app;

public final class CouncilLaunchPolicySmokeTest {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }

    public static void main(String[] args) {
        check("empty task", CouncilLaunchPolicy.EMPTY_TASK.equals(CouncilLaunchPolicy.taskFieldError("")));
        check("too long", CouncilLaunchPolicy.TASK_TOO_LONG_ERROR.equals(
                CouncilLaunchPolicy.taskFieldError(new String(new char[100001]).replace('\0', 'a'))));
        check("ok task", CouncilLaunchPolicy.taskFieldError("hello") == null);
        check("required fast", CouncilLaunchPolicy.requiredProviderIndices(0).length == 1);
        check("required audit", CouncilLaunchPolicy.requiredProviderIndices(2).length == 2);
        check("required council", CouncilLaunchPolicy.requiredProviderIndices(1).length == 3);
        check("resolve fixed mode", CouncilLaunchPolicy.resolveMode(2, "x",
                new boolean[]{true, true, true}, false, new boolean[]{false, false, false}) == 2);
        boolean[] cfg = {true, true, true};
        boolean[] free = {true, true, true};
        int auto = CouncilLaunchPolicy.resolveMode(4, "короткий вопрос", cfg, false, free);
        check("resolve auto returns 0-3", auto >= 0 && auto <= 3);
        check("failure cancel", CouncilLaunchPolicy.looksLikeFailure("ok", true));
        check("failure prefix", CouncilLaunchPolicy.looksLikeFailure("Не удалось связаться", false));
        check("success", !CouncilLaunchPolicy.looksLikeFailure("Итог: всё хорошо", false));
        check("completion fail", CouncilLaunchPolicy.completionStatus(false, true, 3).contains("предупреждением"));
        check("completion ok", CouncilLaunchPolicy.completionStatus(false, false, 5).startsWith("Готово"));
        check("skip confirm empty", CouncilLaunchPolicy.skipSensitiveConfirm(""));
        check("no skip normal", !CouncilLaunchPolicy.skipSensitiveConfirm("задача"));
        System.out.println("CouncilLaunchPolicySmokeTest " + passed + "/" + (passed + failed));
        if (failed > 0) System.exit(1);
    }
}
