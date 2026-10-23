package com.aicouncil.app;

import java.util.Arrays;

public final class InputImportPolicySmokeTest {
    private static int p = 0, f = 0;

    private static void check(String n, boolean ok) {
        if (ok) { p++; System.out.println("PASS " + n); }
        else { f++; System.out.println("FAIL " + n); }
    }

    public static void main(String[] a) {
        check("best label", InputImportPolicy.candidateLabel(0, "  hello  world ").equals("Лучший вариант · hello world"));
        check("alt label", InputImportPolicy.candidateLabel(2, "x").startsWith("Вариант 3 · "));
        String[] labels = InputImportPolicy.candidateLabels(Arrays.asList("a", "b"));
        check("labels size", labels.length == 2 && labels[0].contains("Лучший"));
        check("voice err", InputImportPolicy.voiceOpenError(null).contains("неизвестная ошибка"));
        check("picker", InputImportPolicy.pickerOpenError("PDF", "boom").contains("PDF")
                && InputImportPolicy.pickerOpenError("PDF", "boom").contains("boom"));
        check("import fail", InputImportPolicy.importFailure("io").equals("Импорт не выполнен: io"));
        check("consts", InputImportPolicy.VOICE_APPLIED.length() > 10
                && InputImportPolicy.IMPORT_TEXT_CANCELLED.contains("текстового"));
        System.out.println("InputImportPolicySmokeTest " + p + "/" + (p + f));
        if (f > 0) System.exit(1);
    }
}
