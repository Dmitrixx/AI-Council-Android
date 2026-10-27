package com.aicouncil.app;

public final class TruncatedAnswerPolicySmokeTest {
    private static int checks;
    private static void check(String name, boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
    }
    public static void main(String[] args) {
        check("no marker means no continuation", TruncatedAnswerPolicy.chooseExpertSlot("finished") == -1);
        check("detects truncation marker", TruncatedAnswerPolicy.hasTruncatedAnswer("x " + TruncatedAnswerPolicy.TRUNCATION_MARKER));
        check("quick answer routes to first expert", TruncatedAnswerPolicy.chooseExpertSlot("⚡ БЫСТРЫЙ ОТВЕТ — model\n" + TruncatedAnswerPolicy.TRUNCATION_MARKER) == 0);
        check("independent audit routes to second expert", TruncatedAnswerPolicy.chooseExpertSlot("📝 ИСХОДНЫЙ ОТВЕТ\n...\n🛡 НЕЗАВИСИМЫЙ АУДИТ · model\n" + TruncatedAnswerPolicy.TRUNCATION_MARKER) == 1);
        check("coordinator answer routes to fourth slot", TruncatedAnswerPolicy.chooseExpertSlot("✦ ИИ №1\n...\n✦ ИИ №2\n...\n🏁 ИТОГ КООРДИНАТОРА · model\n" + TruncatedAnswerPolicy.TRUNCATION_MARKER) == 3);
        check("first peer critique routes to first expert", TruncatedAnswerPolicy.chooseExpertSlot("🔎 ВЗАИМНАЯ КРИТИКА · ИИ №1 проверяет ИИ №2\n" + TruncatedAnswerPolicy.TRUNCATION_MARKER) == 0);
        check("third expert answer routes to evidence slot", TruncatedAnswerPolicy.chooseExpertSlot("✦ ИИ №3 · АУДИТ ДОКАЗАТЕЛЬСТВ\n" + TruncatedAnswerPolicy.TRUNCATION_MARKER) == 2);
        check("second peer critique routes to second expert", TruncatedAnswerPolicy.chooseExpertSlot("🔎 ВЗАИМНАЯ КРИТИКА · ИИ №2 проверяет ИИ №1\n" + TruncatedAnswerPolicy.TRUNCATION_MARKER) == 1);
        check("chooses section for latest truncation", TruncatedAnswerPolicy.chooseExpertSlot("🏁 ИТОГ КООРДИНАТОРА\n" + TruncatedAnswerPolicy.TRUNCATION_MARKER + "\n✦ ИИ №1\n" + TruncatedAnswerPolicy.TRUNCATION_MARKER) == 0);
        check("a truncated follow-up routes to its model, not the older council section", TruncatedAnswerPolicy.chooseExpertSlot("🏁 ИТОГ КООРДИНАТОРА\nold\n🧠 ДОПОЛНЕНИЕ ИИ №3 · model\n" + TruncatedAnswerPolicy.TRUNCATION_MARKER) == 2);
        check("follow-up from second expert routes to second slot", TruncatedAnswerPolicy.chooseExpertSlot("🧠 ДОПОЛНЕНИЕ ИИ №2 · model\n" + TruncatedAnswerPolicy.TRUNCATION_MARKER) == 1);
        System.out.println("TruncatedAnswerPolicySmokeTest: " + checks + "/11 passed");
    }
}
