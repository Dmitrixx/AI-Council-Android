package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class CurseOfKnowledgePolicySmokeTest {
    private static int checks;

    public static void main(String[] args) {
        flags("\u041e\u0447\u0435\u0432\u0438\u0434\u043d\u043e, \u0447\u0442\u043e \u043d\u0443\u0436\u043d\u043e \u0437\u0430\u043f\u0443\u0441\u043a\u0430\u0442\u044c \u0441\u0435\u0439\u0447\u0430\u0441.",
                "It is obvious that we should proceed.",
                true);
        quiet("\u0420\u0435\u043a\u043e\u043c\u0435\u043d\u0434\u0443\u044e \u043f\u043e\u0434\u043e\u0436\u0434\u0430\u0442\u044c. \u041f\u043e\u0441\u044b\u043b\u043a\u0430: \u043a\u043e\u043d\u0432\u0435\u0440\u0441\u0438\u044f \u0443\u043f\u0430\u043b\u0430 \u043d\u0430 12% \u0437\u0430 \u043d\u0435\u0434\u0435\u043b\u044e (\u0434\u0430\u043d\u043d\u044b\u0435 \u0438\u0437 \u0434\u0430\u0448\u0431\u043e\u0440\u0434\u0430).",
                "No claim of obviousness here.");
        quiet("\u042d\u0442\u043e curse of knowledge, \u0430 \u043d\u0435 \u0434\u043e\u043a\u0430\u0437\u0430\u0442\u0435\u043b\u044c\u0441\u0442\u0432\u043e. \u041d\u0435\u044f\u0432\u043d\u043e\u0435 \u0437\u043d\u0430\u043d\u0438\u0435 \u043d\u0435 \u043e\u0441\u043d\u043e\u0432\u0430\u043d\u0438\u0435.",
                "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u043d\u0430\u0437\u044b\u0432\u0430\u044e \u043e\u0448\u0438\u0431\u043a\u0443.");
        quiet("\u041f\u043e\u043a\u0430 \u0440\u0430\u043d\u043e \u0440\u0435\u0448\u0430\u0442\u044c: \u0434\u0430\u043d\u043d\u044b\u0445 \u043d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043e\u0447\u043d\u043e.",
                "\u041d\u0443\u0436\u043d\u043e \u0435\u0449\u0451 \u043f\u043e\u0441\u043c\u043e\u0442\u0440\u0435\u0442\u044c \u0436\u0443\u0440\u043d\u0430\u043b.");
        both("\u0412\u0441\u0435\u043c \u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e, \u0447\u0442\u043e \u044d\u0442\u043e \u043f\u043b\u043e\u0445\u043e.",
                "Everyone knows this is the right choice.");
        if (checks < 5) fail("expected at least 5 checks, got " + checks);
        System.out.println("CurseOfKnowledgePolicySmokeTest OK " + checks);
    }

    private static void flags(String a, String b, boolean either) {
        List<String> conflicts = new ArrayList<String>();
        CurseOfKnowledgePolicy.detect(a, b, conflicts);
        check(either && !conflicts.isEmpty(), "expected a curse-of-knowledge signal");
        check(conflicts.get(0).contains("possible signal"), "signal stays a hypothesis");
    }

    private static void both(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        CurseOfKnowledgePolicy.detect(a, b, conflicts);
        check(conflicts.size() >= 2, "both experts should add a second signal, got " + conflicts.size());
    }

    private static void quiet(String a, String b) {
        List<String> conflicts = new ArrayList<String>();
        CurseOfKnowledgePolicy.detect(a, b, conflicts);
        check(conflicts.isEmpty(), "naming or an explicit premise must stay quiet, got " + conflicts);
    }

    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) fail(message);
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }
}
