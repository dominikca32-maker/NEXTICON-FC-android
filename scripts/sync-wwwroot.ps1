param(
  [string]$GameDist = ""
)

$ErrorActionPreference = "Stop"
$AssetsDir = Join-Path $PSScriptRoot "..\app\src\main\assets" | Resolve-Path

if ($GameDist -ne "" -and (Test-Path "$GameDist\index.html")) {
  Write-Host "Syncing game from $GameDist → $AssetsDir"
  Get-ChildItem $AssetsDir -Exclude README.txt | Remove-Item -Recurse -Force
  Copy-Item -Path "$GameDist\*" -Destination $AssetsDir -Recurse -Force
  Write-Host "Done."
  exit 0
}

if (Test-Path "$AssetsDir\index.html") {
  Write-Host "assets/index.html already present — nothing to do."
  exit 0
}

throw "Usage: .\scripts\sync-wwwroot.ps1 -GameDist path\to\NEXTICON-FC\dist"
