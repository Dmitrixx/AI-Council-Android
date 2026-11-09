package com.aicouncil.app;

import java.util.ArrayList;
import java.util.List;

public final class AppealToConsequencesPolicySmokeTest {
    private static int passed;

    public static void main(String[] args) {
        signal("english disaster is treated as disproof",
                "The claim can't be true because otherwise the economy would have to change.");
        signal("russian bad outcome is treated as falsity",
                "Раз так будет плохо, значит это неправда и обсуждать нечего.");
        signal("wish is treated as proof",
                "It must be true because otherwise the plan falls apart.");
        noSignal("naming the fallacy is not committing it",
                "That would be an appeal to consequences, and consequences are not evidence.");
        noSignal("separating stake from fact stays quiet",
                "Последствия не доказательство: страшный исход не делает тезис ложным.");
        noSignal("a real conditional without a truth claim stays quiet",
                "Если запас меньше недели, сначала проверьте склад, а потом решайте.");

        List<String> both = new ArrayList<String>();
        AppealToConsequencesPolicy.detect(
                "Это правда, потому что иначе будет хаос.",
                "We must believe it or else the whole case collapses.",
                both);
        check(count(both, "appeal to consequences") == 2, "both experts closing on consequences are both flagged");

        List<String> wired = new ArrayList<String>();
        DeliberationSignals.detectAll(
                "Слишком страшные последствия, значит гипотеза ложна.",
                "Я оставляю вопрос открытым до нового наблюдения.",
                wired);
        check(contains(wired, "appeal to consequences"), "detectAll wires the appeal-to-consequences check");

        DeliberationIntelligence.Brief brief = DeliberationIntelligence.analyze(
                "Стоит ли считать вывод доказанным?",
                "Это неверно, потому что последствия неприемлемы.",
                "Нужно отдельное наблюдение, ставка не заменяет факт.");
        String map = DeliberationIntelligence.formatForCoordinator(brief);
        check(map.contains("Желаемые или страшные последствия"), "coordinator is told not to treat consequences as proof");

        System.out.println("AppealToConsequencesPolicySmokeTest: " + passed + "/" + passed + " passed");
    }

    private static void signal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        AppealToConsequencesPolicy.detect(text, "", conflicts);
        check(contains(conflicts, "appeal to consequences"), label);
    }

    private static void noSignal(String label, String text) {
        List<String> conflicts = new ArrayList<String>();
        AppealToConsequencesPolicy.detect(text, "", conflicts);
        check(!contains(conflicts, "appeal to consequences"), label);
    }

    private static boolean contains(List<String> items, String needle) {
        for (String item : items) {
            if (item.contains(needle)) return true;
        }
        return false;
    }

    private static int count(List<String> items, String needle) {
        int n = 0;
        for (String item : items) {
            if (item.contains(needle)) n++;
        }
        return n;
    }

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
}
