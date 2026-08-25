# NEXTICON FC — Android WebView shell

Android wrapper for [NEXTICON-FC](https://github.com/dominikca32-maker/NEXTICON-FC).  
Runs the production Vite build inside a **WebView** — no browser tab, works offline on your phone.

## Download

Open **Releases** on this repo and download `NEXTICON-FC-android.apk`.

1. Allow install from unknown sources (or use `adb install`)
2. Open **NEXTICON FC**
3. Needs network only for FlagCDN country flags, Google Fonts, and optional cloud login

## How it works

The app maps `https://app.local/` → bundled assets so SPA absolute paths (`/assets/…`) work offline, same idea as the [Windows WebView2 repo](https://github.com/dominikca32-maker/NEXTICON-FC-webview).

```
app/src/main/assets/
  index.html
  assets/
  event-heroes/
  …
```

## Update the bundled game

From a machine that can build NEXTICON-FC:

```bash
cd path/to/NEXTICON-FC
git checkout staging
pnpm install --frozen-lockfile
pnpm build

cd path/to/NEXTICON-FC-android
./scripts/sync-wwwroot.sh ../NEXTICON-FC/dist
git add app/src/main/assets
git commit -m "Sync assets from staging"
git push
```

PowerShell: `.\scripts\sync-wwwroot.ps1 -GameDist ..\NEXTICON-FC\dist`

CI rebuilds the APK and publishes a new Release.

### CI secret (recommended)

The game repo is private. For GitHub Actions to build a complete APK without committing every `dist/` file, add a repository secret:

- **`GAME_REPO_TOKEN`** — GitHub PAT with `repo` read access to `NEXTICON-FC`

CI clones `staging`, runs `pnpm build`, syncs into `app/src/main/assets/`, then builds the APK.

## Local build

Requirements: JDK 17+, Android SDK (or Android Studio).

```bash
./scripts/sync-wwwroot.sh ../NEXTICON-FC/dist
chmod +x gradlew
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

Install on a device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Limits

- **Android only** (this repo). iPhone → PWA in Safari or a separate iOS wrapper.
- Country flags (FlagCDN) and fonts need internet; Event heroes and UI are local.
- Optional Ranking / cloud login need backend + network.
- Release builds on GitHub Actions are **debug-signed** APKs for sideloading. Play Store needs your own signing key.

## License

WebView host code in this repo. Game content follows NEXTICON-FC.
