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
                + "Если решение не идеально, оно бесполезно.";
        DecisionBriefPolicy.detect(overflow, overflow, capped);
        check(capped.size() <= 6, "signals are capped");
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
