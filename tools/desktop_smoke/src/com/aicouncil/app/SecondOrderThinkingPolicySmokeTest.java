package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class SecondOrderThinkingPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("first-order only verdict",
                "Прямой эффект положительный, сразу окупится.",
                "");
        signal("both experts first-order only",
                "Немедленная выгода решает.",
                "Immediate gain outweighs everything.");
        signal("fork with explicit second order",
                "Достаточно первого порядка.",
                "Нужно учесть второй порядок и адаптацию.");
        noSignal("second order is named",
                "Прямой эффект хорош, но второй порядок может перевернуть.",
                "");
        noSignal("ordinary recommendation stays quiet",
                "Выгода положительная с учётом рисков.",
                "Согласен смотреть на последствия.");

        List<String> idempotent = new ArrayList<String>();
        SecondOrderThinkingPolicy.detect(
                "Прямой эффект положительный, сразу окупится.",
                "",
                idempotent);
        int once = idempotent.size();
        SecondOrderThinkingPolicy.detect(
                "Прямой эффект положительный, сразу окупится.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("SecondOrderThinkingPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        SecondOrderThinkingPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
