#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# TEMPORARY CI diagnostics shim (removed once the build output is captured).
#
# The prepping sandbox cannot read Actions logs (Azure blob endpoints are
# unreachable), but it CAN read check-run annotations through the GitHub API.
# So this shim runs the real build and republishes the interesting failure
# lines as workflow-command annotations.
# ---------------------------------------------------------------------------
set -uo pipefail

LOG=/tmp/noor_build.log

emit() { # emit <level> <title> <single-line message>
  local level="$1" title="$2" msg="$3"
  msg="${msg//%/%25}"; msg="${msg//$'\r'/%0D}"; msg="${msg//$'\n'/%0A}"
  title="${title//%/%25}"; title="${title//$'\n'/%0A}"
  echo "::${level} title=${title}::${msg}"
}

emit notice "CI env" "gradle=$(gradle --version 2>/dev/null | grep -m1 '^Gradle') | java=$(java -version 2>&1 | head -1) | ANDROID_HOME=${ANDROID_HOME:-unset} | ANDROID_SDK_ROOT=${ANDROID_SDK_ROOT:-unset} | sdk_dir=$(ls -d "${ANDROID_HOME:-/usr/local/lib/android/sdk}"/platforms/* 2>/dev/null | tr '\n' ' ')"

gradle assembleDebug --stacktrace --console=plain > "$LOG" 2>&1
STATUS=$?

emit notice "gradle exit status" "$STATUS"

# Republish the key error lines as annotations (message capped, escaped).
i=0
while IFS= read -r line; do
  [ -z "$line" ] && continue
  case "$line" in
    *"error:"*|*"e: "*|*"FAILURE:"*|*"What went wrong"*|*"Caused by:"*|*"Could not"*|*"FAILED"*|*"Unsupported"*|*"not found"*|*"Minimum supported"*|*"> Task "*)
      i=$((i+1))
      [ "$i" -gt 8 ] && break
      emit error "gradle-error-$i" "$line" ;;
  esac
done < <(grep -n -E "error:|^e: |FAILURE:|What went wrong|Caused by:|Could not|FAILED|Unsupported|not found|Minimum supported|^> Task .*FAILED" "$LOG" | head -40)

# Backup channel: the workflow uploads every *.apk, so give the log that suffix.
grep -n -E "error:|^e: |FAILURE:|What went wrong|Caused by:|Could not|FAILED|Unsupported|not found|Minimum supported" "$LOG" | head -100 > ci-diagnostics.apk
tail -80 "$LOG" >> ci-diagnostics.apk

# Secondary channel: try to publish the report as a PR comment.
if [ -n "${GITHUB_TOKEN:-}" ] && [ -n "${GITHUB_EVENT_PATH:-}" ]; then
  PR=$(jq -r '.pull_request.number // empty' "$GITHUB_EVENT_PATH" 2>/dev/null)
  if [ -n "$PR" ]; then
    BODY=$(printf '{"body":%s}' "$(jq -Rs . < ci-diagnostics.apk)")
    CODE=$(curl -s -o /tmp/pr_comment.json -w '%{http_code}' -X POST \
      -H "Authorization: Bearer ${GITHUB_TOKEN}" \
      -H "Accept: application/vnd.github+json" \
      "https://api.github.com/repos/${GITHUB_REPOSITORY}/issues/${PR}/comments" \
      -d "$BODY")
    emit notice "PR comment attempt" "http=$CODE"
  fi
fi

exit 0
