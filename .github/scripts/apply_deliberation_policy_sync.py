#!/usr/bin/env python3
"""Sync the repository's recent deliberation policies into the prepared Android source archive."""
from pathlib import Path
import shutil
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: apply_deliberation_policy_sync.py PROJECT_DIR")

repository = Path(__file__).resolve().parents[2]
project = Path(sys.argv[1]).resolve()
source_app = repository / "app/src/main/java/com/aicouncil/app"
target_app = project / "app/src/main/java/com/aicouncil/app"

POLICIES = (
    "AnchoringPolicy.java",
    "StatusQuoPolicy.java",
    "AvailabilityPolicy.java",
    "RepresentativenessPolicy.java",
    "PlanningFallacyPolicy.java",
    "HindsightBiasPolicy.java",
    "AmbiguityAversionPolicy.java",
    "DeliberationSignals.java",
)
for name in POLICIES:
    source = source_app / name
    if not source.is_file():
        raise SystemExit("Missing repository policy source: " + str(source))
    target_app.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target_app / name)

relative_files = (
    "tools/desktop_smoke/run.sh",
    "tools/desktop_smoke/src/com/aicouncil/app/AnchoringStatusQuoPolicySmokeTest.java",
    "tools/desktop_smoke/src/com/aicouncil/app/RecentHeuristicsSmokeTest.java",
    "tools/desktop_smoke/src/com/aicouncil/app/HindsightAmbiguitySmokeTest.java",
)
for relative in relative_files:
    source = repository / relative
    if not source.is_file():
        raise SystemExit("Missing smoke-test source: " + str(source))
    target = project / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target)

print("Synced %d deliberation policy sources and current smoke tests." % len(POLICIES))
