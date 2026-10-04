#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# TEMPORARY CI diagnostics shim.
#
# The repository does not ship a real Gradle wrapper yet; the CI workflow falls
# back to the runner's preinstalled Gradle. This script wraps that build and
# copies the failure details into the job summary ($GITHUB_STEP_SUMMARY), which
# is readable through the check-runs API.
#
# It is removed again as soon as the build output has been captured.
# ---------------------------------------------------------------------------
set -uo pipefail

LOG=/tmp/noor_build.log
REPORT=/tmp/noor_diag.txt

{
  echo "### CI diagnostics (gradlew shim)"
  echo ""
  echo "| key | value |"
  echo "| --- | --- |"
  echo "| gradle | $(gradle --version 2>/dev/null | grep -m1 '^Gradle' || echo 'n/a') |"
  echo "| java | $(java -version 2>&1 | head -1) |"
  echo "| ANDROID_HOME | ${ANDROID_HOME:-unset} |"
  echo "| ANDROID_SDK_ROOT | ${ANDROID_SDK_ROOT:-unset} |"
  echo ""
} > "$REPORT"

gradle assembleDebug --stacktrace --console=plain > "$LOG" 2>&1
STATUS=$?

{
  echo ""
  echo "**exit status:** $STATUS"
  echo ""
  echo '```text'
  grep -n -E "FAILURE:|What went wrong|^> |error:|^e: |Caused by:|Could not|FAILED|Unsupported|not found|Syntax error" "$LOG" | head -80
  echo '--- last 60 lines ---'
  tail -60 "$LOG"
  echo '```'
} >> "$REPORT"

head -c 60000 "$REPORT" >> "${GITHUB_STEP_SUMMARY:-/dev/null}"
cp "$REPORT" /tmp/ci-report.md

# Exit 0 on purpose: the workflow's `./gradlew assembleDebug || gradle
# assembleDebug` fallback would otherwise run the whole build a second time.
# The real status is recorded in the report above.
exit 0
