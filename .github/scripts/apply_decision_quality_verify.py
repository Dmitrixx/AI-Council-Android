#!/usr/bin/env python3
"""Insert DecisionQualityPolicy static checks into tools/verify_project.py if missing."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
VERIFY = ROOT / "tools/verify_project.py"

QUALITY_CHECK = (
    "check('Качество решения: упущенная выгода, status quo, консенсус без опоры и фиксация на метрике — гипотезы на карте', "
    "(ROOT/'app/src/main/java/com/aicouncil/app/DecisionQualityPolicy.java').exists() and "
    "'DecisionQualityPolicy.detect(a, b, conflicts)' in (ROOT/'app/src/main/java/com/aicouncil/app/DeliberationSignals.java').read_text(encoding='utf-8') and "
    "'возможный сигнал' in (ROOT/'app/src/main/java/com/aicouncil/app/DecisionQualityPolicy.java').read_text(encoding='utf-8') and "
    "'DecisionQualityPolicySmokeTest' in runner and "
    "(ROOT/'tools/desktop_smoke/src/com/aicouncil/app/DecisionQualityPolicySmokeTest.java').exists())\n"
)

BALANCED = (
    "check('Скобки Java сбалансированы: DecisionQualityPolicy', "
    "balanced_java((ROOT/'app/src/main/java/com/aicouncil/app/DecisionQualityPolicy.java').read_text(encoding='utf-8')))\n"
)

def main():
    text = VERIFY.read_text(encoding="utf-8")
    if "DecisionQualityPolicy.detect" in text and "Скобки Java сбалансированы: DecisionQualityPolicy" in text:
        print("verify_project.py already has DecisionQualityPolicy checks")
        return
    if "DecisionQualityPolicy.detect" not in text:
        anchor = "EvidenceIndependencePolicySmokeTest' in runner and 'логический риск'"
        idx = text.find(anchor)
        if idx < 0:
            raise SystemExit("anchor for independence check not found")
        end = text.find("\n", idx)
        text = text[: end + 1] + QUALITY_CHECK + text[end + 1 :]
    if "Скобки Java сбалансированы: DecisionQualityPolicy" not in text:
        bj = "check('Скобки Java сбалансированы: ReasoningFallacyPolicy', balanced_java(FALLACY_POLICY.read_text(encoding='utf-8')))"
        if bj not in text:
            raise SystemExit("balanced fallacy check not found")
        text = text.replace(bj, bj + "\n" + BALANCED.rstrip("\n"), 1)
    VERIFY.write_text(text, encoding="utf-8")
    print("patched", VERIFY)

if __name__ == "__main__":
    main()
