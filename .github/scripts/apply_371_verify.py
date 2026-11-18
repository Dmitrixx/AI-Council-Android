#!/usr/bin/env python3
"""Bump verify_project expected version and required policy list for 3.71.0."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
VP = ROOT / "tools/verify_project.py"

def main() -> int:
    t = VP.read_text(encoding="utf-8")
    t2 = t.replace('EXPECTED_VERSION = "3.70.0"', 'EXPECTED_VERSION = "3.71.0"')
    t2 = t2.replace("EXPECTED_CODE = 238", "EXPECTED_CODE = 239")
    old = '''REQUIRED_POLICIES = [
    "AdverseSelectionPolicy",
    "MoralHazardPolicy",'''
    new = '''REQUIRED_POLICIES = [
    "WinnersCursePolicy",
    "InformationCascadePolicy",
    "FreeRiderPolicy",
    "PathDependencePolicy",
    "LucasCritiquePolicy",
    "AdverseSelectionPolicy",
    "MoralHazardPolicy",'''
    if "WinnersCursePolicy" not in t2:
        if old not in t2:
            raise SystemExit("REQUIRED_POLICIES needle missing")
        t2 = t2.replace(old, new, 1)
    if t2 != t:
        VP.write_text(t2, encoding="utf-8")
        print("verify_project.py updated for 3.71.0")
    else:
        print("verify_project.py already at 3.71.0")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
