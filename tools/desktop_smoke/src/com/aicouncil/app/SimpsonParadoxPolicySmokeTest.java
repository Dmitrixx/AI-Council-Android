package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class SimpsonParadoxPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("aggregate trend applied to every group",
                "В целом продажи растут, значит в каждой группе тоже растёт. Поэтому для всех сегментов расширяемся.",
                "");
        signal("both experts project the aggregate",
                "По всей выборке эффект положительный, значит для каждой группы он тоже положительный.",
                "Overall trend is up, so it holds in every subgroup and each subgroup should scale.");
        noSignal("stratified reversal is named",
                "В целом тренд есть, но внутри подгрупп связь разворачивается — это Симпсон, не переносится на каждую группу.",
                "");
        noSignal("ordinary average is not a subgroup claim",
                "В среднем конверсия около двенадцати процентов. Диапазон 8–16.",
                "Согласен с диапазоном, без переноса на сегменты.");
        noSignal("subgroup language without an aggregate leap",
                "Проверим каждую группу отдельно, прежде чем решать.",
                "");

        List<String> idempotent = new ArrayList<String>();
        SimpsonParadoxPolicy.detect(
                "В целом растёт, значит в каждой группе растёт.",
                "",
                idempotent);
        int once = idempotent.size();
        SimpsonParadoxPolicy.detect(
                "В целом растёт, значит в каждой группе растёт.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("SimpsonParadoxPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        SimpsonParadoxPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
