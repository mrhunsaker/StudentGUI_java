#!/usr/bin/env bash
set -euo pipefail

# Usage: ./generate-appimage.sh [app-version]
# Uses jpackage to create an app-image, then (if available) runs appimagetool to produce an AppImage

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

echo "Generating app-image via jpackage..."
"$JPACKAGE" --type app-image --input "$(dirname "$JAR")" --main-jar "$(basename "$JAR")" --name "$APP_NAME" --app-version "$APP_VERSION" --dest "$APPIMAGE_BUILD_DIR" "${@:2}"

# jpackage will create a directory for the app-image. Locate it.
APP_IMAGE_DIR=$(find "$APPIMAGE_BUILD_DIR" -maxdepth 1 -type d -name "*${APP_NAME}*" -print -quit || true)
if [ -z "$APP_IMAGE_DIR" ]; then
  # fallback: try to detect directory containing AppRun or .desktop
  APP_IMAGE_DIR=$(find "$APPIMAGE_BUILD_DIR" -maxdepth 2 -type d -exec sh -c 'ls "{}"/AppRun 2>/dev/null >/dev/null && echo {}' \; -print -quit || true)
fi

if [ -z "$APP_IMAGE_DIR" ]; then
  echo "ERROR: Unable to locate the generated AppImage directory under $APPIMAGE_BUILD_DIR" >&2
  exit 1
fi

echo "App image folder: $APP_IMAGE_DIR"

if command -v appimagetool >/dev/null 2>&1; then
  echo "appimagetool found; building .AppImage..."
  (cd "$APPIMAGE_BUILD_DIR" && appimagetool "$(basename "$APP_IMAGE_DIR")" )
  echo "AppImage created in $APPIMAGE_BUILD_DIR"
else
  # ensure app-image is copied into the generated builds area, dereferencing symlinks
  OUT_APPIMAGE_DIR="$APPIMAGE_BUILD_DIR/$(basename "$APP_IMAGE_DIR")"
  rm -rf "$OUT_APPIMAGE_DIR"
  if command -v rsync >/dev/null 2>&1; then
    rsync -aL "$APP_IMAGE_DIR"/ "$OUT_APPIMAGE_DIR"/
  else
    cp -rL "$APP_IMAGE_DIR"/* "$OUT_APPIMAGE_DIR"/ || true
  fi
  echo "appimagetool not found. Generated app-image is available at: $OUT_APPIMAGE_DIR" >&2
  echo "Install appimagetool (https://github.com/AppImage/AppImageKit) to create a single-file .AppImage, or distribute the app-image folder." >&2
fi
