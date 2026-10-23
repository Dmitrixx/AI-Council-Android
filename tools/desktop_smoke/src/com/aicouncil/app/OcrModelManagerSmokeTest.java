package com.aicouncil.app;

import java.io.File;
import java.io.IOException;

public final class OcrModelManagerSmokeTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        File sandbox = new File(System.getProperty("java.io.tmpdir"), "aicouncil-ocr-manager-smoke");
        deleteTree(sandbox);
        check(sandbox.mkdirs(), "temporary sandbox is created");
        OcrModelManager manager = new OcrModelManager(sandbox);
        check(manager.getTesseractDirectory().getParentFile().equals(sandbox), "model directory stays under app files directory");
        check("tesseract".equals(manager.getTesseractDirectory().getName()), "Tesseract directory name matches engine layout");
        File rus = manager.modelFileFor("rus");
        check("rus.traineddata".equals(rus.getName()), "Russian file name is deterministic");
        check("tessdata".equals(rus.getParentFile().getName()), "language file is placed in tessdata folder");
        check(!manager.hasUsableModel("rus"), "missing model is not reported as installed");
        check(!manager.hasUsableModel("ukr"), "missing Ukrainian model is not reported as installed");
        expectUnsupported(manager, "eng", "unsupported language cannot build a model path");
        expectUnsupported(manager, "../rus", "path traversal language is rejected");
        check(!new File(rus.getParentFile(), "rus.traineddata.download").exists(), "missing-model check does not create a partial download");
        check(manager.modelFileFor("ukr").getName().equals("ukr.traineddata"), "Ukrainian path is deterministic");
        try {
            manager.ensureModel("rus", () -> true);
            throw new AssertionError("pre-cancelled model download must not start");
        } catch (IOException expected) {
            check(OcrModelManager.CANCELLATION_MESSAGE.equals(expected.getMessage()),
                    "pre-cancelled download reports explicit cancellation");
        }
        check(!new File(sandbox, "tesseract").exists(), "pre-cancelled download does not create files or directories");
        check(OcrModelManager.isCancellation(new IOException(OcrModelManager.CANCELLATION_MESSAGE)),
                "cancellation exception is recognizable by UI layer");
        check(!OcrModelManager.isCancellation(new IOException("network failed")),
                "ordinary network errors are not treated as cancellation");
        try {
            new OcrModelManager(null);
            throw new AssertionError("null files directory should be rejected");
        } catch (IllegalArgumentException expected) {
            checks++;
        }
        deleteTree(sandbox);
        System.out.println("OcrModelManagerSmokeTest: " + checks + "/" + checks + " passed");
    }

    private static void expectUnsupported(OcrModelManager manager, String language, String message) throws Exception {
        try {
            manager.modelFileFor(language);
            throw new AssertionError(message);
        } catch (IOException expected) {
            checks++;
        }
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void deleteTree(File f) {
        if (f == null || !f.exists()) return;
        File[] children = f.listFiles();
        if (children != null) for (File child : children) deleteTree(child);
        f.delete();
    }
}
