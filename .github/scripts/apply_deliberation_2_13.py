#!/usr/bin/env python3
"""Idempotent patch: directional antonyms + confidence mismatch for DeliberationIntelligence."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[2]
TARGET = ROOT / "app/src/main/java/com/aicouncil/app/DeliberationIntelligence.java"

def main():
    if not TARGET.exists():
        print("SKIP: DeliberationIntelligence.java missing")
        return 0
    src = TARGET.read_text(encoding="utf-8")
    if "directionalConflicts" in src and "extractConfidence" in src:
        print("OK: already patched")
        return 0

    if "DIRECTIONAL" not in src:
        needle = '    private static final String[] ABSOLUTE = {'
        if needle not in src:
            print("FAIL: ABSOLUTE block not found")
            return 1
        # Expand ABSOLUTE closing and insert DIRECTIONAL after the array
        # Safer: insert after first occurrence of ABSOLUTE array end
        marker = '"absolutely"' if '"absolutely"' in src else '"абсолютно"'
        # Find ABSOLUTE array end
        start = src.find("private static final String[] ABSOLUTE")
        if start < 0:
            print("FAIL: ABSOLUTE not found")
            return 1
        end = src.find("};", start)
        if end < 0:
            print("FAIL: ABSOLUTE end not found")
            return 1
        insert = '''};
    /** Directional antonym pairs (RU/EN). */
    private static final String[][] DIRECTIONAL = {
            {"рост", "падение"}, {"увеличи", "уменьш"}, {"повыс", "пониж"}, {"рост", "снижен"},
            {"рекоменд", "избег"}, {"принят", "отклон"}, {"запуск", "отлож"},
            {"высокий риск", "низкий риск"}, {"низкий риск", "высокий риск"},
            {"increase", "decrease"}, {"grow", "shrink"}, {"recommend", "avoid"}, {"approve", "reject"},
            {"bullish", "bearish"}, {"optimistic", "pessimistic"}, {"upside", "downside"}
    };
    private static final Pattern CONF_RANGE = Pattern.compile(
            "(?i)(?:уверенн(?:ость|ости)?|confidence|вероятност[ьи])[^0-9%]{0,28}(\\\\d{1,3})\\\\s*[-–—]\\\\s*(\\\\d{1,3})\\\\s*%?");
    private static final Pattern CONF_SINGLE = Pattern.compile(
            "(?i)(?:уверенн(?:ость|ости)?|confidence)[^0-9%]{0,24}(\\\\d{1,3})\\\\s*%");'''
        # The above has too many escapes for writing into java source via this script.
        # Write java-literal correctly:
        insert = """};
    /** Directional antonym pairs (RU/EN). */
    private static final String[][] DIRECTIONAL = {
            {"рост", "падение"}, {"увеличи", "уменьш"}, {"повыс", "пониж"}, {"рост", "снижен"},
            {"рекоменд", "избег"}, {"принят", "отклон"}, {"запуск", "отлож"},
            {"высокий риск", "низкий риск"}, {"низкий риск", "высокий риск"},
            {"increase", "decrease"}, {"grow", "shrink"}, {"recommend", "avoid"}, {"approve", "reject"},
            {"bullish", "bearish"}, {"optimistic", "pessimistic"}, {"upside", "downside"}
    };
    private static final Pattern CONF_RANGE = Pattern.compile(
            "(?i)(?:уверенн(?:ость|ости)?|confidence|вероятност[ьи])[^0-9%]{0,28}(\\d{1,3})\\s*[-–—]\\s*(\\d{1,3})\\s*%?");
    private static final Pattern CONF_SINGLE = Pattern.compile(
            "(?i)(?:уверенн(?:ость|ости)?|confidence)[^0-9%]{0,24}(\\d{1,3})\\s*%");"""
        src = src[:end] + insert + src[end + 2:]

    # Hook polarityConflicts end
    hook = 'conflicts.add("полярность: прямые ответы в начале текста расходятся (да/нет или рекомендовать/не рекомендовать)");'
    if "directionalConflicts" not in src and hook in src:
        methods = r'''
        directionalConflicts(la, lb, conflicts);
        confidenceMismatch(a, b, conflicts);
    }

    private static void directionalConflicts(String la, String lb, List<String> conflicts) {
        if (conflicts.size() >= MAX_CONFLICTS) return;
        for (String[] pair : DIRECTIONAL) {
            String left = pair[0];
            String right = pair[1];
            boolean aLeft = la.contains(left);
            boolean aRight = la.contains(right);
            boolean bLeft = lb.contains(left);
            boolean bRight = lb.contains(right);
            if ((aLeft && !aRight && bRight && !bLeft) || (aRight && !aLeft && bLeft && !bRight)) {
                conflicts.add("направление: эксперты расходятся по оси «" + left + "» ↔ «" + right + "»");
                if (conflicts.size() >= MAX_CONFLICTS) return;
            }
        }
    }

    private static void confidenceMismatch(String a, String b, List<String> conflicts) {
        if (conflicts.size() >= MAX_CONFLICTS) return;
        int[] ca = extractConfidence(a);
        int[] cb = extractConfidence(b);
        if (ca == null || cb == null) return;
        int midA = (ca[0] + ca[1]) / 2;
        int midB = (cb[0] + cb[1]) / 2;
        if (Math.abs(midA - midB) >= 25) {
            conflicts.add("уверенность: заявленные уровни сильно расходятся (ИИ1 ~"
                    + midA + "%, ИИ2 ~" + midB + "%) — сверь калибровку и base rates");
        } else if ((midA >= 85 && midB <= 55) || (midB >= 85 && midA <= 55)) {
            conflicts.add("уверенность: один эксперт чрезмерно уверен относительно другого — проверь overconfidence и residual risk");
        }
    }

    private static int[] extractConfidence(String text) {
        if (text == null || text.isEmpty()) return null;
        Matcher m = CONF_RANGE.matcher(text);
        if (m.find()) {
            Integer lo = parseIntSafe(m.group(1));
            Integer hi = parseIntSafe(m.group(2));
            if (lo != null && hi != null && lo <= hi && hi <= 100) return new int[]{lo, hi};
            if (lo != null && hi == null && lo <= 100) return new int[]{Math.max(0, lo - 10), lo};
        }
        Matcher s = CONF_SINGLE.matcher(text);
        if (s.find()) {
            Integer v = parseIntSafe(s.group(1));
            if (v != null && v <= 100) return new int[]{Math.max(0, v - 5), Math.min(100, v + 5)};
        }
        return null;
    }

    private static Integer parseIntSafe(String raw) {
        if (raw == null) return null;
        try {
            return Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
'''
        # Replace closing of polarityConflicts: after the hook's closing braces
        # Find the polarity method's final closing
        idx = src.find(hook)
        if idx < 0:
            print("FAIL: polarity hook not found")
            return 1
        # Find "    }\n\n    private static int computeAgreementScore" after idx
        close_marker = "    private static int computeAgreementScore"
        cidx = src.find(close_marker, idx)
        if cidx < 0:
            print("FAIL: computeAgreementScore not found after polarity")
            return 1
        # Walk back to previous method-closing brace block
        # Structure: ... }\n    }\n\n    private static int compute...
        # Insert before computeAgreementScore, replacing the last "    }\n\n" of polarityConflicts
        before = src[:cidx]
        # Remove the last "    }\n\n" that closed polarityConflicts
        if before.rstrip().endswith("}"):
            # strip trailing whitespace and one closing brace of polarityConflicts
            stripped = before.rstrip()
            # remove final }
            stripped = stripped[:-1].rstrip()
            src = stripped + methods + "\n\n" + src[cidx:]
        else:
            print("FAIL: unexpected structure before computeAgreementScore")
            return 1

    src = src.replace(
        'РАСХОЖДЕНИЯ (числа и полярность; проверь по тексту, не сглаживай)',
        'РАСХОЖДЕНИЯ (числа, полярность, направление, уверенность; проверь по тексту, не сглаживай)'
    )

    TARGET.write_text(src, encoding="utf-8")
    print("OK: patched DeliberationIntelligence")
    return 0

if __name__ == "__main__":
    sys.exit(main())
