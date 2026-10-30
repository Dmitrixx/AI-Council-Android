package com.aicouncil.app;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local arithmetic for an explicit decision question, before any model runs.
 * Missing labels stay silent. The note is a calculation, not a recommendation
 * and not a claim about units or the real world.
 */
public final class DecisionArithmeticPolicy {
    private static final Pattern PERCENT = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*%");
    private static final Pattern MONEY = Pattern.compile("(-?\\d+(?:[.,]\\d+)?)");

    private DecisionArithmeticPolicy() {}

    public static String evaluateExplicitRequest(String task) {
        if (task == null || task.trim().isEmpty()) return "";
        StringBuilder body = new StringBuilder();
        String information = valueOfInformation(task);
        append(body, information);
        if (information.isEmpty()) append(body, expectedValue(task));
        append(body, breakEven(task));
        append(body, kelly(task));
        if (body.length() == 0) return "";
        return "ДЕТЕРМИНИРОВАННАЯ АРИФМЕТИКА РЕШЕНИЯ (не ответ ИИ):\n"
                + body.toString().trim()
                + "\nМетод: расчёт в приложении. Это не рекомендация действовать. Если подписи чисел означают другое, результат не применять и сказать об этом явно.";
    }

    private static String valueOfInformation(String task) {
        if (!wants(task,
                "ценность информации", "ценность идеальной информации",
                "value of information", "value of perfect information", "evpi",
                "цінність інформації", "цінність ідеальної інформації")) {
            return "";
        }
        Payoff payoff = payoff(task);
        if (payoff == null) return "";
        double evPerfect = round2(payoff.p * Math.max(payoff.win, payoff.wait)
                + (1.0 - payoff.p) * Math.max(-payoff.loss, payoff.wait));
        double evpi = round2(evPerfect - payoff.evBest);
        if (evpi < 0) evpi = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("Ценность идеальной информации:\n");
        appendPayoff(sb, payoff);
        sb.append("EVPI: ").append(num(evpi)).append('\n');
        if (payoff.cost != null && payoff.cost >= 0) {
            if (payoff.cost + 1e-9 < evpi) {
                sb.append("Заявленная цена проверки ").append(num(payoff.cost))
                        .append(" ниже EVPI. Это потолок только для безошибочного теста, а не доказательство, что неполный тест окупается.\n");
            } else {
                sb.append("Заявленная цена проверки ").append(num(payoff.cost))
                        .append(" не ниже EVPI ").append(num(evpi))
                        .append(": даже идеальная информация не окупает эту цену.\n");
            }
        }
        return sb.toString().trim();
    }

