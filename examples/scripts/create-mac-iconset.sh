#!/usr/bin/env bash
# Generates a macOS .icns file from the scatter-plot PNG set.
# Run on macOS with iconutil available.
# Usage: ./create-mac-iconset.sh [source-dir] [output.icns]
# Default source-dir: ../build/icons ; default output: scatter-plot.icns
set -euo pipefail
SRC_DIR="${1:-$(dirname "$0")/../build/icons}"
OUT_FILE="${2:-scatter-plot.icns}"
ICONSET_DIR="scatter-plot.iconset"

if ! command -v iconutil >/dev/null 2>&1; then
  echo "iconutil not found; install Xcode command line tools first." >&2
  exit 1
fi

rm -rf "$ICONSET_DIR"
mkdir "$ICONSET_DIR"

# Map sizes to expected file names.
# If you only have one large PNG, resize using sips commands.
copy_or_resize() {
  local size=$1
  local retina=$2
  local base_name="scatter-plot-${size}.png"
  local src_png="$SRC_DIR/$base_name"
  local target_name="icon_${size}x${size}${retina}.png"
  if [[ -f "$src_png" ]]; then
    cp "$src_png" "$ICONSET_DIR/$target_name"
  else
    echo "Missing $src_png; attempting resize from largest available." >&2
    local largest=$(ls "$SRC_DIR"/scatter-plot-*.png | sort -V | tail -1)
    sips -z "$size" "$size" "$largest" --out "$ICONSET_DIR/$target_name" >/dev/null
  fi
}

# Standard macOS iconset sizes
for s in 16 32 64 128 256 512; do
  copy_or_resize $s ""
  if [[ $s -ne 16 ]]; then
    rs=$((s*2))
    copy_or_resize $s "@2x" # will resize using same base, naming matches iconutil pattern
    mv "$ICONSET_DIR/icon_${s}x${s}@2x.png" "$ICONSET_DIR/icon_${s}x${s}@2x.png" 2>/dev/null || true
  fi
  if [[ $s -eq 512 ]]; then
    copy_or_resize 512 "@2x" # 1024x1024
  fi
done

iconutil -c icns "$ICONSET_DIR" -o "$OUT_FILE"
echo "Created $OUT_FILE from PNG set."