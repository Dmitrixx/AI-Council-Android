package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ScopeInsensitivityPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("magnitude dismissed",
                "Масштаб не важен: 10 или 10000 одно и то же для вывода.",
                "");
        signal("both experts ignore scope",
                "Число не важно, масштаб не меняет вывод.",
                "Scope doesn't matter. Whether 10 or 10000, the call is the same.");
        signal("dismissal versus an explicit ratio",
                "Масштаб не влияет на рекомендацию.",
                "Масштаб учтён: эффект в 1000 раз больше, пропорционально масштабу.");
        noSignal("ratio is already in the answer",
                "Масштаб не важен как лозунг, но масштаб учтён: эффект в 100 раз выше.",
                "");
        noSignal("ordinary small effect is quiet",
                "Эффект пилота небольшой, выборку расширим.",
                "Согласен, эффект пока скромный.");
        noSignal("named bias plus magnitude stays quiet",
                "Это похоже на scope insensitivity, но отношение величин в 100 раз.",
                "");

        List<String> idempotent = new ArrayList<String>();
        ScopeInsensitivityPolicy.detect(
                "Масштаб не важен и число не важно.",
                "",
                idempotent);
        int once = idempotent.size();
        ScopeInsensitivityPolicy.detect(
                "Масштаб не важен и число не важно.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("ScopeInsensitivityPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        ScopeInsensitivityPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
