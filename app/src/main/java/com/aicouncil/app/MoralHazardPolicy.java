package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local moral-hazard signals.
 * Flags a verdict that treats insured, guaranteed or third-party-covered downside
 * as if the protected party will stay equally careful, without stating who bears
 * the loss or how shifted risk can change behavior.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely because someone else pays for the failure.
 */
public final class MoralHazardPolicy {
    private static final int MAX_SIGNALS = 2;

    private MoralHazardPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, ignoresShift(a) || ignoresShift(b),
                "moral hazard: вердикт закрывает выбор так, будто страховка или чужая компенсация не меняет осторожность");
        add(conflicts, initial, ignoresShift(a) && ignoresShift(b),
                "оба эксперта закрывают без учёта переноса downside на страховщика или третью сторону");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "moral hazard (possible signal, requires context): " + core;
        String key = core.substring(0, Math.min(40, core.length()));
        for (String existing : conflicts) {
            if (existing.contains(key)) return;
        }
        conflicts.add(signal);
    }

    private static boolean has(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean acknowledges(String t) {
        return has(t,
                "moral hazard", "моральный риск", "моральная угроза",
                "shifted downside", "перенос риска", "переложенный риск",
                "поведение изменится", "станет рискованнее", "risk-taking will increase",
                "кто несёт убыток", "who bears the loss");
    }

    private static boolean ignoresShift(String t) {
        boolean covered = has(t,
                "застраховано", "страховка покроет", "bailout", "государство компенсирует",
                "убытки покрывает", "downside is insured", "не своими деньгами",
                "гарантия возврата", "losses are covered");
        boolean sameCare = has(t,
                "будут так же осторожны", "поведение не изменится", "risk taking unchanged",
                "осторожность сохранится", "не станут рисковать больше",
                "will stay just as careful", "behavior will not change");
        return covered && sameCare && !acknowledges(t);
    }
}
