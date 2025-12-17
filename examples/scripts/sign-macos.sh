#!/usr/bin/env bash
# Code signing & notarization helper for macOS build artifacts.
# Requires environment variables:
#   MAC_IDENTITY="Developer ID Application: Your Name (TEAMID)"
#   MAC_NOTARIZE_APPLE_ID="appleid@example.com"
#   MAC_NOTARIZE_TEAM_ID="TEAMID"
#   MAC_NOTARIZE_PASS="app-specific-password"
# Usage:
#   ./scripts/sign-macos.sh target/GraphDigitizer.app
set -euo pipefail
APP_PATH=${1:-target/GraphDigitizer.app}
if [ ! -d "$APP_PATH" ]; then
  echo "App path not found: $APP_PATH" >&2; exit 1
fi
: "${MAC_IDENTITY:?Missing MAC_IDENTITY}"
: "${MAC_NOTARIZE_APPLE_ID:?Missing MAC_NOTARIZE_APPLE_ID}"
: "${MAC_NOTARIZE_TEAM_ID:?Missing MAC_NOTARIZE_TEAM_ID}"
: "${MAC_NOTARIZE_PASS:?Missing MAC_NOTARIZE_PASS}"

echo "Signing app bundle..."
codesign --deep --force --options runtime --sign "$MAC_IDENTITY" "$APP_PATH"

echo "Verifying signature..."
codesign --verify --verbose=2 "$APP_PATH"
spctl --verbose=4 --assess --type execute "$APP_PATH" || echo "Gatekeeper assessment warning"

echo "Creating ZIP for notarization..."
ZIP_NAME="$(basename "$APP_PATH").zip"
/usr/bin/ditto -c -k --keepParent "$APP_PATH" "$ZIP_NAME"

echo "Submitting for notarization..."
xcrun notarytool submit "$ZIP_NAME" --apple-id "$MAC_NOTARIZE_APPLE_ID" --team-id "$MAC_NOTARIZE_TEAM_ID" --password "$MAC_NOTARIZE_PASS" --wait

echo "Stapling ticket..."
xcrun stapler staple "$APP_PATH"

echo "Done. Signed and notarized: $APP_PATH"
