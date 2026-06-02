#!/bin/sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPERTIES_FILE="$ROOT_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$PROPERTIES_FILE" ]; then
  echo "Missing $PROPERTIES_FILE" >&2
  exit 1
fi

distribution_url=$(sed -n 's/^distributionUrl=//p' "$PROPERTIES_FILE" | head -n 1)

if [ -z "$distribution_url" ]; then
  echo "Missing distributionUrl in $PROPERTIES_FILE" >&2
  exit 1
fi

if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
fi

MAVEN_USER_HOME="${MAVEN_USER_HOME:-$HOME/.m2}"
WRAPPER_BASE="$MAVEN_USER_HOME/wrapper/dists"
DIST_NAME=$(basename "$distribution_url" .zip)
DIST_BASE=${DIST_NAME%-bin}
DIST_DIR="$WRAPPER_BASE/$DIST_BASE"
MAVEN_BIN="$DIST_DIR/bin/mvn"

if [ ! -x "$MAVEN_BIN" ]; then
  mkdir -p "$WRAPPER_BASE"
  TMP_DIR=$(mktemp -d)
  ZIP_FILE="$TMP_DIR/$DIST_NAME.zip"
  curl -fsSL "$distribution_url" -o "$ZIP_FILE"
  unzip -q "$ZIP_FILE" -d "$WRAPPER_BASE"
  rm -rf "$TMP_DIR"
fi

exec "$MAVEN_BIN" "$@"
