package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class NarrativeFallacyPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("story treated as proof",
                "По этой истории ясно: история доказывает успех, цифры не нужны.",
                "");
        signal("both experts close with story",
                "Убедительная история значит, что риск низкий.",
                "The coherent story means the downside is small.");
        signal("story versus explicit base rate",
                "История убедительнее цифр, поэтому запускаем.",
                "Base rate first: prefer the reference class over the story.");
        noSignal("separation is named",
                "Кажется, что история доказывает, но история не доказательство и base rate важнее сюжета.",
                "");
        noSignal("ordinary story mention stays quiet",
                "Расскажу пример из практики, но опираюсь на частоту в классе.",
                "Согласен смотреть на reference class.");
        noSignal("named fallacy plus separation stays quiet",
                "Это narrative fallacy: story is not a base rate.",
                "");

        List<String> idempotent = new ArrayList<String>();
        NarrativeFallacyPolicy.detect(
                "История доказывает. Сюжет важнее base rate.",
                "",
                idempotent);
        int once = idempotent.size();
        NarrativeFallacyPolicy.detect(
                "История доказывает. Сюжет важнее base rate.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("NarrativeFallacyPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        NarrativeFallacyPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
