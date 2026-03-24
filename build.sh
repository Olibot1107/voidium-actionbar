#!/usr/bin/env bash
set -euo pipefail

PLUGIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PARENT_DIR="$(cd "${PLUGIN_DIR}/.." && pwd)"
if [[ -d "${PARENT_DIR}/server" ]]; then
  ROOT_DIR="${PARENT_DIR}"
else
  ROOT_DIR="${PLUGIN_DIR}"
fi

BUILD_DIR="${PLUGIN_DIR}/build"
CLASSES_DIR="${BUILD_DIR}/classes"

rm -rf "${BUILD_DIR}"
mkdir -p "${CLASSES_DIR}"

JAVA_FILES="$(find "${PLUGIN_DIR}/src/main/java" -type f -name '*.java')"

LIB_JARS=()
while IFS= read -r -d '' f; do
  LIB_JARS+=("$f")
done < <(find "${ROOT_DIR}/server/libraries" -type f -name '*.jar' -print0 2>/dev/null || true)

if [[ ${#LIB_JARS[@]} -eq 0 ]]; then
  echo "No jars found under ${ROOT_DIR}/server/libraries; can't compile." >&2
  exit 1
fi

CLASSPATH="$(IFS=:; echo "${LIB_JARS[*]}")"

echo "Compiling..."
javac \
  --release 21 \
  -proc:none \
  -Xlint:all,-classfile \
  -Werror \
  -classpath "${CLASSPATH}" \
  -d "${CLASSES_DIR}" \
  ${JAVA_FILES}

echo "Packaging jar..."
jar --create --file "${BUILD_DIR}/VoidiumActionBar.jar" \
  -C "${CLASSES_DIR}" . \
  -C "${PLUGIN_DIR}/src/main/resources" .

echo "Built: ${BUILD_DIR}/VoidiumActionBar.jar"
