#!/bin/sh
# Offline javac smoke runner. Skips Android-only types that need the SDK.
set -eu
ROOT=$(CDPATH= cd -- "$(dirname "$0")/../.." && pwd)
OUT=${TMPDIR:-/tmp}/aicouncil-smoke-$$
rm -rf "$OUT"
mkdir -p "$OUT"
LIST="$OUT/sources.txt"
find "$ROOT/app/src/main/java" "$ROOT/tools/desktop_smoke/src" -name '*.java' \
  ! -name 'MainActivity.java' \
  ! -name 'CouncilCrypto.java' \
  ! -name 'CouncilShell.java' \
  ! -name 'ChipFlowLayout.java' \
  ! -name 'LocalDataStore.java' \
  ! -name 'ChatCompletionsClient.java' \
  > "$LIST"
javac --release 8 -encoding UTF-8 -d "$OUT" @"$LIST"

failed=0
ran=0
for src in $(find "$ROOT/tools/desktop_smoke/src" -name '*SmokeTest.java' | sort); do
  cls=$(printf '%s' "$src" | sed "s|$ROOT/tools/desktop_smoke/src/||; s|/|.|g; s|\\.java$||")
  ran=$((ran + 1))
  if ! java -cp "$OUT" "$cls"; then
    echo "FAILED $cls" >&2
    failed=$((failed + 1))
  fi
done
rm -rf "$OUT"
if [ "$failed" -ne 0 ]; then
  echo "desktop_smoke: $failed/$ran classes failed" >&2
  exit 1
fi
echo "desktop_smoke: $ran/$ran classes passed"
exit 0
