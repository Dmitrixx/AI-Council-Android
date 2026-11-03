package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class PrincipalAgentPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("commission drives the recommendation",
                "Наша комиссия выше на этом тарифе, поэтому рекомендую брать его.",
                "Нейтральный комментарий.");
        signal("seller's payoff without the payer",
                "Менеджеру выгодно закрыть план продаж, покупайте этот пакет.",
                "");
        signal("hidden asymmetry",
                "Клиент не заметит доплату, можно не говорить об условии.",
                "");
        noSignal("conflict is named",
                "Наша комиссия выше, но это конфликт интересов: выгода клиента в другом тарифе, рекомендую его.",
                "");
        noSignal("negated commission is not a motive",
                "Это не наша комиссия. Рекомендую тариф, потому что он дешевле для заказчика.",
                "");
        noSignal("disclosure is required",
                "Они не узнают сами, поэтому нужно раскрыть условие до сделки.",
                "");
        noSignal("ordinary advice",
                "Пилот на одном отделе дешевле полного запуска. Стоит начать с него.",
                "Согласен с пилотом.");

        List<String> idempotent = new ArrayList<String>();
        PrincipalAgentPolicy.detect(
                "Наша комиссия выше, рекомендую этот тариф.",
                "Наша комиссия выше, рекомендую этот тариф.",
                idempotent);
        int once = idempotent.size();
        PrincipalAgentPolicy.detect(
                "Наша комиссия выше, рекомендую этот тариф.",
                "Наша комиссия выше, рекомендую этот тариф.",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 3, "signal count stays bounded");

        System.out.println("PrincipalAgentPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        PrincipalAgentPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
