#!/usr/bin/env bash
set -euo pipefail

# Usage: ./generate-rpm.sh [app-version]
# Checks JAVA_HOME, finds the artifact under ../target, and uses jpackage to create a .rpm

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

echo "Attempting to create RPM with jpackage..."
if "$JPACKAGE" --type rpm --input "$(dirname "$JAR")" --main-jar "$(basename "$JAR")" --name "$APP_NAME" --app-version "$APP_VERSION" --dest "$DEST_DIR" "${@:2}"; then
  echo "RPM artifact created in $DEST_DIR"
  exit 0
else
  echo "jpackage did not support RPM on this system or failed. Falling back to app-image + fpm (if available)." >&2
fi

# Fallback: create an app-image in a temporary system dir (avoid filesystem permission issues), then use fpm to create an rpm if possible
TMP_APPIMAGE_DIR=$(mktemp -d /tmp/${APP_NAME}.appimage.XXXX)
# preserve flag: when set to 1 we will keep the tmp dir for inspection
PRESERVE_TMP=0
trap 'if [ "$PRESERVE_TMP" -eq 0 ]; then rm -rf "$TMP_APPIMAGE_DIR"; fi' EXIT
echo "Generating app-image via jpackage in temporary directory $TMP_APPIMAGE_DIR..."
"$JPACKAGE" --type app-image --input "$(dirname "$JAR")" --main-jar "$(basename "$JAR")" --name "$APP_NAME" --app-version "$APP_VERSION" --dest "$TMP_APPIMAGE_DIR" "${@:2}"

FOUND_APP_DIR=$(find "$TMP_APPIMAGE_DIR" -maxdepth 1 -type d -name "*${APP_NAME}*" -print -quit || true)
if [ -z "$FOUND_APP_DIR" ]; then
  echo "ERROR: Unable to locate jpackage app-image directory under $TMP_APPIMAGE_DIR" >&2
  exit 1
fi

echo "App image produced at: $FOUND_APP_DIR"

mkdir -p "$DEST_DIR"

if command -v fpm >/dev/null 2>&1; then
  echo "fpm found; creating RPM from app-image contents..."
  # Use a tmp copy on the local filesystem (/tmp) to avoid symlink/FS issues on repo mount
  TMP_COPY=$(mktemp -d /tmp/${APP_NAME}.rpmroot.XXXX)
  mkdir -p "$TMP_COPY/opt/$APP_NAME"
  # prefer rsync (dereference symlinks) if available
  if command -v rsync >/dev/null 2>&1; then
    rsync -aL "$FOUND_APP_DIR"/ "$TMP_COPY/opt/$APP_NAME/"
  else
    cp -r "$FOUND_APP_DIR"/* "$TMP_COPY/opt/$APP_NAME/" || true
  fi

  (cd "$TMP_COPY" && fpm -s dir -t rpm -n "$APP_NAME" -v "$APP_VERSION" --prefix / -C . opt) \
    && mv "$TMP_COPY"/*.rpm "$DEST_DIR/" 2>/dev/null || true

  # clean tmp copy
  rm -rf "$TMP_COPY"

  if ls "$DEST_DIR"/*.rpm >/dev/null 2>&1; then
    echo "RPM created in $DEST_DIR"
    exit 0
  else
    echo "fpm failed to produce an RPM. Check fpm output above." >&2
    PRESERVE_TMP=1
    echo "App-image preserved at: $TMP_APPIMAGE_DIR" >&2
    exit 1
  fi
else
  echo "fpm not found. To use the fallback, install 'fpm' (https://github.com/jordansissel/fpm) or install system rpm packaging tools so jpackage can produce rpm (e.g. rpm-build)." >&2
  # attempt to copy app-image folder back to generated_builds for inspection, prefer rsync to dereference symlinks
  OUT_APPIMAGE_DIR="$DEST_DIR/appimage"
  rm -rf "$OUT_APPIMAGE_DIR"
  mkdir -p "$(dirname "$OUT_APPIMAGE_DIR")"
  if command -v rsync >/dev/null 2>&1; then
    if rsync -aL "$FOUND_APP_DIR"/ "$OUT_APPIMAGE_DIR"/; then
      echo "App-image copied to: $OUT_APPIMAGE_DIR" >&2
      # clean tmp dir
      PRESERVE_TMP=0
      rm -rf "$TMP_APPIMAGE_DIR"
      exit 1
    else
      echo "rsync failed to copy app-image to $OUT_APPIMAGE_DIR. Preserving temporary app-image at: $TMP_APPIMAGE_DIR" >&2
      PRESERVE_TMP=1
      exit 1
    fi
  else
    # fallback: try to use cp with -L to dereference symlinks
    if cp -rL "$FOUND_APP_DIR"/* "$OUT_APPIMAGE_DIR"/ 2>/dev/null; then
      echo "App-image copied to: $OUT_APPIMAGE_DIR (via cp -rL)" >&2
      PRESERVE_TMP=0
      rm -rf "$TMP_APPIMAGE_DIR"
      exit 1
    fi
    echo "rsync not available and cp -rL failed. Preserving temporary app-image at: $TMP_APPIMAGE_DIR" >&2
    PRESERVE_TMP=1
    exit 1
  fi
fi
