#!/usr/bin/env bash
set -euo pipefail

# Usage: ./generate-dmg.sh [app-version]
# Uses jpackage to create a macOS .dmg. Run on macOS with a JDK that includes jpackage.

APP_NAME=${APP_NAME:-graph-digitizer}
APP_VERSION=${1:-1.0.0}

if [ -z "${JAVA_HOME:-}" ]; then
  echo "ERROR: JAVA_HOME is not set. Please set JAVA_HOME to your JDK (with jpackage) and re-run." >&2
  exit 1
fi

JPACKAGE="$JAVA_HOME/bin/jpackage"
if [ ! -x "$JPACKAGE" ]; then
  echo "ERROR: jpackage not found or not executable at $JPACKAGE" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TARGET_DIR="$SCRIPT_DIR/../target"
DEST_DIR="$SCRIPT_DIR/../target/generated_builds"

JAR=$(ls -t "$TARGET_DIR"/*.jar 2>/dev/null | grep -vE 'sources|original|tests' || true)
JAR="$(echo "$JAR" | head -n1)"

if [ -z "$JAR" ]; then
  echo "ERROR: No JAR found in $TARGET_DIR. Build first (mvn package)." >&2
  exit 1
fi

mkdir -p "$DEST_DIR"

"$JPACKAGE" --type dmg --input "$(dirname "$JAR")" --main-jar "$(basename "$JAR")" --name "$APP_NAME" --app-version "$APP_VERSION" --dest "$DEST_DIR" "${@:2}"

echo "DMG created in $DEST_DIR"
