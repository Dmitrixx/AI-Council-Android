package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class SkinInTheGamePolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("others hold the loss",
                "Риск несут они, не наши деньги. Поэтому делаем и внедряем.",
                "");
        signal("both close without downside owner",
                "We bear no downside, someone else pays. We should proceed.",
                "Потери на них, пострадают они. Вердикт: да, стоит сделать.");
        noSignal("downside owner is named",
                "Чужими деньгами легко советовать, но кто несёт убыток — рекомендатель. Не закрываю.",
                "");
        noSignal("named principle stays quiet",
                "This is skin in the game: we bear no downside, so we should not proceed as if insulated.",
                "");
        noSignal("shifted risk without a recommendation",
                "Downside falls on others. I am not closing the choice.",
                "Риск несёт другой. Выбор ещё не закрыт.");
        noSignal("ordinary recommendation",
                "Пилот на две недели, откат за день. Рекомендую его команде, которая и понесёт операционную нагрузку.",
                "");

        List<String> idempotent = new ArrayList<String>();
        SkinInTheGamePolicy.detect(
                "Not our money. Therefore делаем? We should proceed because someone else pays.",
                "",
                idempotent);
        int once = idempotent.size();
        SkinInTheGamePolicy.detect(
                "Not our money. We should proceed because someone else pays.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("SkinInTheGamePolicySmokeTest: " + passed + "/" + passed + " passed");
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
        SkinInTheGamePolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
