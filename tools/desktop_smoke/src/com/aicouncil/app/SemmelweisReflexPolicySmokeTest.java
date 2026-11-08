package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

/** Offline smoke for SemmelweisReflexPolicy */
public final class SemmelweisReflexPolicySmokeTest {
    public static void main(String[] args) {
        int failed = 0;
        failed += check("empty stays quiet", quiet("", ""));
        failed += check("null-safe", quiet(null, null));
        failed += check("ordinary advice stays quiet",
                quiet("Рекомендую проверить данные независимо.", "Стоит собрать больше наблюдений."));
        failed += check("acknowledged paradigm tension stays quiet",
                quiet("Данные против принятой модели, но устоявшаяся парадигма может быть неполной.", ""));
        failed += check("naive orthodoxy rejection fires",
                fires("Это противоречит устоявшейся парадигме, поэтому можно сразу отвергнуть.", ""));
        failed += check("english orthodoxy rejection fires",
                fires("This contradicts the established paradigm, so we can dismiss it.", ""));
        failed += check("paradigm defense + dismiss fires",
                fires("Устоявшаяся парадигма говорит иначе, поэтому отбрасываем.", ""));
        failed += check("both experts fire dual signal",
                both("Это невозможно, потому что противоречит принятой модели.",
                        "This goes against the accepted model, therefore we can dismiss."));
        failed += check("plain disagreement is not the reflex",
                quiet("Я не согласен с выводом.", "Вывод выглядит слабым."));
        failed += check("confirmation-style language alone is quiet",
                quiet("Данные подтверждают нашу модель.", "Модель хорошо объясняет наблюдения."));

        if (failed > 0) {
            System.err.println("SemmelweisReflexPolicySmokeTest: " + failed + " failed");
            System.exit(1);
        }
        System.out.println("SemmelweisReflexPolicySmokeTest: 10/10 passed");
    }

    private static boolean quiet(String a, String b) {
        List<String> out = new ArrayList<String>();
        SemmelweisReflexPolicy.detect(a, b, out);
        return out.isEmpty();
    }

    private static boolean fires(String a, String b) {
        List<String> out = new ArrayList<String>();
        SemmelweisReflexPolicy.detect(a, b, out);
        if (out.isEmpty()) return false;
        for (String s : out) {
            if (s.contains("semmelweis-reflex") && s.contains("possible signal")) return true;
        }
        return false;
    }

    private static boolean both(String a, String b) {
        List<String> out = new ArrayList<String>();
        SemmelweisReflexPolicy.detect(a, b, out);
        boolean single = false;
        boolean dual = false;
        for (String s : out) {
            if (s.contains("оба эксперта")) dual = true;
            if (s.contains("semmelweis-reflex")) single = true;
        }
        return single && dual;
    }

    private static int check(String name, boolean ok) {
        if (ok) return 0;
        System.err.println("FAIL: " + name);
        return 1;
    }
}
