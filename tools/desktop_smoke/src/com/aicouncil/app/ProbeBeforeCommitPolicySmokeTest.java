package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ProbeBeforeCommitPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("irreversible migration with no probe",
                "Нужно сразу мигрировать все данные: точка невозврата, отката не будет.",
                "");
        signal("vague demand for more data",
                "Нужно больше данных и ещё исследований, потом решим.",
                "");
        signal("commit versus a reversible probe",
                "Подписываем навсегда, нельзя отменить, точка невозврата.",
                "Сначала пилот на десяти пользователях: можно откатить.");
        noSignal("irreversible step already has a cheap probe",
                "Точка невозврата есть, но сначала пилот, можно откатить.",
                "");
        noSignal("more data names the observation that flips the call",
                "Нужно больше данных: если тест покажет отток выше 8%, решение перевернётся.",
                "");
        noSignal("ordinary reversible pilot",
                "Запускаем пилот в этом квартале, можно откатить, если метрика плохая.",
                "Согласен с пилотом.");
        noSignal("refusal of an irreversible step",
                "Не делай необратимый шаг: сначала пилот и дешёвая проверка.",
                "");

        List<String> idempotent = new ArrayList<String>();
        ProbeBeforeCommitPolicy.detect(
                "Точка невозврата, отката не будет, нельзя отменить.",
                "",
                idempotent);
        int once = idempotent.size();
        ProbeBeforeCommitPolicy.detect(
                "Точка невозврата, отката не будет, нельзя отменить.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("ProbeBeforeCommitPolicySmokeTest: " + passed + "/" + passed + " passed");
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
        ProbeBeforeCommitPolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
