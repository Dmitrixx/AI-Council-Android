package com.aicouncil.app;

public final class BaseRateReminderPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        String reminder = BaseRateReminderPolicy.evaluateExplicitRequest(
                "У пациента редкое заболевание, тест положительный. Какова вероятность болезни?");
        check(!reminder.isEmpty(), "rare condition and positive test trigger base-rate reminder");
        check(reminder.contains("теорема Байеса") && reminder.contains("не диагноз"),
                "reminder explains Bayes and avoids diagnosis");
        check(BaseRateReminderPolicy.evaluateExplicitRequest(
                "Rare disease, positive test, prevalence 0.2%. What is the posterior?").isEmpty(),
                "explicit prevalence suppresses generic reminder");
        check(BaseRateReminderPolicy.evaluateExplicitRequest(
                "A positive test was reported, but the condition is common.").isEmpty(),
                "no rare-condition cue stays silent");
        check(BaseRateReminderPolicy.evaluateExplicitRequest(
                "Редкое заболевание обсуждается без теста.").isEmpty(),
                "no positive-test cue stays silent");
        check(BaseRateReminderPolicy.evaluateExplicitRequest(null).isEmpty(),
                "null input stays silent");
        System.out.println("BaseRateReminderPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        passed++;
    }
}
