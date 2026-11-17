package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class AnecdotalEvidencePolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("story used as the decision",
                "У меня был случай: знакомый бросил схему и выздоровел, поэтому выбираем отмену.",
                "");
        signal("english second-hand story closes the choice",
                "I know a guy who skipped the protocol and got better, so we should choose the same.",
                "");
        signal("both experts close on a story",
                "Моя тётя так лечилась, поэтому выбираем этот курс.",
                "A friend of mine did this, therefore we recommend it.");
        noSignal("story named as illustration",
                "У меня был случай, но это только иллюстрация, не частота. Базовая частота неизвестна.",
                "");
        noSignal("friend without a close",
                "Мой знакомый советует книгу.",
                "");
        noSignal("named gap stays quiet",
                "Это anecdotal evidence: один рассказ не задаёт частоту, поэтому не закрываем выбор.",
                "");

        List<String> idempotent = new ArrayList<String>();
        AnecdotalEvidencePolicy.detect(
                "У меня был случай, поэтому выбираем отмену. Это доказывает, что схема не нужна.",
                "",
                idempotent);
        int once = idempotent.size();
        AnecdotalEvidencePolicy.detect(
                "У меня был случай, поэтому выбираем отмену. Это доказывает, что схема не нужна.",
                "",
                idempotent);
        check(idempotent.size() == once, "duplicate scan does not stack the same signal");
        check(once > 0 && once <= 2, "signal count stays bounded");

        System.out.println("AnecdotalEvidencePolicySmokeTest: " + passed + "/" + passed + " passed");
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
        AnecdotalEvidencePolicy.detect(a, b, out);
        return out;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
