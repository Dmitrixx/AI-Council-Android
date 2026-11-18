#!/usr/bin/env python3
"""Apply 3.71.0 version bump and policy wiring."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

def replace_once(path: Path, old: str, new: str, label: str) -> None:
    t = path.read_text(encoding="utf-8")
    if label == "wire" and "WinnersCursePolicy.detect" in t:
        print(f"skip {label}: already applied")
        return
    if old not in t:
        # Gradle / MainActivity may already be at 3.71.0
        if label in ("gradle", "activity"):
            print(f"skip {label}: needle missing (likely already applied)")
            return
        raise SystemExit(f"needle missing for {label} in {path}")
    path.write_text(t.replace(old, new, 1), encoding="utf-8")
    print(f"applied {label}")

def main() -> int:
    signals = ROOT / "app/src/main/java/com/aicouncil/app/DeliberationSignals.java"
    replace_once(
        signals,
        "        AdverseSelectionPolicy.detect(a, b, conflicts);\n    }",
        "        AdverseSelectionPolicy.detect(a, b, conflicts);\n"
        "        WinnersCursePolicy.detect(a, b, conflicts);\n"
        "        InformationCascadePolicy.detect(a, b, conflicts);\n"
        "        FreeRiderPolicy.detect(a, b, conflicts);\n"
        "        PathDependencePolicy.detect(a, b, conflicts);\n"
        "        LucasCritiquePolicy.detect(a, b, conflicts);\n"
        "    }",
        "wire",
    )

    gradle = ROOT / "app/build.gradle"
    gt = gradle.read_text(encoding="utf-8")
    gt2 = gt.replace("versionCode 238", "versionCode 239").replace(
        "versionName '3.70.0'", "versionName '3.71.0'"
    )
    if gt2 != gt:
        gradle.write_text(gt2, encoding="utf-8")
        print("gradle version bumped")
    else:
        print("gradle already at 3.71.0 or different baseline")

    activity = ROOT / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    at = activity.read_text(encoding="utf-8")
    at2 = at.replace('APP_VERSION = "3.70.0"', 'APP_VERSION = "3.71.0"')
    if at2 != at:
        activity.write_text(at2, encoding="utf-8")
        print("MainActivity version bumped")
    else:
        print("MainActivity already at 3.71.0 or different baseline")

    return 0

if __name__ == "__main__":
    raise SystemExit(main())
