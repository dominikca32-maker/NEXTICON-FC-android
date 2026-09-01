# NEXTICON FC — Android WebView shell

Android wrapper for [NEXTICON-FC](https://github.com/dominikca32-maker/NEXTICON-FC).  
Runs the Vite build inside a **WebView** — no browser tab.

## Offline and Online

| Mode | What you get |
|---|---|
| **Offline** | Solo career, Hub, settings, local saves from the bundled `dist`. No network. |
| **Online** | Casual Multiplayer (Freundschaftsspiel) when the phone has a connection **and** the bundled game was built with `VITE_ONLINE_MODE` + Supabase Realtime. Hub badges Multiplayer `offline` and hides a dead QR otherwise. |
| **Deep link** | `https://nexticon-fc.onrender.com/j/{code}` and the staging host open **in this app**. Online: the hosted join URL. Offline: local Hub, not a fake table. |

Two phones need Realtime. The local virtual host is `https://app.local/` (bundled assets). Supabase, `*.onrender.com`, and other origins are **not** intercepted — they go to the network. SPA paths (`/j/…`) fall back to `index.html`.

Verified Android App Links need a signing SHA-256 in `/.well-known/assetlinks.json` on the hosted site. Until then the VIEW / BROWSABLE filters still offer **Open with NEXTICON FC**.

There is **no** Ranked / server host in this shell.

## Download

Open **Releases** on this repo and download `NEXTICON-FC-android.apk`.

1. Allow install from unknown sources (or use `adb install`)
2. Open **NEXTICON FC**
3. Offline Solo works from the bundle. Multiplayer, FlagCDN flags, Google Fonts, and optional cloud login need network.

## How it works

The app maps `https://app.local/` → bundled assets so SPA absolute paths (`/assets/…`) work offline, same idea as the [Windows WebView2 repo](https://github.com/dominikca32-maker/NEXTICON-FC-webview).

```
app/src/main/assets/
  index.html
  assets/
  event-heroes/
  …
```

Back button walks WebView history, then leaves the activity. Camera is optional (QR scan). `usesCleartextTraffic` stays off.

## Update the bundled game

From a machine that can build NEXTICON-FC (use **staging** so Casual Multiplayer is in the bundle; bake `VITE_ONLINE_MODE=true` and the staging Supabase keys if you want two-phone tables):

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

CI rebuilds the APK and publishes a new Release. A store / sideload rebuild is required after a `dist` sync — a web hard-reload is not enough.

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
- Optional Ranking / cloud login need backend + network. OAuth redirect on `app.local` is not a hosted origin.
- Release builds on GitHub Actions are **debug-signed** APKs for sideloading. Play Store needs your own signing key.

## License

WebView host code in this repo. Game content follows NEXTICON-FC.
