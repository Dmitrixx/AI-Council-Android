package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Extra offline reasoning-risk signals that the coordinator must not average away.
 * Each note is a hypothesis, not a verdict. Expanded in v2.40–v2.47.
 */
public final class DecisionBriefPolicy {
    private static final int MAX_SIGNALS = 8;

    private DecisionBriefPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, sunkCost(a) || sunkCost(b),
                "невозвратные затраты: уже потраченное используется как причина продолжать");
        add(conflicts, initial, survivorship(a) || survivorship(b),
                "выжившая выборка: вывод строится по успешным случаям без тех, кто не попал в рассказ");
        add(conflicts, initial, appealToNovelty(a) || appealToNovelty(b),
                "апелляция к новизне: «новое» само по себе принимается за «лучше»");
        add(conflicts, initial, appealToConsequences(a) || appealToConsequences(b),
                "апелляция к последствиям: нежелательный итог используется как доказательство ложности тезиса");
        add(conflicts, initial, cherryPick(a) || cherryPick(b),
                "выборочные данные: неудобные наблюдения предлагается отбросить до разбора");
        add(conflicts, initial, baseRateNeglect(a) || baseRateNeglect(b),
                "базовая частота: яркий единичный случай подменяет долю в похожем классе");
        add(conflicts, initial, nirvana(a) || nirvana(b),
                "идеальное решение: неидеальный вариант отвергается только потому, что он не идеален");
        add(conflicts, initial, anchoring(a) || anchoring(b),
                "якорение: первое число/пример непропорционально задаёт итоговую оценку без пересмотра");
        add(conflicts, initial, planningFallacy(a) || planningFallacy(b),
                "ошибка планирования: сроки/стоимость даны точечно без reference class и без буфера на срыв");
        add(conflicts, initial, availability(a) || availability(b),
                "доступность: яркий недавний пример подменяет частоту в reference class");
        add(conflicts, initial, confirmation(a) || confirmation(b),
                "подтверждающий уклон: ищутся только данные «за», контрпримеры не рассматриваются");
        add(conflicts, initial, motivatedReasoning(a) || motivatedReasoning(b),
                "мотивированное рассуждение: желаемый исход принимается за более вероятный без отдельной проверки");
        add(conflicts, initial, statusQuo(a) || statusQuo(b),
                "статус-кво: сохранение текущего положения предлагается без сравнения выгод и издержек изменения");
        add(conflicts, initial, zeroRisk(a) || zeroRisk(b),
                "нулевой риск: полное устранение одного малого риска предпочитается большему снижению крупного");
        add(conflicts, initial, scopeNeglect(a) || scopeNeglect(b),
                "пренебрежение масштабом: влияние на 10 и на 10 000 трактуется почти одинаково");
        add(conflicts, initial, opportunityCost(a) || opportunityCost(b),
                "упущенная альтернатива: вариант выбирается без цены отказа от следующего лучшего");
        add(conflicts, initial, affirmingConsequent(a) || affirmingConsequent(b),
                "утверждение консеквента: наличие следствия принимается за доказательство именно этой причины");
        add(conflicts, initial, denyingAntecedent(a) || denyingAntecedent(b),
                "отрицание антецедента: отсутствие одного достаточного условия принимается за отсутствие следствия");
        add(conflicts, initial, outcomeBias(a) || outcomeBias(b),
                "ошибка исхода: качество решения оценивается по уже известному итогу, а не по информации на момент выбора");
        add(conflicts, initial, presentBias(a) || presentBias(b),
                "смещение к настоящему: близкая выгода берётся без сравнения более поздней цены");
        add(conflicts, initial, omissionBias(a) || omissionBias(b),
                "смещение бездействия: отказ действовать предпочитается при том же ожидаемом вреде");
    }

    private static void add(List<String> out, int initial, boolean condition, String message) {
        String formatted = "карта решения — возможный сигнал, требуется проверка контекста: " + message;
        if (!condition || out.size() - initial >= MAX_SIGNALS || out.contains(formatted)) return;
        out.add(formatted);
    }

    private static boolean sunkCost(String text) {
        return has(text, "уже вложили", "уже потратили", "жалко бросать", "sunk cost", "already invested", "already spent",
                "вже вклали", "вже витратили")
                && has(text, "поэтому продолж", "нужно продолжать", "нельзя останавливаться", "should continue",
                "can't stop now", "cannot stop now", "тому продовж");
    }

    private static boolean survivorship(String text) {
        return has(text, "только успешн", "только победител", "only the winners", "only successful", "выжившие компан",
                "лише успішн")
                && has(text, "значит", "поэтому", "therefore", "so we should", "тому")
                && !has(text, "неудач", "failed", "кто не", "who didn't", "who did not", "невдач");
    }

    private static boolean appealToNovelty(String text) {
        return has(text, "потому что это новое", "потому что новее", "because it is new", "because it's newer",
                "because it is newer", "тому що нове")
                && has(text, "лучше", "better", "краще");
    }

    private static boolean appealToConsequences(String text) {
        return has(text, "не может быть правдой, потому что", "can't be true because", "cannot be true because",
                "неверно, потому что тогда", "undesirable, so it is false", "too terrible to be true",
                "не може бути правдою, бо");
    }

    private static boolean cherryPick(String text) {
        return has(text, "отбросим противореча", "ignore the studies that disagree", "только подтверждающие данные",
                "only the favorable data", "не будем смотреть на противореча", "відкинемо супереч");
    }

    private static boolean baseRateNeglect(String text) {
        return has(text, "этот случай", "этот человек", "this one case", "моя история", "my story", "цей випадок")
                && has(text, "вероятност", "шанс", "probability", "the chance", "ймовірн")
                && !has(text, "базов", "base rate", "reference class", "среди таких", "базова частота");
    }

    private static boolean nirvana(String text) {
        return has(text, "если не идеально, то не стоит", "unless it is perfect", "не идеально, значит бесполезно",
                "not perfect so worthless", "если решение не идеально, оно бесполезно", "якщо не ідеально");
    }

    private static boolean anchoring(String text) {
        return has(text, "исходя из первой оценки", "начальная цифра", "starting number", "anchor of",
                "оттолкнёмся от", "від першої оцінки") && has(text, "поэтому итог", "therefore the estimate",
                "финальная оценка около", "тому підсумок")
                && !has(text, "пересмотрели", "reference class", "outside view", "пересчитали независимо", "переглянули");
    }

    private static boolean planningFallacy(String text) {
        return has(text, "займёт ровно", "точно за", "will take exactly", "deadline is firm", "срок — ровно",
                "займе рівно")
                && has(text, "дней", "недель", "месяц", "hours", "days", "weeks", "днів", "тижнів")
                && !has(text, "буфер", "reference class", "исторически такие", "outside view", "диапазон сроков",
                "історично такі");
    }

    private static boolean availability(String text) {
        return has(text, "недавно все говорят", "в новостях", "я только что видел", "recent news", "just saw a case",
                "нещодавно всі кажуть")
                && has(text, "значит часто", "therefore common", "типично", "тому часто")
                && !has(text, "статистика", "base rate", "за период", "incidence", "за період");
    }

    private static boolean confirmation(String text) {
        return has(text, "ищем подтверждения", "только данные за", "look only for supporting", "confirming evidence only",
                "шукаємо підтвердження")
                && has(text, "игнорируем противореча", "skip disconfirming", "не смотрим на опровержения",
                "ігноруємо супереч");
    }

    private static boolean motivatedReasoning(String text) {
        return has(text, "хочу чтобы", "хотелось бы чтобы", "i want this to be true", "wish it were true",
                "желаемый исход", "хочеться щоб")
                && has(text, "поэтому вероятно", "значит скорее всего", "so it is likely", "therefore probable",
                "тому ймовірно")
                && !has(text, "независимо от желания", "regardless of preference", "outside view", "reference class",
                "незалежно від бажання");
    }

    private static boolean statusQuo(String text) {
        return has(text, "оставьте как есть", "не менять ничего", "keep the status quo", "leave it as is",
                "лучше не трогать", "залиште як є")
                && has(text, "потому что так привычно", "because it already works", "уже работает", "без изменений лучше",
                "тому що так звично")
                && !has(text, "сравнили альтернатив", "compared options", "cost of change", "выгода изменения",
                "порівняли альтернатив");
    }

    private static boolean zeroRisk(String text) {
        return has(text, "полностью устранить риск", "свести риск к нулю", "eliminate the risk entirely",
                "zero risk", "повністю усунути ризик")
                && has(text, "важнее чем снизить большой", "prefer over reducing the larger", "даже если крупный останется",
                "важливіше ніж знизити великий")
                && !has(text, "expected harm", "ожидаемый ущерб", "сравниваем абсолютный риск", "absolute risk reduction");
    }

    private static boolean scopeNeglect(String text) {
        return has(text, "одинаково важно", "примерно одно и то же", "roughly the same impact",
                "не важно сколько", "scale does not matter", "однаково важливо")
                && has(text, "10 или 10000", "десять или десять тысяч", "10 vs 10000", "few or many",
                "10 чи 10000")
                && !has(text, "пропорционально", "per person", "на человека", "масштабируется", "scales with");
    }

    private static boolean opportunityCost(String text) {
        return has(text, "не считая от чего отказываемся", "without pricing what we give up",
                "ігноруючи від чого відмовляємось", "ignoring the opportunity")
                && has(text, "поэтому выбираем", "so we choose", "тому обираємо", "therefore we pick")
                && !has(text, "упущенн", "opportunity cost", "next-best", "альтернативн");
    }

    private static boolean affirmingConsequent(String text) {
        return has(text, "если бы это было причиной", "if it were the cause", "якби це було причиною")
                && has(text, "эффект есть, значит это и есть причина", "we see the effect, so it is the cause",
                "наслідок є, отже це і є причина")
                && !has(text, "другие причины", "other causes", "не единственн", "not the only", "інші причини");
    }

    private static boolean denyingAntecedent(String text) {
        return has(text, "раз условия нет, вывода тоже нет", "antecedent is false so the consequent is false",
                "раз умови немає, то й висновку немає")
                && !has(text, "необходим", "necessary", "необхідн", "бывает и без", "буває і без");
    }

    private static boolean outcomeBias(String text) {
        return has(text, "решение было плохим, потому что итог плохой",
                "the decision was bad because the outcome was bad",
                "рішення було поганим, бо підсумок поганий",
                "мы всегда это знали после итога", "we knew it all along after the outcome")
                && !has(text, "на тот момент", "at the time", "information available", "інформація на той момент");
    }

    private static boolean presentBias(String text) {
        return has(text, "возьмём сейчас", "take the reward now", "візьмемо зараз")
                && has(text, "даже если потом дороже", "even if it costs more later", "навіть якщо потім дорожче")
                && !has(text, "дисконт", "discount rate", "приведённ", "net present", "приведен");
    }

    private static boolean omissionBias(String text) {
        return has(text, "бездействие лучше действия", "inaction is safer than action", "бездіяльність краща за дію")
                && has(text, "при том же вреде", "same expected harm", "при тій самій шкоді")
                && !has(text, "ожидаемый вред меньше", "expected harm is lower", "очікувана шкода менша");
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
