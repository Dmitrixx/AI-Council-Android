package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class DecisionFramePolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        check(DecisionFramePolicy.block(null).isEmpty(), "null task is silent");
        check(DecisionFramePolicy.block("Стоит ли запускать?").isEmpty(), "short task is silent");

        String open = "Стоит ли нам немедленно переписать весь биллинг на новую платформу и закрыть выбор уже сегодня?";
        String openFrame = DecisionFramePolicy.block(open);
        check(openFrame.contains("КАДР РЕШЕНИЯ"), "open decision gets a frame");
        check(openFrame.contains("Режим: решение"), "decision cue selects decision mode");
        check(openFrame.contains("Альтернативы: в тексте не выделены"), "missing alternatives are named");
        check(openFrame.contains("Величины: не найдены"), "missing magnitude is named");
        check(openFrame.contains("кто несёт убыток"), "missing downside bearer is named");
        check(openFrame.contains("критерий успеха не назван"), "missing success metric is named");
        check(!openFrame.contains("горизонт решения не задан"), "today counts as a horizon");

        String closed = "Стоит ли запускать новый тариф или оставить текущий? "
                + "Решить в этом квартале: убыток несёт команда, базовая частота похожих запусков 20% "
                + "и критерий успеха — удержание 30 дней.";
        String closedFrame = DecisionFramePolicy.block(closed);
        check(closedFrame.contains("запускать новый тариф"), "first alternative is extracted");
        check(closedFrame.contains("оставить текущий"), "second alternative is extracted");
        check(!closedFrame.contains("Решить в этом квартале"), "the next sentence is not an alternative");
        check(closedFrame.contains("20 %"), "percent magnitude is extracted");
        check(closedFrame.contains("явных дыр в постановке не найдено"), "a complete brief has no gaps");

        String forecast = "Оцени шанс, что через год продукт ещё будет жив на рынке подписок без смены модели.";
        String forecastFrame = DecisionFramePolicy.block(forecast);
        check(forecastFrame.contains("Режим: прогноз"), "chance plus horizon is a forecast");
        check(forecastFrame.contains("базовой частоты"), "forecast without a base rate names that gap");

        signal(open, "Поэтому выбираем переписать всё. Однозначно стоит. Закрываем выбор.");
        noSignal(open, "Альтернативы не названы, масштаб не задан. Сначала проверить, а не закрывать выбор.");
        noSignal(closed, "Поэтому выбираем тариф. Закрываем выбор.");
        noSignal("Короткий", "Поэтому выбираем. Закрываем выбор.");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(open,
                "Поэтому выбираем переписать всё. Однозначно стоит. Закрываем выбор.",
                "",
                wired);
        check(contains(wired, "decision frame"), "detectAll wires the decision frame");

        List<String> map = new ArrayList<String>();
        map.add("decision frame (possible signal, requires context): вердикт закрывает выбор");
        StringBuilder cue = new StringBuilder();
        CoordinatorCue.append(cue, map);
        check(cue.toString().contains("неизвестно"), "coordinator refuses to close an open frame");

        List<String> assumptions = new ArrayList<String>();
        assumptions.add("key assumptions (possible signal, requires context): скрытые предпосылки");
        StringBuilder assumptionCue = new StringBuilder();
        CoordinatorCue.append(assumptionCue, assumptions);
        check(assumptionCue.toString().contains("допущен"), "key assumptions reach the coordinator");

        List<String> threshold = new ArrayList<String>();
        threshold.add("threshold effects (possible signal, requires context): порог не назван");
        StringBuilder thresholdCue = new StringBuilder();
        CoordinatorCue.append(thresholdCue, threshold);
        check(thresholdCue.toString().contains("порог"), "threshold effects reach the coordinator");

        List<String> capped = new ArrayList<String>();
        String many = "Поэтому выбираем переписать всё. Однозначно стоит. Закрываем выбор.";
        DecisionFramePolicy.detect(open, many, many, capped);
        int first = capped.size();
        DecisionFramePolicy.detect(open, many, many, capped);
        check(capped.size() == first, "repeated scan does not append duplicate signals");
        check(first > 0 && first <= 2, "number of signals is bounded");
        check(capped.get(0).contains("possible signal"), "signals stay hypotheses");

        System.out.println("DecisionFramePolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String task, String text) {
        List<String> out = new ArrayList<String>();
        DecisionFramePolicy.detect(task, text, "", out);
        check(!out.isEmpty(), "closing past an open frame is a signal");
    }

    private static void noSignal(String task, String text) {
        List<String> out = new ArrayList<String>();
        DecisionFramePolicy.detect(task, text, "", out);
        check(out.isEmpty(), "no signal: " + task.substring(0, Math.min(24, task.length())));
    }

    private static boolean contains(List<String> items, String needle) {
        for (String item : items) {
            if (item.contains(needle)) return true;
        }
        return false;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
