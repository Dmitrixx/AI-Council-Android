#!/usr/bin/env python3
"""Restore full MainActivity from a known-good commit and bump APP_VERSION."""
from __future__ import annotations

import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MAIN = ROOT / "app" / "src" / "main" / "java" / "com" / "aicouncil" / "app" / "MainActivity.java"
GOOD_SHA = "e2ad281"


def main() -> None:
    content = subprocess.check_output(
        ["git", "show", f"{GOOD_SHA}:app/src/main/java/com/aicouncil/app/MainActivity.java"],
        cwd=ROOT,
        text=True,
    )
    content = content.replace('APP_VERSION = "3.26.0"', 'APP_VERSION = "3.46.0"')
    content = content.replace('APP_VERSION = "3.25.0"', 'APP_VERSION = "3.46.0"')
    MAIN.write_text(content, encoding="utf-8")
    size = MAIN.stat().st_size
    if size < 100_000:
        raise SystemExit(f"MainActivity too small after restore: {size}")
    print(f"restored MainActivity ({size} bytes) with APP_VERSION 3.46.0")


if __name__ == "__main__":
    main()
