package com.aicouncil.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/** Integrity metadata for the immutable Tesseract tessdata_fast 4.1.0 language files. */
public final class OcrModelIntegrityPolicy {
    public static final String VERSION = "4.1.0";
    public static final String HASH_ALGORITHM = "SHA-512";

    private static final String RUS = "rus";
    private static final String UKR = "ukr";
    private static final long RUS_BYTES = 3_861_738L;
    private static final long UKR_BYTES = 3_825_102L;
    // SHA-512 digests for the upstream tessdata_fast 4.1.0 files, verified against the distribution manifest.
    private static final String RUS_SHA512 =
            "65254c36df8274cee0a2c32e9632f3c2c3c2c0032314bf0a99a4c23309e862da7" +
            "c038ed85745d58697a814f44c656f22ef5a7815ae2bec33f80de1de8e1ea9dd";
    private static final String UKR_SHA512 =
            "67056a9a25206321c0804ed40520a7b6b1b74746bcbf57c1488faf250d014288" +
            "970de0ee73b7160dfd9fa509bc5fe59f8dcedc5bcbebf879ce87431e3c1fb057";

    private OcrModelIntegrityPolicy() { }

    public static boolean isSupportedLanguage(String language) {
        return RUS.equals(language) || UKR.equals(language);
    }

    public static long expectedLength(String language) {
        if (RUS.equals(language)) return RUS_BYTES;
        if (UKR.equals(language)) return UKR_BYTES;
        return -1L;
    }

    public static String expectedSha512(String language) {
        if (RUS.equals(language)) return RUS_SHA512;
        if (UKR.equals(language)) return UKR_SHA512;
        return null;
    }

    /** Checks both exact length and a pinned cryptographic digest; unsupported languages always fail closed. */
    public static boolean verify(String language, long length, String sha512Hex) {
        long expectedLength = expectedLength(language);
        String expectedDigest = expectedSha512(language);
        return expectedLength > 0 && length == expectedLength && expectedDigest != null
                && sha512Hex != null && expectedDigest.equals(sha512Hex.trim().toLowerCase(Locale.ROOT));
    }

    public static String sha512(File file) throws IOException {
        if (file == null || !file.isFile()) throw new IOException("OCR model file is missing");
        try (InputStream in = new FileInputStream(file)) {
            return sha512(in);
        }
    }

    public static String sha512(InputStream input) throws IOException {
        if (input == null) throw new IOException("OCR model stream is missing");
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-512 is not available on this runtime", impossible);
        }
        byte[] buffer = new byte[16 * 1024];
        int count;
        while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
        byte[] bytes = digest.digest();
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        return result.toString();
    }
}
