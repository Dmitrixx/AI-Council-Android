#!/usr/bin/env python3
"""Apply all MainActivity policy extracts in one step (patch or sequential v1-v9)."""
from pathlib import Path
import subprocess
import sys

def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    activity = root / "app/src/main/java/com/aicouncil/app/MainActivity.java"
    if not activity.is_file():
        raise SystemExit("MainActivity.java not found")
    src = activity.read_text(encoding="utf-8")
    if "BackupExportPolicy.MAX_BACKUP_BYTES" in src and "CouncilLaunchPolicy.MAX_TASK_CHARS" in src:
        print("MainActivity extracts: already applied")
        return

    patch = root / ".github/patches/mainactivity_policy_extracts.patch"
    scripts = root / ".github/scripts"
    if patch.is_file():
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
