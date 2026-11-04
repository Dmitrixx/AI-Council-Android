#!/usr/bin/env python3
"""Sync current repository policies, smoke tests, build metadata and docs into prepared Android source."""
from pathlib import Path
import shutil
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: apply_deliberation_policy_sync.py PROJECT_DIR")

repository = Path(__file__).resolve().parents[2]
project = Path(sys.argv[1]).resolve()
source_app = repository / "app/src/main/java/com/aicouncil/app"
target_app = project / "app/src/main/java/com/aicouncil/app"


def copy_unless_same(source: Path, target: Path) -> None:
    if source.resolve() == target.resolve():
        return
    if not source.is_file():
        raise SystemExit("Missing repository source: " + str(source))
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target)


# Sync every current policy because DeliberationSignals is the authoritative registry.
policy_sources = sorted(source_app.rglob("*Policy.java"))
for source in policy_sources:
    copy_unless_same(source, target_app / source.relative_to(source_app))
for name in ("DeliberationSignals.java", "SynthesisProtocol.java"):
    copy_unless_same(source_app / name, target_app / name)

# Keep the prepared project aligned with the repository's version, dependencies and checks.
for relative in (
    "app/build.gradle",
    "tools/verify_project.py",
    "tools/desktop_smoke/run.sh",
    "README.md",
    "README_RU.md",
    "CHANGELOG_RU.md",
):
    copy_unless_same(repository / relative, project / relative)

# The runner references many focused smoke tests; sync all current Java sources and stubs.
smoke_source = repository / "tools/desktop_smoke/src"
smoke_files = sorted(smoke_source.rglob("*.java"))
for source in smoke_files:
    copy_unless_same(source, project / source.relative_to(repository))

print("Synced %d policy sources, %d smoke Java sources, current Gradle metadata and docs."
      % (len(policy_sources), len(smoke_files)))
