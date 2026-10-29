package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Offline checks that a tiny sample or a single study is not treated as settled
 * science. Signals are hypotheses requiring context; the coordinator must not
 * average them away.
 */
public final class SampleEvidencePolicy {
    private static final int MAX_SIGNALS = 4;

    private SampleEvidencePolicy() {}

    public static void detect(String answerA, String answerB, List<String> conflicts) {
        if (conflicts == null) return;
        String a = lower(answerA);
        String b = lower(answerB);
        int initial = conflicts.size();
        add(conflicts, initial, singleCaseOverclaim(a) || singleCaseOverclaim(b),
                "n=1: единичный случай подаётся как общее правило без оговорки о выборке");
        add(conflicts, initial, singleStudySettled(a) || singleStudySettled(b),
                "одно исследование: результат одного источника принимается за окончательно доказанное");
        add(conflicts, initial, tinySampleCategorical(a) || tinySampleCategorical(b),
                "малая выборка: категоричный вывод при явно маленьком n без оговорки о неопределённости");
        add(conflicts, initial, missingControl(a) || missingControl(b),
                "нет контроля: эффект приписывается вмешательству без группы сравнения или до/после");
    }

    private static void add(List<String> out, int initial, boolean condition, String message) {
        String formatted = "карта решения — возможный сигнал, требуется проверка контекста: " + message;
        if (!condition || out.size() - initial >= MAX_SIGNALS || out.contains(formatted)) return;
        out.add(formatted);
    }

    /** One anecdote / one person used as a universal rule. */
    private static boolean singleCaseOverclaim(String text) {
        boolean single = has(text,
                "один случай", "один человек", "одна компания", "в моей практике",
                "у меня получилось", "я видел одного", "one case", "one person",
                "in my experience", "a single company", "один випадок", "у моїй практиці");
        boolean universal = has(text,
                "всегда так", "значит всегда", "для всех", "универсальн",
                "always works", "works for everyone", "therefore always",
                "для всіх", "завжди так");
        boolean hedge = has(text,
                "не обобща", "нельзя обобщ", "не репрезентатив", "anecdote",
                "недостаточно для обобщения", "не узагальн", "n=1 не доказывает");
        return single && universal && !hedge;
    }

    /** One study / one paper treated as final proof. */
    private static boolean singleStudySettled(String text) {
        boolean oneStudy = has(text,
                "одно исследование", "одно исследование показало", "одна статья",
                "одно исследование доказало", "a single study", "one study shows",
                "one paper proves", "одне дослідження", "одна стаття");
        boolean settled = has(text,
                "доказано", "окончательно", "вопрос закрыт", "settled science",
                "proves that", "therefore proven", "доказано остаточно",
                "більше не сумніватися");
        boolean hedge = has(text,
                "нужна репликация", "needs replication", "предварительн",
                "preliminary", "требуется подтверждение", "потрібна реплікація",
                "meta-analysis", "мета-анализ", "метааналіз");
        return oneStudy && settled && !hedge;
    }

    /** Explicit tiny n plus categorical language. */
    private static boolean tinySampleCategorical(String text) {
        boolean tiny = has(text,
                "n=3", "n = 3", "n=5", "n = 5", "n=10", "n = 10",
                "всего 5 человек", "всего 10 человек", "sample of 5",
                "sample of 10", "лишь трое", "only three people",
                "лише троє", "лише п'ятеро");
        boolean categorical = has(text,
                "доказано", "гарантированно", "всегда", "никогда не",
                "definitely", "always", "never fails", "гарантовано",
                "завжди", "ніколи не");
        boolean hedge = has(text,
                "слишком мал", "too small", "недостаточная мощность",
                "underpowered", "широкий интервал", "wide confidence",
                "занадто мал", "потрібен більший");
        return tiny && categorical && !hedge;
    }

    /** Effect claimed without any comparison / control language. */
    private static boolean missingControl(String text) {
        boolean claimsEffect = has(text,
                "благодаря вмешательству", "из-за нашей меры", "эффект от",
                "сработало благодаря", "caused by the intervention",
                "the treatment worked", "завдяки втручанню", "ефект від");
        boolean hasControl = has(text,
                "контрольная группа", "группа сравнения", "до и после",
                "control group", "before and after", "placebo",
                "рандомиз", "randomized", "контрольна група", "до і після");
        return claimsEffect && !hasControl;
    }

    private static boolean has(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
