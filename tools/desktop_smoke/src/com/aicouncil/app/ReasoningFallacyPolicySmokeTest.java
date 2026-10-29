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
        signal("false compromise", "Истина посередине, поэтому усредним позиции и оба понемногу правы.");
        signal("anchoring", "Первое названное число стало якорем, поэтому около него и останемся рядом.");
        signal("gambler", "Уже три раза подряд неудача, значит сейчас вероятность выросла и должно выпасть.");
        signal("texas sharpshooter", "После данных выбрали кластер и нарисовали круг, поэтому паттерн значим.");
        signal("relative privation", "Есть проблемы хуже, не так плохо как там, поэтому не важно и можно не учитывать.");
        signal("nirvana", "Вариант не идеально и есть недостатки, поэтому отвергнуть и лучше ничего.");
        signal("continuum", "Нет чёткой границы между ними, значит нет различия и можно не различать.");
        pair("opposing recommendations must not be averaged",
                "Нужно запретить запуск, делать это нельзя.",
                "Стоит разрешить запуск: истина посередине, усредним позиции.");

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
        noSignal("refusing to average is not a false compromise",
                "Не усреднять развилку: середина скрыла бы конфликт, нужно выбрать сторону.");
        noSignal("independent trials are not the gambler's fallacy",
                "Серия неудач не меняет шанс: испытания независимы, вероятность не меняется.");
        noSignal("a number with a separate estimate is not anchoring",
                "Первое названное число отброшено: отдельно оценил диапазон, а не останемся рядом.");
        noSignal("pre-registered cluster is not texas sharpshooter",
                "Гипотеза заранее зафиксирована до сбора данных; кластер не выделили после факта.");
        noSignal("acknowledging worse problems without dismissing is not relative privation",
                "Есть проблемы хуже, но оба важны и нужно сравнивать масштаб текущего вреда.");
        noSignal("imperfect but better than status quo is not nirvana",
                "Вариант не идеален, но лучше текущего и улучшает ситуацию поэтапно.");
        noSignal("gray area acknowledged without erasing the distinction is not continuum",
                "Нет резкой границы, но различие всё равно важно и пороговый критерий полезен.");

        List<String> capped = new ArrayList<>();
        String many = "No evidence therefore X is false. Everyone does it, so it must be correct. "
                + "После этого значит из-за этого. Правило для всех кроме нас, потому что мы особенные. "
                + "Если разрешить это, то это неизбежно приведёт к хаосу. По одному случаю ясно, что все люди всегда так делают. "
                + "This response misrepresents my position. You are an idiot, so your argument is wrong. "
                + "Это верно, потому что это верно. Эксперт сказал поэтому это верно. Так всегда делали значит правильно. "
                + "Подумайте о детях вместо аргументов. Это одно и то же хотя разные. Настоящий никогда бы так не сделал значит не настоящий.";
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

    private static void pair(String label, String left, String right) {
        List<String> out = new ArrayList<>();
        ReasoningFallacyPolicy.detect(left, right, out);
        check(out.stream().anyMatch(x -> x.contains("рекомендации расходятся")), label);
        check(out.stream().noneMatch(x -> x.contains("без критерия")), label + " does not also file the unpaired form");
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
