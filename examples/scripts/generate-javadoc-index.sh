#!/usr/bin/env bash
#
# generate-javadoc-index.sh
#
# Purpose:
#   Generate a lightweight landing page (index.html) and metadata for versioned Javadoc
#   publications suitable for a GitHub Pages (gh-pages) branch layout.
#
#   Intended workflow:
#     1. After running: mvn site
#     2. Copy/rsync: target/site/apidocs -> <staging>/LATEST_TMP
#     3. Invoke this script with the release version (e.g. 1.1) and root docs directory.
#     4. Script moves LATEST_TMP to a versioned subdirectory (<root>/<version>/) if not already
#        present, updates versions listing, and writes a top-level index.html that:
#          - Lists all published versions
#          - Redirects (meta refresh + JS) to the latest version's Javadoc package root
#
#   Example:
#     scripts/generate-javadoc-index.sh \
#       --root gh-pages-workdir \
#       --version 1.1 \
#       --latest
#
# Resulting structure (root = gh-pages-workdir):
#   gh-pages-workdir/
#     index.html              (landing/redirect page)
#     versions.json           (machine-readable version metadata)
#     versions.txt            (plain text list)
#     1.0/                    (previous Javadoc)
#     1.1/                    (new Javadoc)
#
# Usage:
#   generate-javadoc-index.sh --root <root_dir> --version <version> [--latest] [--project-url URL]
#                             [--title "Graph Digitizer API"] [--package com.digitizer]
#
# Arguments:
#   --root <dir>        Root directory that becomes the GitHub Pages document root.
#   --version <ver>     Semantic version string for current docs (e.g. 1.2.0, 1.2, v1.2).
#   --latest            Mark this version as the latest (updates redirect target).
#   --project-url <url> Optional project homepage (link displayed on index, defaults to https://github.com/mrhunsaker/Graph_Digitizer_Java_Implementation).
#   --title <text>      Optional custom title for landing page.
#   --package <pkg>     Optional Java base package to deep-link into (e.g. com/digitizer/ui).
#   --help              Show help.
#
# Environment overrides (optional):
#   PROJECT_URL, DOC_TITLE, BASE_PACKAGE
#
# Exit codes:
#   0 success
#   2 usage error
#   3 missing directory or move failure
#
# Notes:
#   - Idempotent: re-running with same version will not duplicate entries.
#   - Will not delete older versions.
#   - Designed to be run inside CI before committing to gh-pages.
#
set -euo pipefail

# ---------------------------
# Default configuration
# ---------------------------
ROOT_DIR=""
VERSION=""
MARK_LATEST=0
PROJECT_URL="${PROJECT_URL:-https://github.com/mrhunsaker/Graph_Digitizer_Java_Implementation}"
DOC_TITLE="${DOC_TITLE:-Graph Digitizer API Documentation}"
BASE_PACKAGE="${BASE_PACKAGE:-}"
INDEX_FILE="index.html"
VERSIONS_JSON="versions.json"
VERSIONS_TXT="versions.txt"
SITE_BASE="${SITE_BASE:-https://mrhunsaker.github.io/Graph_Digitizer_Java_Implementation}"

# ---------------------------
# Helpers
# ---------------------------
print_help() {
  sed -n '1,100p' "$0" | grep -E "^# " | sed 's/^# //'
  cat <<EOF

Script invocation examples:

  # Basic (marks version as latest)
  ./scripts/generate-javadoc-index.sh --root gh-pages-workdir --version 1.1 --latest

  # Provide project URL and deep link into base package
  ./scripts/generate-javadoc-index.sh --root gh-pages-workdir --version 1.2 \
      --latest --project-url https://github.com/your-user/graph-digitizer \
      --package com.digitizer.ui

EOF
}

error() {
  echo "ERROR: $*" >&2
  exit 2
}

info() {
  echo "[info] $*"
}

