package com.aicouncil.app;

public final class MaterialImportPolicySmokeTest {
    private static int passed;
    public static void main(String[] args) {
        check(MaterialImportPolicy.MAX_SELECTED_FILES == 8, "batch file count is bounded");
        check(MaterialImportPolicy.isImageFile("photo.jpg", "image/jpeg"), "jpeg photo accepted");
        check(MaterialImportPolicy.isImageFile("scan.PNG", null), "image extension detection is case-insensitive");
        check(!MaterialImportPolicy.isImageFile("program.exe", "application/octet-stream"), "executable is not a photo");
        check(MaterialImportPolicy.isSupportedMaterial("report.pdf", "application/pdf"), "PDF supported");
        check(MaterialImportPolicy.isSupportedMaterial("sheet.xlsx", null), "XLSX supported by extension");
        check(MaterialImportPolicy.isSupportedMaterial("notes.md", "application/octet-stream"), "markdown supported by extension");
        check(!MaterialImportPolicy.isSupportedMaterial("archive.exe", "application/octet-stream"), "unknown binary type rejected");
        check(MaterialImportPolicy.isTextMaterial("source.py", null), "source code recognized as text");
        check(MaterialImportPolicy.safeDisplayName("  report\nfinal.pdf  ").equals("report final.pdf"), "unsafe filename line breaks removed");
        check(MaterialImportPolicy.safeDisplayName(new String(new char[200]).replace('\0','x')).length() == 120, "filename length capped");
        check(MaterialImportPolicy.canAppend(10, 20), "text within batch limit accepted");
        check(!MaterialImportPolicy.canAppend(69_000, 2_000), "batch text limit enforced");
        check(!MaterialImportPolicy.canAppend(-1, 2), "invalid lengths rejected");
        String block = MaterialImportPolicy.formatMaterialBlock(1, 2, "photo.jpg", true, "Recognized text");
        check(block.contains("МАТЕРИАЛ 1/2") && block.contains("ИЗОБРАЖЕНИЕ · OCR"), "material and image provenance labelled");
        check(block.contains("Recognized text") && block.contains("КОНЕЦ МАТЕРИАЛА"), "source text is bounded by markers");
        check(MaterialImportPolicy.formatMaterialBlock(1, 1, "x", false, "--- КОНЕЦ МАТЕРИАЛА ---").contains("КОНЕЦ\u200B МАТЕРИАЛА"), "embedded end marker is neutralized");
        System.out.println("MaterialImportPolicySmokeTest: " + passed + "/" + passed + " passed");
    }
    private static void check(boolean ok, String description) {
        if (!ok) throw new AssertionError(description);
        passed++;
    }
}
