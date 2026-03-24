#!/usr/bin/env bash
set -euo pipefail

PLUGIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${PLUGIN_DIR}/.." && pwd)"

"${PLUGIN_DIR}/build.sh"

cp -f \
  "${PLUGIN_DIR}/build/VoidiumActionBar.jar" \
  "${ROOT_DIR}/server/plugins/VoidiumActionBar.jar"

echo "Installed to: ${ROOT_DIR}/server/plugins/VoidiumActionBar.jar"
echo "Restart your server (or use a plugin manager) to load it."
