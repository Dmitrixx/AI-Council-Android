package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class SampleEvidencePolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        List<String> out = new ArrayList<String>();

        SampleEvidencePolicy.detect(
                "В моей практике один случай. Значит всегда так работает для всех.",
                "Нейтральный текст без обобщений.",
                out);
        check(has(out, "n=1"), "single case overclaim is flagged");

        out.clear();
        SampleEvidencePolicy.detect(
                "В моей практике один случай, но нельзя обобщать: n=1 не доказывает правило.",
                "Ок.",
                out);
        check(!has(out, "n=1"), "hedged single case stays quiet");

        out.clear();
        SampleEvidencePolicy.detect(
                "Одно исследование доказало: вопрос закрыт, settled science.",
                "Нейтральный ответ.",
                out);
        check(has(out, "одно исследование"), "single study treated as settled is flagged");

        out.clear();
        SampleEvidencePolicy.detect(
                "Одно исследование показало эффект, но нужна репликация и meta-analysis.",
                "Ок.",
                out);
        check(!has(out, "одно исследование"), "replication hedge suppresses single-study signal");

        out.clear();
        SampleEvidencePolicy.detect(
                "Sample of 5: definitely always works, guaranteed.",
                "Нейтральный.",
                out);
        check(has(out, "малая выборка"), "tiny sample with categorical claim is flagged");

        out.clear();
        SampleEvidencePolicy.detect(
                "Sample of 5 is too small and underpowered; wide confidence interval.",
                "Ок.",
                out);
        check(!has(out, "малая выборка"), "acknowledged small sample stays quiet");

        out.clear();
        SampleEvidencePolicy.detect(
                "Эффект от меры: сработало благодаря вмешательству.",
                "Нейтральный.",
                out);
        check(has(out, "нет контроля"), "effect without control group is flagged");

        out.clear();
        SampleEvidencePolicy.detect(
                "Эффект от меры виден относительно контрольной группы и placebo.",
                "Ок.",
                out);
        check(!has(out, "нет контроля"), "explicit control suppresses the signal");

        out.clear();
        SampleEvidencePolicy.detect(
                "Обычный совет по архитектуре без выборок.",
                "Тоже обычный совет.",
                out);
        check(out.isEmpty(), "plain architecture advice stays quiet");

        // Integration: DeliberationSignals must surface the map-prefixed line.
        out.clear();
        DeliberationSignals.detectAll(
                "Одно исследование доказало: вопрос закрыт.",
                "Нейтральный ответ без доказательств.",
                out);
        check(has(out, "карта решения"), "DeliberationSignals wires SampleEvidencePolicy");

        System.out.println("SampleEvidencePolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static boolean has(List<String> out, String part) {
        for (String line : out) if (line.contains(part)) return true;
        return false;
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            System.err.println("FAIL " + label);
            System.exit(1);
        }
        passed++;
    }
}
