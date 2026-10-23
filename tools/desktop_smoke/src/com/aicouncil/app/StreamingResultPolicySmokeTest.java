package com.aicouncil.app;

public final class StreamingResultPolicySmokeTest {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }

    public static void main(String[] args) {
        check("card count fast", StreamingResultPolicy.streamingCardCount(0) == 1);
        check("card count audit", StreamingResultPolicy.streamingCardCount(2) == 2);
        check("card count council", StreamingResultPolicy.streamingCardCount(1) == 3);
        check("titles", StreamingResultPolicy.streamTitle(0).contains("основной")
                && StreamingResultPolicy.streamTitle(2).contains("координатор"));
        check("title OOB", StreamingResultPolicy.DEFAULT_HEADING.equals(StreamingResultPolicy.streamTitle(9)));

        StringBuilder buf = new StringBuilder();
        int n = StreamingResultPolicy.appendToken(buf, "hello");
        check("append basic", n == 5 && "hello".equals(buf.toString()));
        check("append null/empty", StreamingResultPolicy.appendToken(buf, null) == 0
                && StreamingResultPolicy.appendToken(buf, "") == 0);

        StringBuilder full = new StringBuilder();
        for (int i = 0; i < StreamingResultPolicy.MAX_BUFFER_CHARS; i++) full.append('x');
        int over = StreamingResultPolicy.appendToken(full, "OVERFLOW");
        check("append at cap", over == 0 && full.length() == StreamingResultPolicy.MAX_BUFFER_CHARS);

        StringBuilder near = new StringBuilder();
        for (int i = 0; i < StreamingResultPolicy.MAX_BUFFER_CHARS - 3; i++) near.append('y');
        int partial = StreamingResultPolicy.appendToken(near, "ABCDEF");
        check("append partial near cap", partial == 3 && near.length() == StreamingResultPolicy.MAX_BUFFER_CHARS);

        check("throttle empty forces", StreamingResultPolicy.shouldRefreshUi("", 1000, 999));
        check("throttle wait", !StreamingResultPolicy.shouldRefreshUi("a", 100, 50));
        check("throttle ok", StreamingResultPolicy.shouldRefreshUi("a", 200, 50));

        check("body empty", StreamingResultPolicy.WAITING_STREAM.equals(
                StreamingResultPolicy.streamingBodyDisplay(new StringBuilder())));
        check("body text", "hi".equals(StreamingResultPolicy.streamingBodyDisplay(new StringBuilder("hi"))));

        String longH = new String(new char[120]).replace('\0', 'H');
        check("heading truncate", StreamingResultPolicy.truncateHeading(longH).endsWith("…")
                && StreamingResultPolicy.truncateHeading(longH).length() == StreamingResultPolicy.HEADING_MAX_CHARS + 1);
        check("status labels",
                StreamingResultPolicy.STATUS_ERROR.equals(StreamingResultPolicy.stageStatusLabel("error"))
                        && StreamingResultPolicy.STATUS_PARTIAL.equals(StreamingResultPolicy.stageStatusLabel("partial"))
                        && StreamingResultPolicy.stageStatusLabel("ok") == null);
        check("duration", StreamingResultPolicy.durationLabel(1500).contains("1.5"));
        check("placeholder", StreamingResultPolicy.isPlaceholderResult("Ваш итоговый ответ появится здесь…"));

        System.out.println("StreamingResultPolicySmokeTest " + passed + "/" + (passed + failed));
        if (failed > 0) System.exit(1);
    }
}
