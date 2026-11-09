#!/usr/bin/env python3
"""Static verification for AI-Council-Android.

Checks version consistency between app/build.gradle, key policy presence,
and that the local brain modules are wired. Exit 0 on success.
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GRADLE = ROOT / "app" / "build.gradle"
SIGNALS = ROOT / "app" / "src" / "main" / "java" / "com" / "aicouncil" / "app" / "DeliberationSignals.java"

EXPECTED_VERSION = "2.86.0"
EXPECTED_CODE = 153

REQUIRED_POLICIES = [
    "AutomationBiasPolicy",
    "JustWorldBiasPolicy",
    "ProjectionBiasPolicy",
    "DunningKrugerPolicy",
    "OptimismBiasPolicy",
    "NaiveRealismPolicy",
    "CruxPolicy",
    "EndowmentEffectPolicy",
    "MysideBiasPolicy",
    "FalsePrecisionPolicy",
    "IKEAEffectPolicy",
    "FocusingIllusionPolicy",
    "PeakEndRulePolicy",
    "ActionBiasPolicy",
    "NormalcyBiasPolicy",
    "BandwagonBiasPolicy",
    "IllusionOfControlPolicy",
    "ReactiveDevaluationPolicy",
    "ReversibilityPolicy",
    "StrawManPolicy",
    "ZeroSumBiasPolicy",
    "UnderdeterminationPolicy",
    "MotteAndBaileyPolicy",
    "AssumptionLedgerPolicy",
    "SemmelweisReflexPolicy",
    "ReactancePolicy",
    "SlipperySlopePolicy",
    "AdHominemPolicy",
    "WhataboutismPolicy",
    "MovingTheGoalpostsPolicy",
    "CircularReasoningPolicy",
    "PostHocPolicy",
    "ConfusionOfTheInversePolicy",
    "MiddleGroundFallacyPolicy",
    "AppealToConsequencesPolicy",
    "LossAversionPolicy",
    "StatusQuoPolicy",
    "SunkCostPolicy",
    "OutsideViewPolicy",
    "PremortemPolicy",
    "CalibrationPolicy",
    "TailRiskPolicy",
    "ExpectedUtilityPolicy",
]

def main() -> int:
    errors: list[str] = []

    if not GRADLE.is_file():
        errors.append(f"missing {GRADLE}")
    else:
        text = GRADLE.read_text(encoding="utf-8")
        m_name = re.search(r"versionName\s+'([^']+)'", text)
        m_code = re.search(r"versionCode\s+(\d+)", text)
        name = m_name.group(1) if m_name else None
        code = int(m_code.group(1)) if m_code else None
        if name != EXPECTED_VERSION:
            errors.append(f"versionName mismatch: got {name!r}, expected {EXPECTED_VERSION!r}")
        if code != EXPECTED_CODE:
            errors.append(f"versionCode mismatch: got {code}, expected {EXPECTED_CODE}")

    if not SIGNALS.is_file():
        errors.append(f"missing {SIGNALS}")
    else:
        signals = SIGNALS.read_text(encoding="utf-8")
        for policy in REQUIRED_POLICIES:
            if f"{policy}.detect" not in signals:
                errors.append(f"{policy} not wired into DeliberationSignals.detectAll")

    for name in ("MysideBiasPolicy.java", "FalsePrecisionPolicy.java", "EndowmentEffectPolicy.java", "IKEAEffectPolicy.java", "FocusingIllusionPolicy.java", "PeakEndRulePolicy.java", "ActionBiasPolicy.java", "NormalcyBiasPolicy.java", "BandwagonBiasPolicy.java", "IllusionOfControlPolicy.java", "ReactiveDevaluationPolicy.java", "ReversibilityPolicy.java", "StrawManPolicy.java", "ZeroSumBiasPolicy.java", "UnderdeterminationPolicy.java", "MotteAndBaileyPolicy.java", "AssumptionLedgerPolicy.java", "SemmelweisReflexPolicy.java", "ReactancePolicy.java", "SlipperySlopePolicy.java", "AdHominemPolicy.java", "WhataboutismPolicy.java", "MovingTheGoalpostsPolicy.java", "CircularReasoningPolicy.java", "PostHocPolicy.java", "ConfusionOfTheInversePolicy.java", "MiddleGroundFallacyPolicy.java", "AppealToConsequencesPolicy.java"):
        policy_path = ROOT / "app" / "src" / "main" / "java" / "com" / "aicouncil" / "app" / name
        if not policy_path.is_file():
            errors.append(f"missing {policy_path.name}")

    intel_path = ROOT / "app" / "src" / "main" / "java" / "com" / "aicouncil" / "app" / "DeliberationIntelligence.java"
    if not intel_path.is_file():
        errors.append("missing DeliberationIntelligence.java")
    else:
        intel = intel_path.read_text(encoding="utf-8")
        if "possible signal" not in intel or "isHypothesisSignal" not in intel:
            errors.append("confidence ceiling ignores English possible signal markers")

    activity = ROOT / "app" / "src" / "main" / "java" / "com" / "aicouncil" / "app" / "MainActivity.java"
    if not activity.is_file():
        errors.append("missing MainActivity.java")
    else:
        activity_text = activity.read_text(encoding="utf-8")
        if f'APP_VERSION = "{EXPECTED_VERSION}"' not in activity_text:
            errors.append(f"MainActivity APP_VERSION is not {EXPECTED_VERSION}")

    if errors:
        print("verify_project.py FAILED:")
        for e in errors:
            print(f"  - {e}")
        return 1

    print(f"verify_project.py OK — {EXPECTED_VERSION} / versionCode {EXPECTED_CODE}, {len(REQUIRED_POLICIES)} core policies wired")
    return 0

if __name__ == "__main__":
    sys.exit(main())
