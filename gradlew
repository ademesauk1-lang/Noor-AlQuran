#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# TEMPORARY CI diagnostics shim (removed once the build passes).
# ---------------------------------------------------------------------------
set -uo pipefail

LOG=/tmp/noor_build.log

emit() {
  local level="$1" title="$2" msg="$3"
  msg="${msg//%/%25}"; msg="${msg//$'\r'/%0D}"; msg="${msg//$'\n'/%0A}"
  title="${title//%/%25}"; title="${title//$'\n'/%0A}"
  echo "::${level} title=${title}::${msg}"
}

gradle "$@" --console=plain > "$LOG" 2>&1
STATUS=$?

emit notice "gradle exit status" "status=$STATUS | apks=$(find . -name '*.apk' | tr '\n' ' ')"

if [ "$STATUS" -ne 0 ]; then
  # Group errors by file so one noisy file cannot hide errors in later files.
  FILES=$(grep -E '^e: file://' "$LOG" | sed -E 's|^e: file://[^ ]+/Noor-AlQuran/([^:]+):.*|\1|' | sort -u)
  idx=0
  for f in $FILES; do
    idx=$((idx+1))
    [ "$idx" -gt 7 ] && break
    emit error "errors-in-$f" "$(grep -E "^e: .*$f:" "$LOG" | head -20)"
  done
  if [ "$idx" -eq 0 ]; then
    BLOCK=$(awk '/^> Task .*FAILED|^FAILURE:|^\* What went wrong:/,0' "$LOG" | head -80)
    [ -z "$BLOCK" ] && BLOCK=$(tail -80 "$LOG")
    emit error "gradle-failure-block" "$BLOCK"
  fi
fi

exit "$STATUS"