    private static String expectedValue(String task) {
        if (!wants(task, "ожидаемая ценность", "ожидаемое значение", "expected value", "очікувана цінність")) {
            return "";
        }
        Payoff payoff = payoff(task);
        if (payoff == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("Ожидаемая ценность:\n");
        appendPayoff(sb, payoff);
        sb.append(choiceLine(payoff));
        return sb.toString().trim();
    }

    private static String breakEven(String task) {
        if (!wants(task,
                "точка безубыточности", "безубыточность", "break-even", "break even",
                "точка беззбитковості", "беззбитковість")) {
            return "";
        }
        Double fixed = moneyNear(task, "постоянные затраты", "постоянные расходы", "fixed costs", "fixed cost", "фіксовані витрати");
        Double price = moneyNear(task, "цена продажи", "цена единицы", "продажная цена", "price", "ціна", "цена");
        Double variable = moneyNear(task, "переменные затраты", "переменные расходы", "variable cost", "змінні витрати");
        if (fixed == null || price == null || variable == null) return "";
        if (fixed < 0 || price < 0 || variable < 0) return "";
        double contribution = round2(price - variable);
        if (!(contribution > 0)) {
            return "Точка безубыточности не существует: вклад на единицу не положительный (цена "
                    + num(price) + ", переменные " + num(variable) + ").";
        }
        double units = round2(fixed / contribution);
        if (!Double.isFinite(units) || units < 0 || units > 1.0e12) return "";
        return "Точка безубыточности: " + num(units) + " единиц при постоянных " + num(fixed)
                + ", цене " + num(price) + " и переменных " + num(variable) + ".\n"
                + "Вклад на единицу: " + num(contribution) + ". Единицы и валюта не выводятся из текста.";
    }

    private static String kelly(String task) {
        if (!wants(task,
                "критерий келли", "критерий келлі", "критерій келлі", "критерій келли",
                "kelly criterion", "kelly fraction", "доля келли", "формула келли")) {
            return "";
        }
        Double p = percentNear(task,
                "вероятность выигрыша", "вероятность победы", "win probability",
                "ймовірність виграшу", "шанс выигрыша");
        if (p == null) return "";
        Double net = moneyNear(task, "чистый коэффициент", "чистий коефіцієнт", "net odds");
        Double decimal = moneyNear(task, "десятичный коэффициент", "десятичний коефіцієнт", "decimal odds", "коэффициент", "коефіцієнт");
        double b;
        String oddsLine;
        if (net != null) {
            if (!(net > 0) || net > 1.0e6) return "";
            b = net;
            oddsLine = "Чистый коэффициент b: " + num(b);
        } else if (decimal != null) {
            if (!(decimal > 1.0) || decimal > 1.0e6) {
                return "Десятичный коэффициент " + num(decimal)
                        + " не даёт положительного чистого коэффициента. Доля Келли не считается.";
            }
            b = round2(decimal - 1.0);
            oddsLine = "Десятичный коэффициент: " + num(decimal) + ", чистый b: " + num(b);
        } else {
            return "";
        }
        if (!(b > 0)) return "";
        double f = round2(p - (1.0 - p) / b);
        if (!Double.isFinite(f)) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("Критерий Келли:\n");
        sb.append("Вероятность выигрыша: ").append(pct(p)).append('\n');
        sb.append(oddsLine).append('\n');
        if (f <= 0) {
            sb.append("Полная доля Келли не положительна (").append(num(f * 100.0))
                    .append("% банка): при этих числах ставка не имеет положительного ожидания.");
        } else {
            sb.append("Полная доля Келли: ").append(num(f * 100.0)).append("% банка\n");
            sb.append("Половина Келли, справочно: ").append(num(f * 50.0)).append("% банка\n");
            if (f > 1.0) {
                sb.append("Расчёт больше 100% банка. Это не разрешение занимать или усреднять плечо.");
            } else {
                sb.append("Полная доля агрессивна и не является размером ставки.");
            }
        }
        return sb.toString().trim();
    }

    private static void appendPayoff(StringBuilder sb, Payoff payoff) {
        sb.append("Вероятность успеха: ").append(pct(payoff.p)).append('\n');
        sb.append("Ожидаемая ценность действия: ").append(num(payoff.evAct)).append('\n');
        sb.append("Ожидаемая ценность бездействия: ").append(num(payoff.wait)).append('\n');
        sb.append(choiceLine(payoff));
    }

    private static String choiceLine(Payoff payoff) {
        if (Math.abs(payoff.evAct - payoff.wait) < 0.005) {
            return "Оба варианта имеют одну ожидаемую ценность " + num(payoff.evAct) + ".\n";
        }
        if (payoff.evAct > payoff.wait) {
            return "Большее среднее у действия (" + num(payoff.evAct) + "), не у бездействия.\n";
        }
        return "Большее среднее у бездействия (" + num(payoff.wait) + "), не у действия.\n";
    }

    private static Payoff payoff(String task) {
        Double p = percentNear(task,
                "вероятность успеха", "вероятность благоприят", "probability of success",
                "ймовірність успіху", "шанс успеха", "шанс успіху");
        Double win = moneyNear(task, "выигрыш", "прибыль при успехе", "gain", "виграш");
        Double lossRaw = moneyNear(task, "потеря", "убыток", "loss", "втрата", "збиток");
        if (p == null || win == null || lossRaw == null) return null;
        if (!(win > 0) || Math.abs(lossRaw) > 1.0e12) return null;
        double loss = Math.abs(lossRaw);
        Double waitRaw = moneyNear(task,
                "если не делать", "если не действовать", "бездействие",
                "do nothing", "якщо не робити");
        double wait = waitRaw == null ? 0.0 : waitRaw;
        Double cost = moneyNear(task,
                "цена проверки", "стоимость проверки", "стоимость теста",
                "test cost", "вартість перевірки", "ціна перевірки");
        if (cost != null && cost < 0) cost = null;
        double evAct = round2(p * win - (1.0 - p) * loss);
        double evBest = round2(Math.max(evAct, wait));
        return new Payoff(p, win, loss, wait, cost, evAct, evBest);
    }

    private static boolean wants(String task, String... cues) {
        String lower = task.toLowerCase(Locale.ROOT);
        for (int i = 0; i < cues.length; i++) {
            if (lower.contains(cues[i])) return true;
        }
        return false;
    }

    private static void append(StringBuilder body, String section) {
        if (section == null || section.isEmpty()) return;
        if (body.length() > 0) body.append('\n');
        body.append(section).append('\n');
    }

    private static double round2(double value) {
        if (!Double.isFinite(value)) return Double.NaN;
        return Math.round(value * 100.0) / 100.0;
    }

    private static String num(double value) {
        double rounded = round2(value);
        if (!Double.isFinite(rounded)) return "";
        if (Math.abs(rounded - Math.rint(rounded)) < 1.0e-9 && Math.abs(rounded) < 1.0e12) {
            return String.format(Locale.ROOT, "%.0f", rounded);
        }
        return String.format(Locale.ROOT, "%.2f", rounded);
    }

    private static String pct(double unitProbability) {
        return num(unitProbability * 100.0) + "%";
    }

    private static Double percentNear(String text, String... cues) {
        String lower = text.toLowerCase(Locale.ROOT);
        for (int i = 0; i < cues.length; i++) {
            int at = lower.indexOf(cues[i]);
            if (at < 0) continue;
            Double forward = closestPercent(text, at + cues[i].length(), Math.min(text.length(), at + cues[i].length() + 22));
            if (forward != null) return forward;
        }
        return null;
    }

    private static Double closestPercent(String text, int from, int to) {
        if (to <= from) return null;
        String slice = cutClause(text.substring(from, to));
        Matcher matcher = PERCENT.matcher(slice);
        if (!matcher.find()) return null;
        double raw = Double.parseDouble(matcher.group(1).replace(',', '.'));
        if (!Double.isFinite(raw) || raw < 0.0 || raw > 100.0) return null;
        return raw / 100.0;
    }

    private static Double moneyNear(String text, String... cues) {
        String lower = text.toLowerCase(Locale.ROOT);
        for (int i = 0; i < cues.length; i++) {
            int at = lower.indexOf(cues[i]);
            if (at < 0) continue;
            Double forward = closestMoney(text, at + cues[i].length(), Math.min(text.length(), at + cues[i].length() + 24));
            if (forward != null) return forward;
        }
        return null;
    }

    private static Double closestMoney(String text, int from, int to) {
        if (to <= from) return null;
        String slice = cutClause(text.substring(from, to));
        Matcher matcher = MONEY.matcher(slice);
        while (matcher.find()) {
            String tail = slice.substring(matcher.end(), Math.min(slice.length(), matcher.end() + 2)).trim();
            if (tail.startsWith("%")) continue;
            double raw = Double.parseDouble(matcher.group(1).replace(',', '.'));
            if (!Double.isFinite(raw) || Math.abs(raw) > 1.0e12) return null;
            return raw;
        }
        return null;
    }

    private static String cutClause(String slice) {
        for (int i = 0; i < slice.length(); i++) {
            char ch = slice.charAt(i);
            if (ch == '.') {
                char prev = i > 0 ? slice.charAt(i - 1) : ' ';
                char next = i + 1 < slice.length() ? slice.charAt(i + 1) : ' ';
                if (prev >= '0' && prev <= '9' && next >= '0' && next <= '9') continue;
                return slice.substring(0, i);
            }
            if (ch != ',') continue;
            char next = i + 1 < slice.length() ? slice.charAt(i + 1) : ' ';
            if (next >= '0' && next <= '9') continue;
            return slice.substring(0, i);
        }
        return slice;
    }

    private static final class Payoff {
        final double p;
        final double win;
        final double loss;
        final double wait;
        final Double cost;
        final double evAct;
        final double evBest;

        Payoff(double p, double win, double loss, double wait, Double cost, double evAct, double evBest) {
            this.p = p;
            this.win = win;
            this.loss = loss;
            this.wait = wait;
            this.cost = cost;
            this.evAct = evAct;
            this.evBest = evBest;
        }
    }
}
