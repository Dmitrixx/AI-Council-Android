package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Ranks the open decision-frame gaps and names the single observation
 * with the most leverage. A list of caveats is not a verdict.
 * Naming the gap is not itself an error. The coordinator cannot close
 * while this observation is still missing.
 */
public final class NextObservationPolicy {
    private static final int MAX_SIGNALS = 2;

    private NextObservationPolicy() {}

    public static String block(String task) {
        if (task == null || task.trim().length() < 12) return "";
        List<DecisionFramePolicy.Gap> ranked = rank(task);
        StringBuilder sb = new StringBuilder();
        sb.append("СЛЕДУЮЩЕЕ НАБЛЮДЕНИЕ (одно, с наибольшим рычагом; не список всех дыр):\n");
        if (ranked.isEmpty()) {
            sb.append("Явных дыр кадра нет. Всё равно назови одно наблюдение, которое перевернёт вывод. Без него метка остаётся гипотезой.\n");
            return sb.toString().trim();
        }
        DecisionFramePolicy.Gap top = ranked.get(0);
        sb.append("Сначала узнай: ").append(ask(top.key)).append("\n");
        sb.append("Почему это, а не остальное: ").append(why(top.key)).append("\n");
        if (ranked.size() > 1) {
            sb.append("Отложено, пока не закрыто первое: ").append(ranked.get(1).text);
            if (ranked.size() > 2) sb.append("; ").append(ranked.get(2).text);
            sb.append(".\n");
        }
        sb.append("Правило: не закрывай выбор списком оговорок. Либо названо это наблюдение, либо явно «неизвестно», и тогда метка — гипотеза.\n");
        return sb.toString().trim();
    }

    public static void detect(String task, String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        if (task == null || task.trim().length() < 40) return;
        List<DecisionFramePolicy.Gap> ranked = rank(task);
        if (ranked.isEmpty()) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        DecisionFramePolicy.Gap top = ranked.get(0);
        boolean aCloses = closes(a) && !acknowledges(a, top.key);
        boolean bCloses = closes(b) && !acknowledges(b, top.key);
        add(conflicts, initial, aCloses || bCloses,
                "next observation: вердикт закрывает выбор, не назвав наблюдение с наибольшим рычагом (" + top.text + ")");
        add(conflicts, initial, aCloses && bCloses,
                "next observation: оба эксперта закрывают выбор списком оговорок, без одного решающего наблюдения");
    }

    private static List<DecisionFramePolicy.Gap> rank(String task) {
        List<DecisionFramePolicy.Gap> ranked = new ArrayList<DecisionFramePolicy.Gap>(DecisionFramePolicy.gaps(task));
        final String frozen = task;
        ranked.sort((left, right) -> Integer.compare(leverage(right.key, frozen), leverage(left.key, frozen)));
        return ranked;
    }

    static int leverage(String key, String task) {
        SynthesisProtocol.Kind kind = SynthesisProtocol.classify(task);
        boolean forecast = kind == SynthesisProtocol.Kind.FORECAST;
        boolean ruinous = ruin(task);
        if ("options".equals(key)) return 100;
        if ("errorcost".equals(key)) return 92;
        if ("downside".equals(key)) return ruinous ? 98 : 74;
        if ("base".equals(key)) return forecast ? 96 : 68;
        if ("horizon".equals(key)) return 58;
        if ("scale".equals(key)) return 54;
        if ("opportunity".equals(key)) return 48;
        if ("premortem".equals(key)) return 44;
        if ("metric".equals(key)) return 36;
        return 10;
    }

    private static String ask(String key) {
        if ("options".equals(key)) return "второй живой вариант, включая «оставить как есть», и чем он хуже.";
        if ("scale".equals(key)) return "величину ставки — деньги, время или долю.";
        if ("horizon".equals(key)) return "срок, после которого рекомендация перестаёт быть верной.";
        if ("downside".equals(key)) return "чей ресурс сгорает, если рекомендация ошибочна.";
        if ("base".equals(key)) return "долю похожих случаев, где выбранный путь сработал, или прямое «её нет».";
        if ("metric".equals(key)) return "измерение и порог, после которого рекомендацию следует отменить.";
        if ("errorcost".equals(key)) return "что дороже — лишнее действие или упущенный шанс — и хотя бы порядок этой разницы.";
        if ("opportunity".equals(key)) return "от чего отказываются при этом выборе и чем следующий лучший вариант хуже.";
        if ("premortem".equals(key)) return "одну правдоподобную причину провала через год и критерий остановки сейчас.";
        return "одно наблюдение, которое перевернёт вывод.";
    }

    private static String why(String key) {
        if ("options".equals(key)) return "без второго варианта это не выбор.";
        if ("errorcost".equals(key)) return "пока не ясно, какая ошибка дороже, ожидаемая польза не говорит, что делать.";
        if ("downside".equals(key)) return "чужой риск легко советовать.";
        if ("base".equals(key)) return "яркий сценарий без частоты — только внутренний взгляд.";
        if ("horizon".equals(key)) return "верный на неделю совет может быть плохим на год.";
        if ("scale".equals(key)) return "один процент значит разное на разных ставках.";
        if ("opportunity".equals(key)) return "цена выбора — это отказ, а не только получение.";
        if ("premortem".equals(key)) return "критерий остановки заранее дешевле надежды разобраться потом.";
        return "это наблюдение сильнее остальных меняет сам ответ.";
    }

    private static boolean ruin(String task) {
        String text = lower(task);
        return text.contains("плеч") || text.contains("leverage") || text.contains("all-in")
                || text.contains("разорит") || text.contains("весь капитал") || text.contains("необратим");
    }

    private static boolean closes(String text) {
        return has(text,
                "поэтому делаем", "поэтому выбираем", "следует запустить",
                "рекомендую делать", "однозначно стоит", "закрываем выбор",
                "we should proceed", "therefore we should", "the choice is clear");
    }

    private static boolean acknowledges(String text, String key) {
        if (has(text, "неизвестно", "сначала проверить", "следующее наблюдение", "гипотеза")) return true;
        if ("options".equals(key)) return has(text, "альтернатив", "оставить как есть", "status quo");
        if ("scale".equals(key)) return has(text, "масштаб", "величина неизвест", "ставка не назван");
        if ("horizon".equals(key)) return has(text, "горизонт", "срок не задан");
        if ("downside".equals(key)) return has(text, "кто несёт", "кто несет", "downside");
        if ("base".equals(key)) return has(text, "базовая частота", "base rate", "похожих случа");
        if ("metric".equals(key)) return has(text, "критерий успеха", "success metric");
        if ("errorcost".equals(key)) return has(text, "какая ошибка дороже", "асимметрия ошибки", "false positive", "false negative", "error-cost");
        if ("opportunity".equals(key)) return has(text, "альтернативная стоимость", "opportunity cost", "от чего отказыва");
        if ("premortem".equals(key)) return has(text, "премортем", "критерий отмены", "kill criterion");
        return false;
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "next observation (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean has(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }
}
