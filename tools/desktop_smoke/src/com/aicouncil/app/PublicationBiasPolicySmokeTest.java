package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class PublicationBiasPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("published slice closed as proof",
                "Все статьи подтверждают пользу. Поэтому это работает, эффект доказан.",
                "");
        signal("both experts close on successes",
                "Only the successes count. Success stories prove it, therefore it works.",
                "Только успешные кейсы. Все кейсы успешные, можно внедрять, вердикт: эффект есть.");
        noSignal("file drawer is named",
                "Все статьи подтверждают, но ящик стола не пуст: неопубликованные неудачи не посчитаны.",
                "");
        noSignal("named bias stays quiet",
                "This is publication bias. Every published paper agrees, but that is not the effect is real.",
                "");
        noSignal("successes without a close",
                "Only the successes are on the slide. I am not closing the effect.",
                "Только опубликованные работы на столе. Эффект ещё не доказан.");
        noSignal("ordinary evidence note",
                "Два независимых отчёта с отрицательным результатом тоже лежат в папке. Эффект не закрываю.",
                "");

        List<String> idempotent = new ArrayList<String>();
        PublicationBiasPolicy.detect(
                "Every published paper agrees, therefore it works. The effect is real.",
                "",
                idempotent);
        int once = idempotent.size();
        PublicationBiasPolicy.detect(
                "Every published paper agrees, therefore it works. The effect is real.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("PublicationBiasPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String a, String b) {
        List<String> out = run(a, b);
        check(!out.isEmpty(), label + " should signal");
        if (!out.isEmpty()) {
            check(out.get(0).contains("possible signal"), label + " is marked as a hypothesis");
        }
    }

    private static void noSignal(String label, String a, String b) {
        check(run(a, b).isEmpty(), label + " should stay quiet");
    }

    private static List<String> run(String a, String b) {
        List<String> out = new ArrayList<String>();
        PublicationBiasPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
