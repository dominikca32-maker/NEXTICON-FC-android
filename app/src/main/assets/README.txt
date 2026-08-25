NEXTICON FC game files go here (root of assets/, not a subfolder).

Before building the APK, copy the production build from NEXTICON-FC:

  pnpm build   # in NEXTICON-FC → dist/

Then run from this repo:

  ./scripts/sync-wwwroot.sh ../NEXTICON-FC/dist

You should end up with:

  app/src/main/assets/index.html
  app/src/main/assets/assets/…
  app/src/main/assets/event-heroes/…

The WebView host maps https://app.local/ → these files for offline SPA routing.
