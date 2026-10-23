package com.aicouncil.app;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public final class OcrModelIntegrityPolicySmokeTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        check(OcrModelIntegrityPolicy.VERSION.equals("4.1.0"), "pinned immutable model version");
        check(OcrModelIntegrityPolicy.isSupportedLanguage("rus"), "Russian model is supported");
        check(OcrModelIntegrityPolicy.isSupportedLanguage("ukr"), "Ukrainian model is supported");
        check(!OcrModelIntegrityPolicy.isSupportedLanguage("eng"), "unknown language is rejected");
        check(OcrModelIntegrityPolicy.expectedLength("rus") == 3_861_738L, "Russian model length is pinned");
        check(OcrModelIntegrityPolicy.expectedLength("ukr") == 3_825_102L, "Ukrainian model length is pinned");
        check(OcrModelIntegrityPolicy.expectedLength("eng") == -1L, "unknown model length fails closed");
        check(OcrModelIntegrityPolicy.expectedSha512("rus").length() == 128, "Russian SHA-512 is complete");
        check(OcrModelIntegrityPolicy.expectedSha512("ukr").length() == 128, "Ukrainian SHA-512 is complete");
        check(OcrModelIntegrityPolicy.verify("rus", 3_861_738L,
                OcrModelIntegrityPolicy.expectedSha512("rus")), "pinned Russian model digest accepted");
        check(OcrModelIntegrityPolicy.verify("ukr", 3_825_102L,
                OcrModelIntegrityPolicy.expectedSha512("ukr").toUpperCase()), "digest comparison is case-insensitive");
        check(!OcrModelIntegrityPolicy.verify("rus", 3_861_737L,
                OcrModelIntegrityPolicy.expectedSha512("rus")), "wrong file size rejected");
        check(!OcrModelIntegrityPolicy.verify("ukr", 3_825_102L, "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"), "wrong digest rejected");
        check(!OcrModelIntegrityPolicy.verify("eng", 3_861_738L,
                OcrModelIntegrityPolicy.expectedSha512("rus")), "digest cannot authorize another language");
        check(!OcrModelIntegrityPolicy.verify("rus", 3_861_738L, null), "missing digest rejected");
        String abcHash = OcrModelIntegrityPolicy.sha512(
                new ByteArrayInputStream("abc".getBytes(StandardCharsets.US_ASCII)));
        check(("ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a" +
                "2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f").equals(abcHash),
                "SHA-512 implementation matches known vector");
        check(!abcHash.equals(OcrModelIntegrityPolicy.expectedSha512("rus")), "other bytes do not match pinned model digest");
        System.out.println("OcrModelIntegrityPolicySmokeTest: " + checks + "/" + checks + " passed");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
