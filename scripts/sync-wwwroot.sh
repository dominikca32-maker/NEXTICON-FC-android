#!/usr/bin/env bash
set -euo pipefail

GAME_DIST="${1:-}"
ASSETS_DIR="$(cd "$(dirname "$0")/.." && pwd)/app/src/main/assets"

if [[ -n "$GAME_DIST" && -f "$GAME_DIST/index.html" ]]; then
  echo "Syncing game from $GAME_DIST → $ASSETS_DIR"
  find "$ASSETS_DIR" -mindepth 1 ! -name 'README.txt' -exec rm -rf {} + 2>/dev/null || true
  cp -R "$GAME_DIST"/. "$ASSETS_DIR/"
  echo "Done. assets/index.html present: $(test -f "$ASSETS_DIR/index.html" && echo yes || echo no)"
  exit 0
fi

if [[ -f "$ASSETS_DIR/index.html" ]]; then
  echo "assets/index.html already present — nothing to do."
  exit 0
fi

echo "Usage: $0 /path/to/NEXTICON-FC/dist" >&2
echo "Or build NEXTICON-FC first: pnpm build" >&2
exit 1
