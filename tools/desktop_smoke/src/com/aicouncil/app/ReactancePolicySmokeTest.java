package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

/** offline smoke for ReactancePolicy */
public final class ReactancePolicySmokeTest {
    public static void main(String[] args) {
        int failed = 0;
        failed += check("empty stays quiet", quiet("", ""));
        failed += check("null-safe", quiet(null, null));
        failed += check("ordinary advice stays quiet",
                quiet("Рекомендую рассмотреть вариант A.", "Стоит взвесить плюсы и минусы."));
        failed += check("acknowledged autonomy tension stays quiet",
                quiet("Ощущение принуждения не аргумент; оцениваю по существу.", ""));
        failed += check("naive forced-reject fires",
                fires("Отвергаю, потому что заставляют — раз нет выбора, значит против.", ""));
        failed += check("english forced-reject fires",
                fires("I reject because it feels forced and there is no choice.", ""));
        failed += check("coercive frame + autonomy reject fires",
                fires("Вы обязаны это сделать, отказать нельзя. Поэтому отказываюсь.", ""));
        failed += check("both experts fire dual signal",
                both("Не буду, раз принуждают — это звучит как приказ, поэтому нет.",
                        "Sounds like an order, so no — I resist because they demand."));
        failed += check("plain disagreement is not reactance",
                quiet("Я не согласен с выводом.", "Вывод выглядит слабым."));
        failed += check("directive recommendation alone is quiet",
                quiet("Нужно сделать X.", "Следует выполнить шаг Y."));

        if (failed > 0) {
            System.err.println("ReactancePolicySmokeTest: " + failed + " failed");
            System.exit(1);
        }
        System.out.println("ReactancePolicySmokeTest: 10/10 passed");
    }

    private static boolean quiet(String a, String b) {
        List<String> out = new ArrayList<String>();
        ReactancePolicy.detect(a, b, out);
        return out.isEmpty();
    }

    private static boolean fires(String a, String b) {
        List<String> out = new ArrayList<String>();
        ReactancePolicy.detect(a, b, out);
        if (out.isEmpty()) return false;
        for (String s : out) {
            if (s.contains("reactance") && s.contains("possible signal")) return true;
        }
        return false;
    }

    private static boolean both(String a, String b) {
        List<String> out = new ArrayList<String>();
        ReactancePolicy.detect(a, b, out);
        boolean single = false;
        boolean dual = false;
        for (String s : out) {
            if (s.contains("оба эксперта")) dual = true;
            if (s.contains("reactance")) single = true;
        }
        return single && dual;
    }

    private static int check(String name, boolean ok) {
        if (ok) return 0;
        System.err.println("FAIL: " + name);
        return 1;
    }
}
