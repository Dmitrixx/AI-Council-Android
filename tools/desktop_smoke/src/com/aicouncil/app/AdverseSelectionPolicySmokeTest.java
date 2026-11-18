package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

/** Offline smoke for AdverseSelectionPolicy */
public final class AdverseSelectionPolicySmokeTest {
    public static void main(String[] args) {
        int failed = 0;
        failed += check("empty stays quiet", quiet("", ""));
        failed += check("null-safe", quiet(null, null));
        failed += check("ordinary advice stays quiet",
                quiet("Сравните цену и покрытие до покупки.", "Look at the deductible first."));
        failed += check("acknowledged selection stays quiet",
                quiet("Кто оформит страховку — не средний клиент: это неблагоприятный отбор, пул смещён.", ""));
        failed += check("contract talk alone stays quiet",
                quiet("Кто оформит страховку, получит выплату быстрее.", ""));
        failed += check("typicality alone stays quiet",
                quiet("Качество пула не ухудшится, клиенты останутся прежними.", ""));
        failed += check("moral hazard is not this check",
                quiet("Убытки покрывает страховка, и будут так же осторожны.", ""));
        failed += check("russian pool claim fires",
                fires("Кто оформит страховку — такие же как средний клиент, качество пула не ухудшится.", ""));
        failed += check("english pool claim fires",
                fires("Those who take the offer are the same as the average customer, so the pool will not worsen.", ""));
        failed += check("both experts fire dual signal",
                both("Заёмщики которые придут такие же как средний, пул останется средним.",
                        "Applicants who apply are typical of everyone and the pool stays average."));
        failed += check("selection bias sample is not a contract pool",
                quiet("Опрошенные значит рекомендую запустить опрос ещё раз.", ""));

        if (failed > 0) {
            System.err.println("AdverseSelectionPolicySmokeTest: " + failed + " failed");
            System.exit(1);
        }
        System.out.println("AdverseSelectionPolicySmokeTest: 11/11 passed");
    }

    private static boolean quiet(String a, String b) {
        List<String> out = new ArrayList<String>();
        AdverseSelectionPolicy.detect(a, b, out);
        return out.isEmpty();
    }

    private static boolean fires(String a, String b) {
        List<String> out = new ArrayList<String>();
        AdverseSelectionPolicy.detect(a, b, out);
        if (out.isEmpty()) return false;
        for (String s : out) {
            if (s.contains("adverse selection") && s.contains("possible signal")) return true;
        }
        return false;
    }

    private static boolean both(String a, String b) {
        List<String> out = new ArrayList<String>();
        AdverseSelectionPolicy.detect(a, b, out);
        boolean single = false;
        boolean dual = false;
        for (String s : out) {
            if (s.contains("оба эксперта")) dual = true;
            if (s.contains("adverse selection")) single = true;
        }
        return single && dual;
    }

    private static int check(String name, boolean ok) {
        if (ok) return 0;
        System.err.println("FAIL: " + name);
        return 1;
    }
}
