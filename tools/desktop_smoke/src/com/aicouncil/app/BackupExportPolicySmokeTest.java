package com.aicouncil.app;

public final class BackupExportPolicySmokeTest {
    private static int p = 0, f = 0;

    private static void check(String n, boolean ok) {
        if (ok) { p++; System.out.println("PASS " + n); }
        else { f++; System.out.println("FAIL " + n); }
    }

    public static void main(String[] a) {
        check("fits", BackupExportPolicy.fitsBackupSize(100)
                && !BackupExportPolicy.fitsBackupSize(BackupExportPolicy.MAX_BACKUP_BYTES + 1));
        check("shrink stop", !BackupExportPolicy.canShrinkFurther(750, 3000, 1500));
        check("shrink ok", BackupExportPolicy.canShrinkFurther(30000, 100000, 60000));
        check("next", BackupExportPolicy.nextTaskLimit(30000) < 30000
                && BackupExportPolicy.nextTaskLimit(100) == 750);
        check("saved", BackupExportPolicy.savedMessage(true).contains("сокращена")
                && BackupExportPolicy.savedMessage(false).contains("не включены"));
        check("file base empty", BackupExportPolicy.resultFileBase("").equals("AI_Council_result"));
        check("file base long", BackupExportPolicy.resultFileBase("a".repeat(100)).length() == 48);
        check("title", BackupExportPolicy.backupFileTitle("20260101_1200").startsWith("AI_Council_backup_"));
        System.out.println("BackupExportPolicySmokeTest " + p + "/" + (p + f));
        if (f > 0) System.exit(1);
    }
}
