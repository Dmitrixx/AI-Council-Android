package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local decision-map signals: common judgment traps that appear in recommendations.
 * Each hit is a hypothesis that needs context, not a verdict.
 * Coordinator must not average these away.
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
                "невозвратные затраты: прошлые вложения используются как причина продолжать");
        add(conflicts, initial, survivorship(a) || survivorship(b),
                "выжившая выборка: только успешные случаи видны, неудачи отброшены");
        add(conflicts, initial, novelty(a) || novelty(b),
                "новизна: «новое» принимается за «лучше» без сравнения");
        add(conflicts, initial, consequences(a) || consequences(b),
                "аргумент от последствий: тяжёлые последствия объявлены опровержением тезиса");
        add(conflicts, initial, cherryPick(a) || cherryPick(b),
                "cherry-pick: оставляются только подтверждающие данные, противоречащие отбрасываются");
        add(conflicts, initial, baseRateNeglect(a) || baseRateNeglect(b),
                "пренебрежение базовой частотой: яркий случай поднимает вероятность без reference class");
        add(conflicts, initial, nirvana(a) || nirvana(b),
                "nirvana: неидеальный вариант объявлен бесполезным вместо сравнения с доступными");
        add(conflicts, initial, motivatedReasoning(a) || motivatedReasoning(b),
                "мотивированное рассуждение: желание, чтобы тезис был верен, выдаётся за вероятность");
        add(conflicts, initial, statusQuo(a) || statusQuo(b),
                "status quo bias: «оставить как есть» без сравнения альтернатив и cost of change");
        add(conflicts, initial, zeroRisk(a) || zeroRisk(b),
                "zero-risk bias: полное устранение малого риска важнее снижения крупного");
        add(conflicts, initial, scopeNeglect(a) || scopeNeglect(b),
                "scope neglect: масштаб (10 vs 10 000) объявлен несущественным");
        add(conflicts, initial, anchoring(a) || anchoring(b),
                "якорение: начальная цифра удерживает итог рядом без outside view");
        add(conflicts, initial, planningFallacy(a) || planningFallacy(b),
                "planning fallacy: срок дан как точная точка без буфера и диапазона");
        add(conflicts, initial, availability(a) || availability(b),
                "availability: недавние рассказы принимаются за частоту без статистики");
        add(conflicts, initial, confirmation(a) || confirmation(b),
                "confirmation bias: ищутся только подтверждения, опровержения игнорируются");
        add(conflicts, initial, opportunityCost(a) || opportunityCost(b),
                "упущенная выгода: путь выбран без учёта next-best альтернативы");
        add(conflicts, initial, affirmingConsequent(a) || affirmingConsequent(b),
                "утверждение консеквента: эффект объявлен уникальной причиной");
        add(conflicts, initial, denyingAntecedent(a) || denyingAntecedent(b),
                "отрицание антецедента: отсутствие условия объявлено отсутствием вывода");
        add(conflicts, initial, outcomeBias(a) || outcomeBias(b),
                "outcome bias: плохой итог объявлен доказательством плохого решения");
        add(conflicts, initial, presentBias(a) || presentBias(b),
                "present bias: немедленная выгода выбрана без учёта discount rate");
        add(conflicts, initial, omissionBias(a) || omissionBias(b),
                "omission bias: бездействие объявлено лучше действия при том же вреде");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "карта решения (возможный сигнал, требует контекста): " + core;
        for (String existing : conflicts) {
            if (existing.contains(core)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static boolean multi(String text, String[] groupA, String[] groupB) {
        return has(text, groupA) && has(text, groupB);
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    // --- detectors ---

    private static boolean sunkCost(String t) {
        return multi(t,
                new String[]{"уже вложили", "already invested", "уже потратили", "вже вклали"},
                new String[]{"поэтому продолжаем", "therefore continue", "нельзя останавливаться",
                        "cannot stop", "тому продовжуємо"});
    }

    private static boolean survivorship(String t) {
        return multi(t,
                new String[]{"только успешные", "only successful", "тільки успішні"},
                new String[]{"значит это работает", "therefore it works", "тому це працює"});
    }

    private static boolean novelty(String t) {
        return multi(t,
                new String[]{"потому что это новое", "because it is new", "тому що нове"},
                new String[]{"значит лучше", "therefore better", "тому краще"});
    }

    private static boolean consequences(String t) {
        return multi(t,
                new String[]{"не может быть правдой", "cannot be true", "не може бути правдою"},
                new String[]{"потому что тогда будет плохо", "because then it would be bad",
                        "тому що тоді буде погано"});
    }

    private static boolean cherryPick(String t) {
        return multi(t,
                new String[]{"только подтверждающие", "only confirming", "тільки підтверджуючі"},
                new String[]{"противоречащие отбросим", "discard contradicting", "відкинемо суперечливі"});
    }

    private static boolean baseRateNeglect(String t) {
        return multi(t,
                new String[]{"этот случай яркий", "this case is vivid", "цей випадок яскравий"},
                new String[]{"значит вероятность высока", "therefore probability is high",
                        "тому ймовірність висока"});
    }

    private static boolean nirvana(String t) {
        return multi(t,
                new String[]{"не идеально", "not perfect", "неідеально"},
                new String[]{"бесполезно", "useless", "відкинути", "therefore reject"});
    }

    private static boolean motivatedReasoning(String t) {
        return multi(t,
                new String[]{"хочу чтобы это было правдой", "want it to be true", "хочу щоб це було правдою"},
                new String[]{"поэтому вероятно так", "therefore probably so", "тому ймовірно так"});
    }

    private static boolean statusQuo(String t) {
        return multi(t,
                new String[]{"оставьте как есть", "leave as is", "залишити як є"},
                new String[]{"потому что так привычно", "because it is familiar", "тому що звично"});
    }

    private static boolean zeroRisk(String t) {
        return multi(t,
                new String[]{"полностью устранить риск", "eliminate risk completely", "повністю усунути ризик"},
                new String[]{"важнее чем снизить большой", "more important than reducing large",
                        "важливіше ніж знизити великий"});
    }

    private static boolean scopeNeglect(String t) {
        return multi(t,
                new String[]{"одинаково важно", "equally important", "однаково важливо",
                        "10 или 10000", "10 or 10000"},
                new String[]{"scale does not matter", "масштаб не важен", "масштаб не важливий"});
    }

    private static boolean anchoring(String t) {
        return multi(t,
                new String[]{"оттолкнёмся от начальной цифры", "start from the first number",
                        "відштовхнемося від початкової"},
                new String[]{"итог около", "result near", "залишимось поруч"});
    }

    private static boolean planningFallacy(String t) {
        return multi(t,
                new String[]{"займёт ровно", "will take exactly", "займе рівно"},
                new String[]{"срок без запаса", "no buffer", "без буфера"});
    }

    private static boolean availability(String t) {
        return multi(t,
                new String[]{"недавно все говорят", "everyone is talking recently", "нещодавно всі говорять"},
                new String[]{"значит часто", "therefore frequent", "тому часто"});
    }

    private static boolean confirmation(String t) {
        return multi(t,
                new String[]{"ищем подтверждения", "search for confirmation", "шукаємо підтвердження"},
                new String[]{"игнорируем противоречащие", "ignore contradicting", "ігноруємо суперечливі"});
    }

    private static boolean opportunityCost(String t) {
        return multi(t,
                new String[]{"поэтому выбираем этот путь", "therefore choose this path",
                        "тому обираємо цей шлях"},
                new String[]{"не считая от чего отказываемся", "without counting what we forgo",
                        "не рахуючи від чого відмовляємося"});
    }

    private static boolean affirmingConsequent(String t) {
        return multi(t,
                new String[]{"если бы это было причиной", "if this were the cause",
                        "якби це була причина"},
                new String[]{"эффект есть, значит это и есть причина",
                        "effect is present, therefore this is the cause",
                        "ефект є, отже це і є причина"});
    }

    private static boolean denyingAntecedent(String t) {
        return multi(t,
                new String[]{"раз условия нет", "since the condition is absent",
                        "раз умови немає"},
                new String[]{"вывода тоже нет", "conclusion is also absent",
                        "висновку теж немає"});
    }

    private static boolean outcomeBias(String t) {
        return multi(t,
                new String[]{"решение было плохим", "decision was bad", "рішення було поганим"},
                new String[]{"потому что итог плохой", "because the outcome was bad",
                        "тому що підсумок поганий"});
    }

    private static boolean presentBias(String t) {
        return multi(t,
                new String[]{"возьмём сейчас", "take it now", "візьмемо зараз"},
                new String[]{"даже если потом дороже", "even if later more expensive",
                        "навіть якщо потім дорожче"});
    }

    private static boolean omissionBias(String t) {
        return multi(t,
                new String[]{"бездействие лучше", "inaction is better", "бездіяльність краще"},
                new String[]{"при том же вреде", "with the same harm", "при тій самій шкоді"});
    }
}
