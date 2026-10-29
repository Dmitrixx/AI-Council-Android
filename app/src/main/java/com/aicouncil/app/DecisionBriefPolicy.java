package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Extra offline reasoning-risk signals that the coordinator must not average away.
 * Each note is a hypothesis, not a verdict. Expanded in v2.40–v2.42.
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
    }

    private static void add(List<String> out, int initial, boolean condition, String message) {
        String formatted = "карта решения — возможный сигнал, требуется проверка контекста: " + message;
        if (!condition || out.size() - initial >= MAX_SIGNALS || out.contains(formatted)) return;
        out.add(formatted);
    }

    private static boolean sunkCost(String text) {
        return has(text, "уже вложили", "уже потратили", "жалко бросать", "sunk cost", "already invested", "already spent")
                && has(text, "поэтому продолж", "нужно продолжать", "нельзя останавливаться", "should continue", "can't stop now", "cannot stop now");
    }

    private static boolean survivorship(String text) {
        return has(text, "только успешн", "только победител", "only the winners", "only successful", "выжившие компан")
                && has(text, "значит", "поэтому", "therefore", "so we should")
                && !has(text, "неудач", "failed", "кто не", "who didn't", "who did not");
    }

    private static boolean appealToNovelty(String text) {
        return has(text, "потому что это новое", "потому что новее", "because it is new", "because it's newer", "because it is newer")
                && has(text, "лучше", "better");
    }

    private static boolean appealToConsequences(String text) {
        return has(text, "не может быть правдой, потому что", "can't be true because", "cannot be true because",
                "неверно, потому что тогда", "undesirable, so it is false", "too terrible to be true");
    }

    private static boolean cherryPick(String text) {
        return has(text, "отбросим противореча", "ignore the studies that disagree", "только подтверждающие данные",
                "only the favorable data", "не будем смотреть на противореча");
    }

    private static boolean baseRateNeglect(String text) {
        return has(text, "этот случай", "этот человек", "this one case", "моя история", "my story")
                && has(text, "вероятност", "шанс", "probability", "the chance")
                && !has(text, "базов", "base rate", "reference class", "среди таких");
    }

    private static boolean nirvana(String text) {
        return has(text, "если не идеально, то не стоит", "unless it is perfect", "не идеально, значит бесполезно",
                "not perfect so worthless", "если решение не идеально, оно бесполезно");
    }

    private static boolean anchoring(String text) {
        return has(text, "исходя из первой оценки", "начальная цифра", "starting number", "anchor of",
                "оттолкнёмся от") && has(text, "поэтому итог", "therefore the estimate", "финальная оценка около")
                && !has(text, "пересмотрели", "reference class", "outside view", "пересчитали независимо");
    }

    private static boolean planningFallacy(String text) {
        return has(text, "займёт ровно", "точно за", "will take exactly", "deadline is firm", "срок — ровно")
                && has(text, "дней", "недель", "месяц", "hours", "days", "weeks")
                && !has(text, "буфер", "reference class", "исторически такие", "outside view", "диапазон сроков");
    }

    private static boolean availability(String text) {
        return has(text, "недавно все говорят", "в новостях", "я только что видел", "recent news", "just saw a case")
                && has(text, "значит часто", "therefore common", "типично")
                && !has(text, "статистика", "base rate", "за период", "incidence");
    }

    private static boolean confirmation(String text) {
        return has(text, "ищем подтверждения", "только данные за", "look only for supporting", "confirming evidence only")
                && has(text, "игнорируем противореча", "skip disconfirming", "не смотрим на опровержения");
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
