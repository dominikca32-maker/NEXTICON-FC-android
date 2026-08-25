#!/usr/bin/env bash
set -euo pipefail

GAME_DIST="${1:-}"
ASSETS_DIR="$(cd "$(dirname "$0")/../app/src/main/assets" && pwd)"

if [[ -z "$GAME_DIST" ]]; then
  echo "Usage: $0 <path-to-game-dist>" >&2
  exit 1
fi

if [[ ! -d "$GAME_DIST" ]]; then
  echo "Game dist not found: $GAME_DIST" >&2
  exit 1
fi

rm -rf "${ASSETS_DIR:?}/"*
cp -a "$GAME_DIST"/. "$ASSETS_DIR"/
echo "Synced $GAME_DIST -> $ASSETS_DIR"
