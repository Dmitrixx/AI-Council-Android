package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class DecisionBriefPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("sunk cost", "Мы уже вложили год, поэтому продолжаем и нельзя останавливаться.");
        signal("survivorship", "Только успешные компании так делают, значит это работает.");
        signal("novelty", "Потому что это новое, значит лучше.");
        signal("consequences", "Это не может быть правдой, потому что тогда будет плохо.");
        signal("cherry pick", "Оставим только подтверждающие данные, противоречащие отбросим.");
        signal("base rate", "Этот случай яркий, значит вероятность высока.");
        signal("nirvana", "Если решение не идеально, оно бесполезно.");
        signal("motivated reasoning", "Хочу чтобы это было правдой, поэтому вероятно так и есть.");
        signal("status quo", "Оставьте как есть, потому что так привычно и уже работает.");
        signal("zero risk", "Полностью устранить риск важнее чем снизить большой, даже если крупный останется.");
        signal("scope neglect", "Одинаково важно, 10 или 10000 — scale does not matter.");

        noSignal("spent effort without a continue-because claim",
                "Мы уже вложили год. Это не причина продолжать само по себе.");
        noSignal("winners mentioned together with failures",
                "Только успешные видны сразу, но неудачи тоже есть, поэтому вывод рано делать.");
        noSignal("new without better", "Инструмент новый, качество ещё не сравнивали.");
        noSignal("bad outcome is not treated as disproof",
                "Последствия были бы тяжёлыми, но это не делает тезис ложным.");
        noSignal("case with an explicit base rate",
                "Этот случай ярок, но базовая частота в похожем классе низкая, поэтому шанс невелик.");
        noSignal("imperfect option kept in play",
                "Решение не идеально, но всё равно может быть лучшим из доступных.");
        noSignal("wish without probability claim",
                "Хотелось бы чтобы это сработало; вероятность нужно считать отдельно.");
        noSignal("status quo with explicit comparison",
                "Оставить как есть можно, но сравнили альтернативы и cost of change выше.");
        noSignal("risk reduction ranked by expected harm",
                "Снижаем крупный риск по expected harm, а не гонимся за zero risk малого.");
        noSignal("scope treated proportionally",
                "Влияние масштабируется: на 10 000 человек это пропорционально сильнее, чем на 10.");

        List<String> many = new ArrayList<>();
        String packed = "Мы уже вложили год, поэтому продолжаем и нельзя останавливаться. "
                + "Только успешные компании так делают, значит это работает. "
                + "Потому что это новое, значит лучше. "
                + "Это не может быть правдой, потому что тогда будет плохо.";
        DecisionBriefPolicy.detect(packed, "", many);
        check(many.size() == 4, "four distinct signals, under the cap");
        check(many.stream().allMatch(x -> x.contains("возможный сигнал") && x.contains("контекста")),
                "signals stay hypotheses");
        int size = many.size();
        DecisionBriefPolicy.detect(packed, "", many);
        check(many.size() == size, "repeat scan does not duplicate");
        List<String> capped = new ArrayList<>();
        String overflow = packed + " Только подтверждающие данные оставим. "
                + "Этот случай яркий, значит вероятность высока. "
                + "Если решение не идеально, оно бесполезно. "
                + "Хочу чтобы это было правдой, поэтому вероятно так и есть.";
        DecisionBriefPolicy.detect(overflow, overflow, capped);
        check(capped.size() <= 8, "signals are capped");
        System.out.println("DecisionBriefPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String answer) {
        List<String> out = new ArrayList<>();
        DecisionBriefPolicy.detect(answer, "", out);
        check(!out.isEmpty(), label + " detected");
        check(out.stream().allMatch(x -> x.startsWith("карта решения")), label + " is labelled as a map signal");
    }

    private static void noSignal(String label, String answer) {
        List<String> out = new ArrayList<>();
        DecisionBriefPolicy.detect(answer, "", out);
        check(out.isEmpty(), label);
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            System.err.println("FAIL " + label);
            System.exit(1);
        }
        passed++;
    }
}
