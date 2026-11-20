package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Local rhyme-as-reason / processing-fluency signals.
 * Flags a verdict that treats a fluent, easy-to-process, catchy, or rhyming claim
 * as more true, preferable, or decisive solely because of its fluency or form,
 * without independent evidence or mechanism.
 * Naming the gap is not itself an error.
 * The coordinator cannot close solely on processing fluency or rhyme.
 */
public final class RhymeAsReasonPolicy {
    private static final int MAX_SIGNALS = 2;

    private RhymeAsReasonPolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();

        add(conflicts, initial, closesOnFluency(a) || closesOnFluency(b),
                "rhyme-as-reason / fluency: вердикт закрывает выбор на лёгкости восприятия, рифме или catchy формулировке без независимого основания");
        add(conflicts, initial, closesOnFluency(a) && closesOnFluency(b),
                "оба эксперта закрывают на processing fluency или рифме");
    }

    private static void add(List<String> conflicts, int initial, boolean hit, String core) {
        if (!hit) return;
        if (conflicts.size() - initial >= MAX_SIGNALS) return;
        String signal = "rhyme as reason (possible signal, requires context): " + core;
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
                "rhyme as reason", "рифма как причина", "processing fluency",
                "fluency is not evidence", "лёгкость не доказательство",
                "catchy does not make it true", "рифма не делает истинным");
    }

    private static boolean closesOnFluency(String t) {
        boolean fluent = has(t,
                "звучит убедительно", "sounds convincing", "easy to remember",
                "рифмуется", "it rhymes", "catchy phrase", "запоминающаяся формулировка",
                "просто звучит правильно", "it just feels right", "flows well",
                "the slogan says it all", "слоган говорит сам за себя");
        boolean closes = has(t,
                "поэтому вердикт", "therefore the conclusion", "значит так и есть",
                "so it must be true", "закрываем на этом", "this settles it",
                "поэтому выбираем", "so we go with");
        boolean independent = has(t,
                "независимое основание", "independent evidence", "механизм",
                "base rate", "базовая частота", "данные показывают", "data show");
        return fluent && closes && !independent && !acknowledges(t);
    }
}
