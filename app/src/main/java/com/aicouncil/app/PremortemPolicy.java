package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local pre-mortem and second-order effects signals.
 * Flags when a decision or plan is discussed without explicit failure-mode analysis,
 * second-order consequences, or "what would make this wrong" considerations.
 * Hypotheses requiring context, not verdicts. Coordinator must not average them away.
 */
public final class PremortemPolicy {
    private static final int MAX_SIGNALS = 5;

    private PremortemPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, decisionWithoutPremortem(a) || decisionWithoutPremortem(b),
                "премортем отсутствует: решение/план предложены без явного анализа режимов отказа или «что сделает это неправильным»");
        add(conflicts, initial, missingSecondOrder(a) || missingSecondOrder(b),
                "второго порядка эффекты не рассмотрены: прямые последствия названы, косвенные/системные отсутствуют");
        add(conflicts, initial, successOnlyFraming(a, b),
                "только успех: оба эксперта описывают позитивный сценарий без явного downside или failure mode");
        add(conflicts, initial, noKillCriteria(a) || noKillCriteria(b),
                "нет критериев отмены: план без условий, при которых следует остановиться или пересмотреть");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "премортем (возможный сигнал, требует контекста): " + core;
        for (String existing : conflicts) {
            if (existing.contains(core.substring(0, Math.min(42, core.length())))) return;
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

    private static boolean decisionWithoutPremortem(String t) {
        boolean decision = has(t, "рекомендую", "стоит", "следует", "план", "решение", "recommend", "should", "plan", "decide");
        boolean premortem = has(t, "премортем", "premortem", "pre-mortem", "режим отказа", "failure mode",
                "что если не", "what if it fails", "что сделает это неправильным", "kill criteria", "критерии отмены");
        return decision && !premortem;
    }

    private static boolean missingSecondOrder(String t) {
        boolean direct = has(t, "результат", "эффект", "последствие", "outcome", "effect", "consequence");
        boolean second = has(t, "второго порядка", "second-order", "косвенн", "системн", "ripple", "downstream", "долгосрочн");
        return direct && !second;
    }

    private static boolean successOnlyFraming(String a, String b) {
        boolean aPos = has(a, "успех", "выгод", "плюс", "success", "benefit", "upside") && !has(a, "риск", "потер", "fail", "downside", "риск");
        boolean bPos = has(b, "успех", "выгод", "плюс", "success", "benefit", "upside") && !has(b, "риск", "потер", "fail", "downside", "риск");
        return aPos && bPos;
    }

    private static boolean noKillCriteria(String t) {
        boolean plan = has(t, "план", "этап", "roadmap", "plan", "phase");
        boolean kill = has(t, "остановить", "отменить", "kill criteria", "stop if", "пересмотреть если", "если не сработает");
        return plan && !kill;
    }
}