# ---------------------------
# Parse arguments
# ---------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --root)
      ROOT_DIR="${2:-}"; shift 2;;
    --version)
      VERSION="${2:-}"; shift 2;;
    --latest)
      MARK_LATEST=1; shift;;
    --project-url)
      PROJECT_URL="${2:-}"; shift 2;;
    --title)
      DOC_TITLE="${2:-}"; shift 2;;
    --package)
      BASE_PACKAGE="${2:-}"; shift 2;;
    --help|-h)
      print_help; exit 0;;
    *)
      error "Unknown argument: $1"
      ;;
  esac
done

# ---------------------------
# Validation
# ---------------------------
[[ -z "${ROOT_DIR}" ]] && error "--root is required"
[[ -z "${VERSION}" ]] && error "--version is required"

if [[ ! -d "${ROOT_DIR}" ]]; then
  error "Root directory '${ROOT_DIR}' does not exist"
fi

VERSION_DIR="${ROOT_DIR}/${VERSION}"

# Expect user to have staged Javadoc in a temp location or already in version dir
# Accept either:
#   1. ROOT_DIR/${VERSION} exists (already placed)
#   2. ROOT_DIR/apidocs-temp exists and needs to be moved
TEMP_SRC="${ROOT_DIR}/apidocs-temp"

if [[ -d "${VERSION_DIR}" ]]; then
  info "Version directory already present: ${VERSION_DIR}"
elif [[ -d "${TEMP_SRC}" ]]; then
  info "Moving temp Javadoc '${TEMP_SRC}' to '${VERSION_DIR}'"
  mv "${TEMP_SRC}" "${VERSION_DIR}"
else
  error "Neither version directory '${VERSION_DIR}' nor temp source '${TEMP_SRC}' exists. Place generated Javadoc first."
fi

# Basic sanity: confirm there is an index.html inside version directory
if [[ ! -f "${VERSION_DIR}/index.html" ]]; then
  error "No index.html found inside '${VERSION_DIR}'. Javadoc generation may have failed."
fi

# ---------------------------
# Update versions metadata
# ---------------------------
VERSIONS_LIST=()
if [[ -f "${ROOT_DIR}/${VERSIONS_TXT}" ]]; then
  mapfile -t VERSIONS_LIST < "${ROOT_DIR}/${VERSIONS_TXT}"
fi

# Add new version if not present
if ! grep -qx "${VERSION}" "${ROOT_DIR}/${VERSIONS_TXT}" 2>/dev/null; then
  info "Appending version '${VERSION}' to ${VERSIONS_TXT}"
  echo "${VERSION}" >> "${ROOT_DIR}/${VERSIONS_TXT}"
fi

# Re-read list (sorted descending using version sort, fallback lexical)
mapfile -t VERSIONS_LIST < "${ROOT_DIR}/${VERSIONS_TXT}"
SORTED_VERSIONS=$(printf "%s\n" "${VERSIONS_LIST[@]}" | sort -rV)

# Create JSON metadata
{
  echo "{"
  echo "  \"latest\": \"$(printf "%s\n" "${SORTED_VERSIONS}" | head -n1)\","
  echo "  \"versions\": ["
  COUNT=0
  TOTAL=$(printf "%s\n" "${SORTED_VERSIONS}" | wc -l | tr -d ' ')
  while read -r v; do
    COUNT=$((COUNT+1))
    COMMA=","
    [[ ${COUNT} -eq ${TOTAL} ]] && COMMA=""
    echo "    \"${v}\"${COMMA}"
  done <<< "${SORTED_VERSIONS}"
  echo "  ]"
  echo "}"
} > "${ROOT_DIR}/${VERSIONS_JSON}"

info "Wrote versions metadata: ${VERSIONS_JSON}"

LATEST_VERSION=$(jq -r '.latest' "${ROOT_DIR}/${VERSIONS_JSON}" 2>/dev/null || printf "%s" "$(printf "%s\n" "${SORTED_VERSIONS}" | head -n1)")

