package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class ReasoningFallacyPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("no evidence therefore false", "There is no evidence for X, therefore X does not exist.");
        signal("bandwagon", "Everyone does it, so it must be correct.");
        signal("post hoc", "После этого значит из-за этого изменение точно вызвало результат.");
        signal("special pleading", "Правило для всех кроме нас, потому что мы особенные.");
        signal("slippery slope", "Если разрешить это, то это неизбежно приведёт к хаосу.");
        signal("hasty generalization", "По одному случаю ясно, что все люди всегда так делают.");
        signal("strawman", "This response misrepresents my position instead of addressing it.");
        signal("ad hominem", "You are an idiot, so your argument is wrong.");
        signal("circular reasoning", "Это верно, потому что это верно.");
        signal("appeal to authority", "Эксперт сказал, поэтому это верно, не нужно проверять.");
        signal("appeal to tradition", "Так всегда делали, значит правильно.");
        signal("tu quoque", "А ты сам так делаешь, поэтому твой аргумент неверен.");
        signal("false dichotomy", "Либо соглашайся, либо уходи, других вариантов нет.");
        signal("red herring", "Но что насчёт другой проблемы вместо ответа на основной вопрос.");
        signal("appeal to emotion", "Подумайте о детях, это ужасно, вместо аргументов просто почувствуйте.");
        signal("false equivalence", "Это одно и то же, хотя они разные по сути.");
        signal("no true scotsman", "Настоящий шотландец никогда бы так не сделал, значит он не настоящий.");
        signal("appeal to nature", "Это естественно, значит хорошо и правильно.");
        signal("genetic fallacy", "Произошло от плохого источника, поэтому неверно.");
        signal("burden shift", "Я утверждаю это так, докажи что это не так.");
        signal("moving goalposts", "Хотя ранее требовалось только это, но этого недостаточно, нужно ещё.");
        signal("sunk cost", "Уже вложили много, значит надо продолжать несмотря на данные.");
        signal("composition", "Каждая часть хорошая, значит целое тоже хорошее без обоснования.");
        signal("division", "Целое надёжное, значит каждая часть надёжная.");
        signal("equivocation", "Слово используется в разных смыслах внутри аргумента.");
        signal("cherry picking", "Только удобные данные подтверждают, игнорируя противоположные.");
        signal("false cause", "Вместе с этим значит причина без механизма.");

        noSignal("missing evidence alone is not proof of a fallacy",
                "No evidence was found yet, so the claim remains unresolved.");
        noSignal("popularity without an inference is not enough",
                "Many people use this product; usage is not evidence either way.");
        noSignal("temporal order without a causal claim is not enough",
                "After this, the temperature changed, but we do not know why.");
        noSignal("one case reported without group generalization is not enough",
                "One case was reported; it should not be generalized without more data.");
        noSignal("personal disagreement without insult is not ad hominem",
                "I disagree with your conclusion, but the reasoning should still be checked.");
        noSignal("tradition described without normative conclusion is not enough",
                "This practice has historical roots; its current effectiveness needs evaluation.");
        noSignal("emotion without substituting for argument is not enough",
                "This outcome is sad, but we should still examine the data.");
        noSignal("similarity without claiming full equivalence is not enough",
                "These two cases share some features but differ in important ways.");
        noSignal("natural description without normative claim is not appeal to nature",
                "This process is natural; its desirability depends on the context.");
        noSignal("origin mentioned without dismissing the content is not genetic fallacy",
                "The idea originates from earlier work; its current validity needs separate evaluation.");
        noSignal("past investment described without requiring continuation is not sunk cost",
                "We already invested; future decisions should still be based on current data.");
        noSignal("parts properties mentioned without transferring to whole is not composition",
                "Each part has this property; the whole needs separate analysis.");

        List<String> capped = new ArrayList<>();
        String many = "No evidence therefore X is false. Everyone does it, so it must be correct. "
                + "После этого значит из-за этого. Правило для всех кроме нас, потому что мы особенные. "
                + "Если разрешить это, то это неизбежно приведёт к хаосу. По одному случаю ясно, что все люди всегда так делают. "
                + "This response misrepresents my position. You are an idiot, so your argument is wrong. "
                + "Это верно, потому что это верно. Эксперт сказал поэтому это верно. Так всегда делали значит правильно. "
                + "Подумайте о детях вместо аргументов. Это одно и то же хотя разные. Настоящий никогда бы так не сделал значит не настоящий. "
                + "Уже вложили значит продолжать. Каждая часть значит целое.";
        ReasoningFallacyPolicy.detect(many, many, capped);
        check(capped.size() <= 10, "number of signals is bounded");
        List<String> unique = new ArrayList<>(capped);
        ReasoningFallacyPolicy.detect(many, many, capped);
        check(capped.size() == unique.size(), "repeated scan does not append duplicate signals");
        check(capped.stream().allMatch(x -> x.contains("возможный сигнал") && x.contains("контекста")),
                "signals are explicitly qualified as possible and context-dependent");
        System.out.println("ReasoningFallacyPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String answer) {
        List<String> out = new ArrayList<>();
        ReasoningFallacyPolicy.detect(answer, "", out);
        check(!out.isEmpty(), label + " signal detected");
        check(out.stream().allMatch(x -> x.contains("логический риск")), label + " is not stated as proven");
    }

    private static void noSignal(String label, String answer) {
        List<String> out = new ArrayList<>();
        ReasoningFallacyPolicy.detect(answer, "", out);
        check(out.isEmpty(), label);
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
