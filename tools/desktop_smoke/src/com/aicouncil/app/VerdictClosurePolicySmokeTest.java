package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

/** Offline smoke for VerdictClosurePolicy. */
public final class VerdictClosurePolicySmokeTest {
    private static final String DECISION =
            "Стоит ли запускать новый продукт в этом квартале или отказаться и оставить текущий план?";
    private static final String FORECAST =
            "Дай прогноз: какова вероятность успеха этого запуска через год и какой шанс провала?";

    public static void main(String[] args) {
        int failed = 0;
        failed += check("empty stays quiet", quiet(DECISION, "", ""));
        failed += check("null-safe", quiet(null, null, null));
        failed += check("short task stays quiet",
                quiet("стоит ли", longDo(), ""));
        failed += check("prose without a label stays quiet",
                quiet(DECISION,
                        "Сравните цену, спрос и канал, прежде чем выбирать. Это ещё не рекомендация команды.",
                        ""));
        failed += check("word сделать is not the label",
                quiet(DECISION,
                        "Нужно сделать прототип и посмотреть на спрос, но итог я пока не маркирую.",
                        ""));
        failed += check("do without slots fires",
                fires(DECISION, longDo(), ""));
        failed += check("do with falsifier and reversibility stays quiet",
                quiet(DECISION, closedWell(), ""));
        failed += check("naming the gap stays quiet",
                quiet(DECISION,
                        "ИТОГ: ДЕЛАТЬ. Вердикт не закрыт: нет наблюдения, которое перевернёт вывод, и обратимость неизвестна.",
                        ""));
        failed += check("probe with a concrete check stays quiet",
                quiet(DECISION,
                        "ИТОГ: СНАЧАЛА ПРОВЕРИТЬ. Пилот на 20 клиентах: если конверсия ниже 4%, вывод перевернётся.",
                        ""));
        failed += check("bare probe fires",
                fires(DECISION,
                        "ИТОГ КООРДИНАТОРА: СНАЧАЛА ПРОВЕРИТЬ. Нужно подумать ещё и потом вернуться к вопросу позже.",
                        ""));
        failed += check("two labels fire",
                fires(DECISION,
                        "ИТОГ: ДЕЛАТЬ и СНАЧАЛА ПРОВЕРИТЬ одновременно, хотя наблюдение уже есть и шаг необратим.",
                        ""));
        failed += check("both experts add the dual signal",
                both(DECISION, longDo(), longDo().replace("ДЕЛАТЬ", "НЕ ДЕЛАТЬ")));
        failed += check("point forecast fires",
                forecastFires("Итоговая вероятность успеха 70%. Запускать стоит уже в этом виде."));
        failed += check("ranged forecast with reference class stays quiet",
                forecastQuiet("Итоговая вероятность 55–70%. Reference class — похожие запуски, базовая частота около 40%."));
        failed += check("explicit missing base rate stays quiet on that slot",
                forecastQuiet("Вероятность 40–60%. Базовой частоты нет, поэтому диапазон широкий."));
        failed += check("footer reads the coordinator, not an earlier expert",
                footerUsesCoordinator());
        failed += check("footer stays empty when the verdict is complete",
                footer(DECISION, "🏁 ИТОГ КООРДИНАТОРА\n\n" + closedWell()).isEmpty());
        failed += check("decision reminder is present",
                VerdictClosurePolicy.reminder(DECISION).contains("СНАЧАЛА ПРОВЕРИТЬ"));
        failed += check("forecast reminder is present",
                VerdictClosurePolicy.reminder(FORECAST).contains("диапазон"));
        failed += check("short reminder is empty",
                VerdictClosurePolicy.reminder("стоит ли").isEmpty());
        failed += check("cue matches the closure signal", cueMatches());

        if (failed > 0) {
            System.err.println("VerdictClosurePolicySmokeTest: " + failed + " failed");
            System.exit(1);
        }
        System.out.println("VerdictClosurePolicySmokeTest: 21/21 passed");
    }

    private static String longDo() {
        return "ИТОГ: ДЕЛАТЬ. Продукт выглядит убедительно, команда готова, и откладывать запуск нет причины. "
                + "Рекомендация окончательная.";
    }

    private static String closedWell() {
        return "ИТОГ: ДЕЛАТЬ. Шаг обратим: откатить цену можно за неделю. "
                + "Наблюдение, которое перевернёт вывод: если пилот покажет конверсию ниже 4%.";
    }

    private static boolean quiet(String task, String a, String b) {
        List<String> out = new ArrayList<String>();
        VerdictClosurePolicy.detect(task, a, b, out);
        return out.isEmpty();
    }

    private static boolean fires(String task, String a, String b) {
        List<String> out = new ArrayList<String>();
        VerdictClosurePolicy.detect(task, a, b, out);
        if (out.isEmpty()) return false;
        for (String signal : out) {
            if (signal.contains("verdict closure") && signal.contains("possible signal")) return true;
        }
        return false;
    }

    private static boolean both(String task, String a, String b) {
        List<String> out = new ArrayList<String>();
        VerdictClosurePolicy.detect(task, a, b, out);
        boolean single = false;
        boolean dual = false;
        for (String signal : out) {
            if (signal.contains("both experts")) dual = true;
            if (signal.contains("verdict closure")) single = true;
        }
        return single && dual;
    }

    private static boolean forecastFires(String verdict) {
        return !VerdictClosurePolicy.gaps(FORECAST, verdict).isEmpty();
    }

    private static boolean forecastQuiet(String verdict) {
        return VerdictClosurePolicy.gaps(FORECAST, verdict).isEmpty();
    }

    private static String footer(String task, String result) {
        return VerdictClosurePolicy.footer(task, result);
    }

    private static boolean footerUsesCoordinator() {
        String report = "✦ ИИ №1\n\n" + longDo()
                + "\n\n🏁 ИТОГ КООРДИНАТОРА\n\n" + closedWell();
        return VerdictClosurePolicy.footer(DECISION, report).isEmpty();
    }

    private static boolean cueMatches() {
        List<String> conflicts = new ArrayList<String>();
        VerdictClosurePolicy.detect(DECISION, longDo(), "", conflicts);
        StringBuilder sb = new StringBuilder();
        CoordinatorCue.append(sb, conflicts);
        String text = sb.toString();
        return text.contains("СНАЧАЛА ПРОВЕРИТЬ") && (text.contains("обратим") || text.contains("метк"));
    }

    private static int check(String name, boolean ok) {
        if (ok) return 0;
        System.err.println("FAIL: " + name);
        return 1;
    }
}