# If explicitly marking latest and differs, force override
if [[ ${MARK_LATEST} -eq 1 && "${LATEST_VERSION}" != "${VERSION}" ]]; then
  info "Overriding latest to '${VERSION}' (was '${LATEST_VERSION}')"
  LATEST_VERSION="${VERSION}"
  # Re-write JSON with explicit override
  {
    echo "{"
    echo "  \"latest\": \"${LATEST_VERSION}\","
    echo "  \"versions\": ["
    COUNT=0
    TOTAL=$(printf "%s\n" "${SORTED_VERSIONS}" | wc -l | tr -d ' ')
    while read -r v; do
      COUNT=$((COUNT+1))
      COMMA=","
      [[ ${COUNT} -eq ${TOTAL} ]] && COMMA=""
      echo "    \"${v}\"${COMMA}"
    done <<< "${SORTED_VERSIONS}"
    echo "  ]"
    echo "}"
  } > "${ROOT_DIR}/${VERSIONS_JSON}"
fi

# ---------------------------
# Landing page (index.html)
# ---------------------------
REDIRECT_TARGET="${SITE_BASE}/${LATEST_VERSION}/index.html"
# Deep package linking disabled to ensure redirect hits version root index.html
# (Previously attempted to link into package path which lacked its own index.html)
PROJECT_LINK_HTML="<p><a href=\"${PROJECT_URL}\">Project Homepage</a></p>"

# Generate HTML
cat > "${ROOT_DIR}/${INDEX_FILE}" <<EOF
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>${DOC_TITLE}</title>
  <meta http-equiv="refresh" content="4; url=${REDIRECT_TARGET}" />
  <style>
    body { font-family: system-ui, Arial, sans-serif; margin: 2rem; line-height: 1.5; }
    h1 { margin-top: 0; }
    code { background: #f5f5f5; padding: 0.2rem 0.4rem; border-radius: 4px; }
    .versions { margin-top: 1.5rem; }
    .latest { font-weight: bold; color: #0a5; }
    .footer { margin-top: 2.5rem; font-size: 0.85rem; color: #666; }
    a { color: #0366d6; text-decoration: none; }
    a:hover { text-decoration: underline; }
  </style>
  <script>
    setTimeout(function(){ window.location.href = "${REDIRECT_TARGET}"; }, 4000);
  </script>
</head>
<body>
  <h1>${DOC_TITLE}</h1>
  <p>You will be redirected shortly to the latest API documentation: <strong>${LATEST_VERSION}</strong>.</p>
  ${PROJECT_LINK_HTML}
  <div class="versions">
    <h2>Published Versions</h2>
    <ul>
EOF

while read -r v; do
  CLASS=""
  [[ "${v}" == "${LATEST_VERSION}" ]] && CLASS="latest"
  echo "      <li class=\"${CLASS}\"><a href=\"${SITE_BASE}/${v}/index.html\">${v}</a> $( [[ "${CLASS}" == "latest" ]] && echo "(latest)" )</li>" >> "${ROOT_DIR}/${INDEX_FILE}"
done <<< "${SORTED_VERSIONS}"

cat >> "${ROOT_DIR}/${INDEX_FILE}" <<EOF
    </ul>
  </div>
  <div class="footer">
    <p>Generated on $(date -u +"%Y-%m-%d %H:%M UTC").</p>
    <p>Metadata: <a href="${SITE_BASE}/${VERSIONS_JSON}">${VERSIONS_JSON}</a> | <a href="${SITE_BASE}/${VERSIONS_TXT}">${VERSIONS_TXT}</a></p>
  </div>
</body>
</html>
EOF

info "Landing page generated: ${ROOT_DIR}/${INDEX_FILE}"
info "Redirect target: ${REDIRECT_TARGET}"
info "Latest version: ${LATEST_VERSION}"
info "Done."
