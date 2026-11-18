#!/usr/bin/env python3
"""Wire 3.71.0 institutional policies into DeliberationSignals if missing."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SIGNALS = ROOT / "app" / "src" / "main" / "java" / "com" / "aicouncil" / "app" / "DeliberationSignals.java"

def main() -> int:
    t = SIGNALS.read_text(encoding="utf-8")
    if "WinnersCursePolicy.detect" in t:
        print("already wired")
        return 0
    old = "        AdverseSelectionPolicy.detect(a, b, conflicts);\n    }"
    new = (
        "        AdverseSelectionPolicy.detect(a, b, conflicts);\n"
        "        WinnersCursePolicy.detect(a, b, conflicts);\n"
        "        InformationCascadePolicy.detect(a, b, conflicts);\n"
        "        FreeRiderPolicy.detect(a, b, conflicts);\n"
        "        PathDependencePolicy.detect(a, b, conflicts);\n"
        "        LucasCritiquePolicy.detect(a, b, conflicts);\n"
        "    }"
    )
    if old not in t:
        print("needle missing")
        return 1
    SIGNALS.write_text(t.replace(old, new, 1), encoding="utf-8")
    print("wired 3.71.0 policies")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
