#!/usr/bin/env bash
set -euo pipefail

# Usage: ./generate-snap.sh [app-version]
# Attempts to create a snap from the jpackage app-image output using snapcraft if available.

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
APPIMAGE_BUILD_DIR="$DEST_DIR/appimage"
mkdir -p "$APPIMAGE_BUILD_DIR"

echo "Generating app-image via jpackage (snap building uses the app-image)..."
"$JPACKAGE" --type app-image --input "$(dirname "$JAR")" --main-jar "$(basename "$JAR")" --name "$APP_NAME" --app-version "$APP_VERSION" --dest "$APPIMAGE_BUILD_DIR" "${@:2}"

# Locate AppDir produced by jpackage
APP_IMAGE_DIR=$(find "$APPIMAGE_BUILD_DIR" -maxdepth 1 -type d -name "*${APP_NAME}*" -print -quit || true)
if [ -z "$APP_IMAGE_DIR" ]; then
  echo "ERROR: Could not find app-image dir. Aborting." >&2
  exit 1
fi

if command -v snapcraft >/dev/null 2>&1; then
  echo "snapcraft found; creating temporary snap project..."
  TMP_SNAP="$DEST_DIR/snap-temp"
  rm -rf "$TMP_SNAP"
  mkdir -p "$TMP_SNAP/prime"
  # copy appimage contents into prime, dereference symlinks when possible
  if command -v rsync >/dev/null 2>&1; then
    rsync -aL "$APP_IMAGE_DIR"/ "$TMP_SNAP/prime/"
  else
    cp -rL "$APP_IMAGE_DIR"/* "$TMP_SNAP/prime/" || true
  fi

  cat > "$TMP_SNAP/snapcraft.yaml" <<EOF
name: $APP_NAME
version: '$APP_VERSION'
summary: $APP_NAME
description: $APP_NAME packaged snap
grade: stable
confinement: strict
apps:
  $APP_NAME:
    command: bin/$APP_NAME
parts: {}
EOF

  (cd "$TMP_SNAP" && snapcraft pack)
  echo "snap (.snap) created in $TMP_SNAP"
  echo "Move or copy the .snap from $TMP_SNAP to your desired location."
else
  # ensure app-image is available under generated_builds and dereferenced
  OUT_APPIMAGE_DIR="$APPIMAGE_BUILD_DIR/$(basename "$APP_IMAGE_DIR")"
  rm -rf "$OUT_APPIMAGE_DIR"
  if command -v rsync >/dev/null 2>&1; then
    rsync -aL "$APP_IMAGE_DIR"/ "$OUT_APPIMAGE_DIR"/
  else
    cp -rL "$APP_IMAGE_DIR"/* "$OUT_APPIMAGE_DIR"/ || true
  fi
  echo "snapcraft not found. I generated an app-image under: $OUT_APPIMAGE_DIR" >&2
  echo "To build a snap automatically, install snapcraft and re-run this script. Alternatively, package manually using snapcraft pack in a project directory." >&2
fi
