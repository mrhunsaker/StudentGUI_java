#!/usr/bin/env bash
set -euo pipefail

# Helper script run inside a container to create a per-arch runtime artifact.
# Usage: ./build_runtime_in_container.sh <arch>
# Example: ./build_runtime_in_container.sh amd64

ARCH="$1"
OUT_DIR="target/generated_builds"
RT_DIR="$OUT_DIR/runtime-${ARCH}"

echo "Building runtime for arch: ${ARCH}"
mkdir -p "${RT_DIR}"

if command -v jlink >/dev/null 2>&1; then
  echo "jlink detected. Running example jlink invocation (customize for your project)."
  # NOTE: Customize the following jlink command for your project modules and options.
  # Example placeholder that will create a minimal runtime image. Replace --add-modules with your app modules.
  jlink --add-modules java.base --output "${RT_DIR}/runtime" || true
else
  echo "jlink not available in this container. Creating a dummy runtime layout for example purposes."
  mkdir -p "${RT_DIR}/runtime/bin"
  cat > "${RT_DIR}/runtime/bin/app" <<'EOF'
#!/bin/sh
echo "Hello from runtime ${ARCH}"
EOF
  chmod +x "${RT_DIR}/runtime/bin/app"
fi

echo "Packaging runtime artifact into zip..."
pushd "${RT_DIR}" > /dev/null
zip -r "../runtime-${ARCH}.zip" ./*
popd > /dev/null

echo "Created ${OUT_DIR}/runtime-${ARCH}.zip"
