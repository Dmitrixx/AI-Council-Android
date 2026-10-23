#!/usr/bin/env python3
"""Apply all MainActivity policy extracts in one step (patch or sequential v1-v9)."""
from pathlib import Path
import subprocess
import sys
import urllib.request

# Known-good MainActivity before permanent extracts (commit 125a66e)
KNOWN_GOOD_URL = (
    "https://raw.githubusercontent.com/Dmitrixx/AI-Council-Android/"
    "125a66e17593c64c86d5e2b13028558c18b5d8dc/"
    "app/src/main/java/com/aicouncil/app/MainActivity.java"
)
MIN_EXPECTED_BYTES = 100000

def restore_known_good(activity: Path) -> None:
    print("MainActivity looks truncated/corrupt; restoring known-good snapshot…")
    with urllib.request.urlopen(KNOWN_GOOD_URL, timeout=60) as resp:
        data = resp.read()
    if len(data) < MIN_EXPECTED_BYTES:
        raise SystemExit("restored content too small: %d" % len(data))
    activity.write_bytes(data)
    print("MainActivity restored: %d bytes" % len(data))

def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found")
    size = activity.stat().st_size
    src = activity.read_text(encoding="utf-8", errors="replace")
    if size < MIN_EXPECTED_BYTES or "package com.aicouncil.app" not in src or "class MainActivity" not in src:
        restore_known_good(activity)
        src = activity.read_text(encoding="utf-8")

    if "BackupExportPolicy.MAX_BACKUP_BYTES" in src and "CouncilLaunchPolicy.MAX_TASK_CHARS" in src:
        print("MainActivity extracts: already applied")
        return

    patch = root / ".github/patches/mainactivity_policy_extracts.patch"
    scripts = root / ".github/scripts"
    if patch.is_file() and patch.stat().st_size > 100:
        r = subprocess.run(
            ["patch", "-p1", "--forward", "--reject-file=-", "-i", str(patch)],
            cwd=str(root),
            capture_output=True,
            text=True,
        )
        if r.returncode == 0:
            print("MainActivity extracts: applied via patch")
            return
        print("patch failed, falling back to sequential scripts:", (r.stderr or r.stdout)[:300])

    for v in range(1, 10):
        script = scripts / f"apply_mainactivity_extract_v{v}.py"
        if not script.is_file():
            raise SystemExit(f"missing {script.name}")
        subprocess.check_call([sys.executable, str(script), str(root)])
    print("MainActivity extracts: applied via v1-v9")


if __name__ == "__main__":
    main()
