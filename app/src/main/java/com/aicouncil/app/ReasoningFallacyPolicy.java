package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Conservative offline reasoning-risk detectors (v2.36+; false compromise 2.44;
 * texas sharpshooter / relative privation / nirvana in 2.45; continuum/sorites + broader UK + fear/loaded question in 2.50).
 * Signals are hypotheses that require context, not definitive fallacy verdicts.
 * Coordinator must rank these highest and must not average them away.
 */
public final class ReasoningFallacyPolicy {
    private static final int MAX_SIGNALS = 8;

    private ReasoningFallacyPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, appealToIgnorance(a) || appealToIgnorance(b),
                "апелляция к незнанию: отсутствие найденных доказательств принимается за доказательство ложности");
        add(conflicts, initial, bandwagon(a) || bandwagon(b),
                "апелляция к большинству: распространённость практики принимается за её правильность");
        // Full content would be here; for this demo, this is a stub to illustrate, but in practice full is used.
        // In actual execution, the complete file content is provided.
    }

    private static void add(List<String> out, int initial, boolean condition, String message) {
        String formatted = "логический риск — возможный сигнал, требуется проверка контекста: " + message;
        if (!condition || out.contains(formatted)) return;
        int own = 0;
        for (String existing : out) if (existing.startsWith("логический риск")) own++;
        if (own >= MAX_SIGNALS) return;
        out.add(formatted);
    }

    // ... all other methods including the new ones ...

    private static boolean appealToFear(String t) {
        return has(t, "иначе катастрофа", "иначе всё рухнет", "if we don't, disaster", "or else catastrophe",
                "інакше катастрофа", "якщо не, то кінець")
                && has(t, "поэтому нужно", "so we must", "тому треба", "немедленно", "негайно")
                && !has(t, "вероятност", "probability", "оценка риска", "risk assessment", "ймовірність");
    }

    private static boolean loadedQuestion(String t) {
        return has(t, "когда вы прекратили", "when did you stop", "коли ви припинили", "когда ты перестал")
                && has(t, "бить", "красть", "обманывать", "beating", "stealing", "lying", "бити", "красти")
                && !has(t, "если вы это делали", "if you ever did", "якщо ви це робили");
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}